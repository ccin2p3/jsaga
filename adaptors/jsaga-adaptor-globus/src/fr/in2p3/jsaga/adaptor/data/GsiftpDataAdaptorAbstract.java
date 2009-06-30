package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.globus.common.ChainedIOException;
import org.globus.ftp.*;
import org.globus.ftp.exception.*;
import org.globus.gsi.gssapi.GlobusGSSException;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpDataAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class GsiftpDataAdaptorAbstract implements DataCopy, DataRename,
//    FileReaderGetter, FileWriterPutter
        FileReaderStreamFactory, FileWriterStreamFactory
{
    protected static final String TCP_BUFFER_SIZE = "TCPBufferSize";
    protected int m_TCPBufferSize;
    protected boolean m_DataChannelAuthentication;

    protected GSSCredential m_credential;
    protected GridFTPClient m_client;

    public abstract String getType();
    public abstract Usage getUsage();
    public abstract Default[] getDefaults(Map attributes) throws IncorrectStateException;
    public abstract FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;
    public abstract FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_credential = ((GSSCredentialSecurityAdaptor) securityAdaptor).getGSSCredential();
    }

    public BaseURL getBaseURL() throws IncorrectURLException {
        return new BaseURL(2811);
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // configure
        if (attributes!=null && attributes.containsKey(TCP_BUFFER_SIZE)) {
            try {
                m_TCPBufferSize = Integer.parseInt((String) attributes.get(TCP_BUFFER_SIZE));
            } catch (NumberFormatException e) {
                throw new BadParameterException("Bad value for configuration attribute: "+TCP_BUFFER_SIZE, e);
            }
        }
        m_DataChannelAuthentication = true;

        // open connection
        m_client = createConnection(m_credential, host, port, m_DataChannelAuthentication);
    }

    public void disconnect() throws NoSuccessException {
        try {
            m_client.close();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            boolean exists = m_client.exists(absolutePath);

            //workaround: if permission is denied, throw an exception instead of returning false
            if (exists) {
                return true;
            } else {
                try {
                    this.getAttributes(absolutePath, additionalArgs);   //may throw a PermissionDenied exception
                    return true;
                } catch (DoesNotExistException e) {
                    return false;
                }
            }
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (DoesNotExistException doesNotExist) {
                throw new NoSuccessException(e);
            } catch (BadParameterException badParameter) {
                throw new NoSuccessException("Unexpected exception", e);
            }
        }
    }

    /** not used (too slow) */
    public void getToStream(String absolutePath, String additionalArgs, OutputStream stream) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        final boolean autoFlush = false;
        final boolean ignoreOffset = true;
        try {
            if (m_TCPBufferSize > 0) {
                m_client.setTCPBufferSize(m_TCPBufferSize);
            }

            m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_client.setMode(GridFTPSession.MODE_STREAM); //MODE_EBLOCK induce error: "451 refusing to store with active mode"
            m_client.setPassive();
            m_client.setLocalActive();
            m_client.get(
                    absolutePath,
                    new DataSinkStream(stream, autoFlush, ignoreOffset),
                    null);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    /** not used (too slow) */
    public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        final int DEFAULT_BUFFER_SIZE = 16384;
        try {
            if (m_TCPBufferSize > 0) {
                m_client.setTCPBufferSize(m_TCPBufferSize);
            }

            m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_client.setMode(GridFTPSession.MODE_EBLOCK);
            m_client.setPassive();
            m_client.setLocalActive();
            m_client.put(
                absolutePath,
                new DataSourceStream(stream, DEFAULT_BUFFER_SIZE),
                    null,
                    append);
        } catch (Exception e) {
            try {
                throw rethrowExceptionFull(e);
            } catch (DoesNotExistException e2) {
                throw new ParentDoesNotExist(e);
            }
        }
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // create input stream
        try {
            return new GsiftpInputStream(m_client, absolutePath);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+"/"+fileName;

        // test existence
        if (exclusive) {
            boolean exists;
            try {
                exists = m_client.exists(absolutePath);
            } catch (Exception e) {
                try {
                    throw rethrowExceptionFull(e);
                } catch (DoesNotExistException e1) {
                    throw new ParentDoesNotExist(e);
                }
            }
            if (exists) {
                throw new AlreadyExistsException("File already exists: "+fileName);
            }
        }

        // create new connection (else test setUp hangs)
        GridFTPClient tmpConnection = createConnection(m_credential, m_client, m_DataChannelAuthentication);

        // create output stream
        try {
            return new GsiftpOutputStream(tmpConnection, absolutePath, append);
        } catch (Exception e) {
            try {
                throw rethrowExceptionFull(e);
            } catch (DoesNotExistException e2) {
                throw new ParentDoesNotExist(e);
            }
        }
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        // connect to peer server
        GsiftpDataAdaptorAbstract targetAdaptor = new Gsiftp1DataAdaptor();
        targetAdaptor.m_credential = m_credential;
        targetAdaptor.connect(null, targetHost, targetPort, null, null);

        //todo: remove this block when overwriting target file will work (it only works with UrlCopy)
        if (overwrite && targetAdaptor.exists(targetAbsolutePath, additionalArgs)) {
            try {
                targetAdaptor.m_client.deleteFile(targetAbsolutePath);
            } catch (Exception e) {
                throw new PermissionDeniedException("Failed to overwrite target file", e);
            }
        }

        // need to check existence of target explicitely, else exception is never thrown
        if (!overwrite && targetAdaptor.exists(targetAbsolutePath, additionalArgs)) {
            throw new AlreadyExistsException("File already exists");
        }

        try {
            // for compatibility with VDT-1.6, .NET implementation
            m_client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            targetAdaptor.m_client.setDataChannelAuthentication(DataChannelAuthentication.NONE);

            // transfer file
            m_client.setType(GridFTPSession.TYPE_IMAGE);
            targetAdaptor.m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_client.setMode(GridFTPSession.MODE_EBLOCK);
            targetAdaptor.m_client.setMode(GridFTPSession.MODE_EBLOCK);
            m_client.setStripedActive(targetAdaptor.m_client.setStripedPassive());
            m_client.extendedTransfer(sourceAbsolutePath, targetAdaptor.m_client, targetAbsolutePath, null);
        } catch (Exception e) {
            throw rethrowExceptionFull(e);
        } finally {
            // disconnect from peer server
            targetAdaptor.disconnect();
        }
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        // connect to peer server
        GsiftpDataAdaptorAbstract sourceAdaptor = new Gsiftp1DataAdaptor();
        sourceAdaptor.m_credential = m_credential;
        sourceAdaptor.connect(null, sourceHost, sourcePort, null, null);

        //todo: remove this block when overwriting target file will work (it only works with UrlCopy)
        if (overwrite && this.exists(targetAbsolutePath, additionalArgs)) {
            try {
                m_client.deleteFile(targetAbsolutePath);
            } catch (Exception e) {
                throw new PermissionDeniedException("Failed to overwrite target file", e);
            }
        }

        // need to check existence of target explicitely, else exception is never thrown
        if (!overwrite && this.exists(targetAbsolutePath, additionalArgs)) {
            throw new AlreadyExistsException("File already exists");
        }

        try {
            // for compatibility with VDT-1.6, .NET implementation
            sourceAdaptor.m_client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
            m_client.setDataChannelAuthentication(DataChannelAuthentication.NONE);

            // transfer file
            sourceAdaptor.m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_client.setType(GridFTPSession.TYPE_IMAGE);
            sourceAdaptor.m_client.setMode(GridFTPSession.MODE_EBLOCK);
            m_client.setMode(GridFTPSession.MODE_EBLOCK);
            sourceAdaptor.m_client.setStripedActive(m_client.setStripedPassive());
            sourceAdaptor.m_client.extendedTransfer(sourceAbsolutePath, m_client, targetAbsolutePath, null);
        } catch (Exception e) {
            throw rethrowExceptionFull(e);
        } finally {
            // disconnect from peer server
            sourceAdaptor.disconnect();
        }
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        try {
            m_client.rename(sourceAbsolutePath, targetAbsolutePath);
        } catch (Exception e) {
            throw rethrowExceptionFull(e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            m_client.deleteFile(parentAbsolutePath+"/"+fileName);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        try {
            m_client.makeDir(parentAbsolutePath+"/"+directoryName);
        } catch (Exception e) {
            try {
                throw rethrowExceptionFull(e);
            } catch (DoesNotExistException e2) {
                throw new ParentDoesNotExist(e);
            }
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            m_client.deleteDir(parentAbsolutePath+"/"+directoryName);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    private static GridFTPClient createConnection(GSSCredential cred, GridFTPClient client, boolean reqDCAU) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            return createConnection(cred, client.getHost(), client.getPort(), reqDCAU);
        } catch (AuthenticationFailedException e) {
            throw new PermissionDeniedException(e);
        } catch (AuthorizationFailedException e) {
            throw new PermissionDeniedException(e);
        }
    }
    private static GridFTPClient createConnection(GSSCredential cred, String host, int port, boolean reqDCAU) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        try {
            GridFTPClient client = new GridFTPClient(host, port);
            client.setAuthorization(HostAuthorization.getInstance());
            client.authenticate(cred);

            // may disable data channel authentication
            if (client.isFeatureSupported("DCAU")) {
                if (! reqDCAU) {
                    client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
                }
            } else {
                client.setLocalNoDataChannelAuthentication();
            }

            // returns
            return client;
        } catch (ChainedIOException e) {
            try {
                throw e.getException();
            } catch (GlobusGSSException gssException) {
                throw new AuthenticationFailedException(gssException);
            } catch (Throwable throwable) {
                throw new TimeoutException(throwable);
            }
        } catch (IOException e) {
            if (e.getMessage()!=null && e.getMessage().indexOf("Authentication") > -1) {
                throw new AuthenticationFailedException(e);
            } else {
                throw new TimeoutException(e);
            }
        } catch (ServerException e) {
            switch(e.getCode()) {
                case ServerException.SERVER_REFUSED:
                    try {
                        throw e.getRootCause();
                    } catch (UnexpectedReplyCodeException unexpectedReplyCode) {
                        switch(unexpectedReplyCode.getReply().getCode()) {
                            case 530:
                                throw new AuthorizationFailedException(unexpectedReplyCode);
                            default:
                                throw new NoSuccessException(unexpectedReplyCode);
                        }
                    } catch (Exception e1) {
                        throw new NoSuccessException(e1);
                    }
                default:
                    throw new NoSuccessException(e);
            }
        }
    }

    protected NoSuccessException rethrowException(Exception exception) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            throw rethrowExceptionFull(exception);
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        }
    }

    private NoSuccessException rethrowExceptionFull(Exception exception) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        try {
            throw exception;
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (BadParameterException e) {throw e;}
        catch (DoesNotExistException e) {throw e;}
        catch (AlreadyExistsException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (IllegalArgumentException e) {
            throw new BadParameterException(e);
        } catch (IOException e) {
            throw new TimeoutException(e);
        } catch (ServerException e) {
            switch(e.getCode()) {
                case ServerException.SERVER_REFUSED:
                    try {
                        throw e.getRootCause();
                    } catch (UnexpectedReplyCodeException unexpectedReplyCode) {
                        switch(unexpectedReplyCode.getReply().getCode()) {
                            case 112:
                                throw new TimeoutException(e);
                            case 500:
                            case 521:
                            case 550:
                                this.rethrowParsedException(unexpectedReplyCode);
                            default:
                                throw new NoSuccessException(e);
                        }
                    } catch (Exception e1) {
                        throw new PermissionDeniedException(e1);
                    }
                case ServerException.REPLY_TIMEOUT:             throw new TimeoutException(e);
                default:                                        throw new NoSuccessException(e);
            }
        } catch (ClientException e) {
            switch(e.getCode()) {
                case ClientException.NOT_AUTHORIZED:            throw new PermissionDeniedException(e);
                case ClientException.REPLY_TIMEOUT:             throw new TimeoutException(e);
                default:                                        throw new NoSuccessException(e);
            }
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }
    protected abstract void rethrowParsedException(UnexpectedReplyCodeException e) throws DoesNotExistException, AlreadyExistsException, PermissionDeniedException, NoSuccessException;
}
