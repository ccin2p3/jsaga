package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.*;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
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
import java.lang.Exception;
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
public abstract class GsiftpDataAdaptorAbstract implements FileReader, FileWriter, DirectoryReader, DirectoryWriter, DataCopy, DataRename, DataGet, DataPut {
    protected GSSCredential m_credential;
    protected GridFTPClient m_client;

    public abstract String[] getSchemeAliases();
    public abstract Usage getUsage();
    public abstract Default[] getDefaults(Map attributes) throws IncorrectState;
    public abstract boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess;
    public abstract FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_credential = ((GSSCredentialSecurityAdaptor) securityAdaptor).getGSSCredential();
    }

    public int getDefaultPort() {
        return 2811;
    }

    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        try {
            m_client = new GridFTPClient(host, port);
            m_client.setAuthorization(HostAuthorization.getInstance());
            m_client.authenticate(m_credential);
        } catch (ChainedIOException e) {
            try {
                throw e.getException();
            } catch (GlobusGSSException gssException) {
                throw new AuthenticationFailed(gssException);
            } catch (Throwable throwable) {
                throw new Timeout(throwable);
            }
        } catch (IOException e) {
            if (e.getMessage()!=null && e.getMessage().indexOf("Authentication Error") > -1) {
                throw new AuthenticationFailed(e);
            } else {
                throw new Timeout(e);
            }
        } catch (ServerException e) {
            switch(e.getCode()) {
                case ServerException.SERVER_REFUSED:
                    try {
                        throw e.getRootCause();
                    } catch (UnexpectedReplyCodeException unexpectedReplyCode) {
                        switch(unexpectedReplyCode.getReply().getCode()) {
                            case 530:
                                throw new AuthorizationFailed(unexpectedReplyCode);
                            default:
                                throw new NoSuccess(unexpectedReplyCode);
                        }
                    } catch (Exception e1) {
                        throw new NoSuccess(e1);
                    }
            }
        }
    }

    public void disconnect() throws NoSuccess {
        try {
            m_client.close();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    public boolean exists(String absolutePath) throws PermissionDenied, Timeout, NoSuccess {
        try {
            return m_client.exists(absolutePath);
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (DoesNotExist doesNotExist) {
                throw new NoSuccess(e);
            } catch (BadParameter badParameter) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
    }

    public boolean isEntry(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return !isDirectory(absolutePath);
    }

    public long getSize(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            return m_client.getSize(absolutePath);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    public InputStream getInputStream(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_client.setMode(GridFTPSession.MODE_STREAM); //MODE_EBLOCK induce error: "451 refusing to store with active mode"
            m_client.setPassive();
            m_client.setLocalActive();
        } catch (Exception e) {
            throw rethrowException(e);
        }
        return new GsiftpInputStream(m_client, absolutePath);
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        String absolutePath = parentAbsolutePath+fileName;
        if (exclusive && this.exists(absolutePath)) {
            // need to check existence explicitely, else exception is never thrown
            throw new AlreadyExists("File already exists");
        } else if (!this.exists(parentAbsolutePath)) {
            // need to check existence explicitely, else exception is thrown to late (when writing bytes)
            throw new DoesNotExist("Parent directory does not exist");
        }
        try {
            m_client.setType(GridFTPSession.TYPE_IMAGE);
            m_client.setMode(GridFTPSession.MODE_EBLOCK);
            m_client.setPassive();
            m_client.setLocalActive();
        } catch (Exception e) {
            throw rethrowExceptionFull(e);
        }
        return new GsiftpOutputStream(m_client, absolutePath, append);
    }

    /** does not work */
    public void getToStream(String absolutePath, OutputStream stream) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        final boolean autoFlush = false;
        final boolean ignoreOffset = true;
        try {
            m_client.get(
                    absolutePath,
                    new DataSinkStream(stream, autoFlush, ignoreOffset),
                    null);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    /** does not work */
    public void putFromStream(String absolutePath, InputStream stream, boolean append) throws PermissionDenied, BadParameter, AlreadyExists, Timeout, NoSuccess {
        final int DEFAULT_BUFFER_SIZE = 16384;
        try {
            m_client.put(
                absolutePath,
                new DataSourceStream(stream, DEFAULT_BUFFER_SIZE),
                    null,
                    append);
        } catch (Exception e) {
            try {
                throw rethrowExceptionFull(e);
            } catch (DoesNotExist e1) {
                throw new NoSuccess(e1);
            }
        }
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        // connect to peer server
        GsiftpDataAdaptorAbstract targetAdaptor = new Gsiftp1DataAdaptor();
        targetAdaptor.m_credential = m_credential;
        targetAdaptor.connect(null, targetHost, targetPort, null);

        //todo: remove this block when overwriting target file will work (it only works with UrlCopy)
        if (overwrite && targetAdaptor.exists(targetAbsolutePath)) {
            try {
                targetAdaptor.m_client.deleteFile(targetAbsolutePath);
            } catch (Exception e) {
                throw new PermissionDenied("Failed to overwrite target file", e);
            }
        }

        // need to check existence of target explicitely, else exception is never thrown
        if (!overwrite && targetAdaptor.exists(targetAbsolutePath)) {
            throw new AlreadyExists("File already exists");
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

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        // connect to peer server
        GsiftpDataAdaptorAbstract sourceAdaptor = new Gsiftp1DataAdaptor();
        sourceAdaptor.m_credential = m_credential;
        sourceAdaptor.connect(null, sourceHost, sourcePort, null);

        //todo: remove this block when overwriting target file will work (it only works with UrlCopy)
        if (overwrite && this.exists(targetAbsolutePath)) {
            try {
                m_client.deleteFile(targetAbsolutePath);
            } catch (Exception e) {
                throw new PermissionDenied("Failed to overwrite target file", e);
            }
        }

        // need to check existence of target explicitely, else exception is never thrown
        if (!overwrite && this.exists(targetAbsolutePath)) {
            throw new AlreadyExists("File already exists");
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

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        try {
            m_client.rename(sourceAbsolutePath, targetAbsolutePath);
        } catch (Exception e) {
            throw rethrowExceptionFull(e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            m_client.deleteFile(parentAbsolutePath+"/"+fileName);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    public void makeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        try {
            m_client.makeDir(parentAbsolutePath+"/"+directoryName);
        } catch (Exception e) {
            throw rethrowExceptionFull(e);
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            m_client.deleteDir(parentAbsolutePath+"/"+directoryName);
        } catch (Exception e) {
            throw rethrowException(e);
        }
    }

    protected static NoSuccess rethrowException(Exception exception) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            throw rethrowExceptionFull(exception);
        } catch (AlreadyExists e) {
            throw new NoSuccess(e);
        }
    }

    private static NoSuccess rethrowExceptionFull(Exception exception) throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        try {
            throw exception;
        } catch (IllegalArgumentException e) {
            throw new BadParameter(e);
        } catch (IOException e) {
            throw new Timeout(e);
        } catch (ServerException e) {
            switch(e.getCode()) {
                case ServerException.SERVER_REFUSED:
                    try {
                        throw e.getRootCause();
                    } catch (UnexpectedReplyCodeException unexpectedReplyCode) {
                        switch(unexpectedReplyCode.getReply().getCode()) {
                            case 112:
                                throw new Timeout(e);
                            case 500:
                                String msg = unexpectedReplyCode.getReply().getMessage();
                                if (msg.indexOf("No such") > -1) {
                                    throw new DoesNotExist(unexpectedReplyCode);
                                } else if (msg.indexOf("exists") > -1) {
                                    throw new AlreadyExists(unexpectedReplyCode);
                                } else if (msg.indexOf("Permission denied") > -1) {
                                    throw new PermissionDenied(e);
                                } else {
                                    throw new NoSuccess(e);
                                }
                            case 550:
                                msg = unexpectedReplyCode.getReply().getMessage();
                                if (msg.indexOf("No such") > -1) {
                                    throw new DoesNotExist(unexpectedReplyCode);
                                } else {
                                    throw new NoSuccess(e);
                                }
                            default:
                                throw new NoSuccess(e);
                        }
                    } catch (Exception e1) {
                        throw new PermissionDenied(e1);
                    }
                case ServerException.REPLY_TIMEOUT:             throw new Timeout(e);
                default:                                        throw new NoSuccess(e);
            }
        } catch (ClientException e) {
            switch(e.getCode()) {
                case ClientException.NOT_AUTHORIZED:            throw new PermissionDenied(e);
                case ClientException.REPLY_TIMEOUT:             throw new Timeout(e);
                default:                                        throw new NoSuccess(e);
            }
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
}
