package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.URL;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Exception;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SagaDataAdaptor implements FileReader, FileWriter, DataCopy, DataRename {
    private NSDirectory m_root;

    public String[] getSchemeAliases() {return null;}
    public Usage getUsage() {return null;}
    public Default[] getDefaults(Map attributes) throws IncorrectState {return null;}
    public Class[] getSupportedSecurityAdaptorClasses() {return new Class[]{InMemoryProxySecurityAdaptor.class};}
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {}
    public int getDefaultPort() {return 0;}

    public SagaDataAdaptor(URI url, GSSCredential cred) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            Context context = ContextFactory.createContext();
            context.setAttribute("Type", "GSSCredential");
            context.setAttribute("Indice", "0");
            context.setAttribute("UserProxy", InMemoryProxySecurityAdaptor.toBase64(cred));
            Session session = SessionFactory.createSession(false);
            session.addContext(context);
            URL rootUrl = new URL(url.toString());
            rootUrl.setFragment("#GSSCredential[0]");
            m_root = NSFactory.createNSDirectory(session, rootUrl, Flags.NONE.getValue());
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (AuthenticationFailed e) {
            throw new NoSuccess(e);
        } catch (AuthorizationFailed e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (AlreadyExists e) {
            throw new NoSuccess(e);
        }
    }

    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // do nothing
    }

    public void disconnect() throws NoSuccess {
        try {
            m_root.close();
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

    public boolean exists(String absolutePath) throws PermissionDenied, Timeout, NoSuccess {
        NSEntry entry = this.getEntry(absolutePath);
        try {
            return entry.isDir() || entry.isEntry() || entry.isLink();
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (AuthenticationFailed e) {
            throw new NoSuccess(e);
        } catch (AuthorizationFailed e) {
            throw new NoSuccess(e);
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            return this.getEntry(absolutePath).isDir();
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (AuthenticationFailed e) {
            throw new NoSuccess(e);
        } catch (AuthorizationFailed e) {
            throw new NoSuccess(e);
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

    public boolean isEntry(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            return this.getEntry(absolutePath).isEntry();
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (AuthenticationFailed e) {
            throw new NoSuccess(e);
        } catch (AuthorizationFailed e) {
            throw new NoSuccess(e);
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

    public long getSize(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        NSEntry entry = this.getEntry(absolutePath);
        if (entry instanceof File) {
            try {
                return ((File) entry).getSize();
            } catch (NotImplemented e) {
                throw new NoSuccess(e);
            } catch (AuthenticationFailed e) {
                throw new NoSuccess(e);
            } catch (AuthorizationFailed e) {
                throw new NoSuccess(e);
            } catch (IncorrectState e) {
                throw new NoSuccess(e);
            }
        } else {
            throw new NoSuccess("Tranfer URL is not a file");
        }
    }

    public InputStream getInputStream(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        NSEntry entry = this.getEntry(absolutePath);
        if (entry instanceof File) {
            return new SagaInputStream((File) entry);
        } else {
            throw new NoSuccess("Tranfer URL is not a file");
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        NSEntry entry;
        try {
            int flags = (exclusive ? Flags.EXCL : Flags.NONE).or(append ? Flags.APPEND : Flags.NONE);
            entry = m_root.open(
                    new URL(parentAbsolutePath+"/"+fileName),
                    flags);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        if (entry instanceof File) {
            return new SagaOutputStream((File) entry);
        } else {
            throw new NoSuccess("Tranfer URL is not a file");
        }
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        try {
            String protocol = m_root.getURL().getScheme();
            URL targetUrl = new URL(getURLString(protocol, targetHost, targetPort, targetAbsolutePath));
            int flags = (overwrite ? Flags.OVERWRITE : Flags.NONE).getValue();
            this.getEntry(sourceAbsolutePath).copy(
                    targetUrl,
                    flags);
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        URL sourceUrl;
        try {
            String protocol = m_root.getURL().getScheme();
            sourceUrl = new URL(getURLString(protocol, sourceHost, sourcePort, sourceAbsolutePath));
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
        NSEntry entry = this.getEntry(targetAbsolutePath);
        try {
            Method m = entry.getClass().getMethod("copyFrom", new Class[]{URL.class, int.class});
            if (m != null) {
                m.invoke(entry, new Object[]{sourceUrl, (overwrite ? Flags.OVERWRITE : Flags.NONE)});
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuccess(e);
        } catch (IllegalAccessException e) {
            throw new NoSuccess(e);
        } catch (InvocationTargetException e) {
            throw new NoSuccess(e);
        }
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite) throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        try {
            int flags = (overwrite ? Flags.OVERWRITE : Flags.NONE).getValue();
            this.getEntry(sourceAbsolutePath).move(
                    new URL(targetAbsolutePath),
                    flags);
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (AuthenticationFailed e) {
            throw new NoSuccess(e);
        } catch (AuthorizationFailed e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            this.getEntry(parentAbsolutePath+"/"+fileName).remove(Flags.NONE.getValue());
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (AuthorizationFailed e) {
            throw new NoSuccess(e);
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (AuthenticationFailed e) {
            throw new NoSuccess(e);
        }
    }

    private NSEntry getEntry(String absolutePath) throws NoSuccess {
        try {
            return m_root.open(new URL(absolutePath), Flags.NONE.getValue());
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    private static String getURLString(String scheme, String host, int port, String path) throws IncorrectURL {
        try {
            return new java.net.URI(scheme, null, host, port, path, null, "#GSSCredential[0]").toString();
        } catch (URISyntaxException e) {
            throw new IncorrectURL(e);
        }
    }
}
