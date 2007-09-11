package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.*;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.security.GlobusSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.globus.common.ChainedIOException;
import org.globus.ftp.*;
import org.globus.ftp.exception.*;
import org.globus.gsi.gssapi.GlobusGSSException;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

import java.io.*;
import java.lang.Exception;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpDataAdaptor implements FileReader, FileWriter, DirectoryReader, DirectoryWriter, DataCopy, DataRename, DataGet, DataPut {
    private static final String PROTECTION = "Protection";
    protected GSSCredential m_credential;
    protected int m_protection = GridFTPSession.PROTECTION_CLEAR;
    protected GridFTPClient m_client;

    public Usage getUsage() {
        return new UOptional(PROTECTION);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return new Default[]{new Default(PROTECTION, "none")};
    }

    public void setAttributes(Map attributes) throws BadParameter {
        if (attributes.containsKey(PROTECTION)) {
            String value = ((String) attributes.get(PROTECTION)).toLowerCase();
            if (value.equalsIgnoreCase("none")) {
                m_protection = GridFTPSession.PROTECTION_CLEAR;
            } else if (value.equalsIgnoreCase("integrity")) {
                m_protection = GridFTPSession.PROTECTION_SAFE;
            } else if (value.equalsIgnoreCase("confidentiality")) {
                m_protection = GridFTPSession.PROTECTION_CONFIDENTIAL;
            } else if (value.equalsIgnoreCase("privacy")) {
                m_protection = GridFTPSession.PROTECTION_PRIVATE;
            } else {
                throw new BadParameter("Attribute '"+PROTECTION+"' has unexpected value: "+value);
            }
        }
    }

    public String[] getSupportedContextTypes() {
        return new String[]{"Globus", "MyProxy", "VOMS"};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_credential = ((GlobusSecurityAdaptor) securityAdaptor).getGSSCredential();
    }

    public String getScheme() {
        return "gsiftp";
    }

    public String[] getSchemeAliases() {
        return new String[]{"gridftp"};
    }

    public int getDefaultPort() {
        return 2811;
    }

    //workaround
    private URI m_uri;
    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        try {
            m_uri = new URI("gsiftp", userInfo, host, port, "/", null, null);
        } catch (URISyntaxException e) {
            throw new NoSuccess(e);
        }

        try {
            m_client = new GridFTPClient(host, port);
            m_client.setAuthorization(HostAuthorization.getInstance());
            m_client.authenticate(m_credential);
            if (m_protection != GridFTPSession.PROTECTION_CLEAR) {
                m_client.setDataChannelProtection(m_protection);
                m_client.setProtectionBufferSize(16384);
            }
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

    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            MlsxEntry entry = m_client.mlst(absolutePath);
            return entry.get("type").endsWith("dir");
        } catch (Exception e) {
            try {
                throw rethrowException(e);
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
        if (overwrite) {
            //workaround: file is not overwritten when using GridFTPClient directly
            UrlCopy copyMgr = new UrlCopy();
            copyMgr.setUseThirdPartyCopy(true);
            copyMgr.setSourceCredentials(m_credential);
            copyMgr.setDestinationCredentials(m_credential);
            try {
                URI sourceUri = m_uri;
                URI targetUri = new URI("gsiftp", null, targetHost, targetPort, "/", null, null);
                // do not use URI.resolve() because UrlCopy requires double-slash for absolute paths
                copyMgr.setSourceUrl(new GlobusURL(sourceUri.toString()+"/"+sourceAbsolutePath));
                copyMgr.setDestinationUrl(new GlobusURL(targetUri.toString()+"/"+targetAbsolutePath));
                copyMgr.copy();
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
            return; //==============================> EXIT
        }

        // connect to peer server
        GsiftpDataAdaptor targetAdaptor = new GsiftpDataAdaptor();
        targetAdaptor.m_credential = m_credential;
        targetAdaptor.m_protection = m_protection;
        targetAdaptor.connect(null, targetHost, targetPort);

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
        if (overwrite) {
            //workaround: file is not overwritten when using GridFTPClient directly
            UrlCopy copyMgr = new UrlCopy();
            copyMgr.setUseThirdPartyCopy(true);
            copyMgr.setSourceCredentials(m_credential);
            copyMgr.setDestinationCredentials(m_credential);
            try {
                URI sourceUri = new URI("gsiftp", null, sourceHost, sourcePort, "/", null, null);
                URI targetUri = m_uri;
                // do not use URI.resolve() because UrlCopy requires double-slash for absolute paths
                copyMgr.setSourceUrl(new GlobusURL(sourceUri.toString()+"/"+sourceAbsolutePath));
                copyMgr.setDestinationUrl(new GlobusURL(targetUri.toString()+"/"+targetAbsolutePath));
                copyMgr.copy();
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
            return; //==============================> EXIT
        }

        // connect to peer server
        GsiftpDataAdaptor sourceAdaptor = new GsiftpDataAdaptor();
        sourceAdaptor.m_credential = m_credential;
        sourceAdaptor.m_protection = m_protection;
        sourceAdaptor.connect(null, sourceHost, sourcePort);

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

    public FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Vector v;
        try {
            m_client.setMode(GridFTPSession.MODE_STREAM);
            m_client.setPassiveMode(true);
            v = m_client.mlsd(absolutePath);
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (BadParameter badParameter) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
        List files = new ArrayList();
        for (int i=0; i<v.size(); i++) {
            MlsxEntry entry = (MlsxEntry) v.get(i);
            try {
                files.add(new GsiftpFileAttributes(entry));
            } catch(DoesNotExist e) {
                // ignore this entry: ., .. or null
            }
        }
        return (FileAttributes[]) files.toArray(new FileAttributes[files.size()]);
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
