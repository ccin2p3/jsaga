package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.io.InputStream;
import java.io.OutputStream;
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
public class SagaDataAdaptor implements FileReaderStreamFactory, FileWriterStreamFactory, DataCopy, DataRename {
    private Session m_session;
    private URL m_rootUrl;

    public String getType() {return null;}
    public Usage getUsage() {return null;}
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {return null;}
    public Class[] getSupportedSecurityAdaptorClasses() {return new Class[]{InMemoryProxySecurityAdaptor.class};}
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {}
    public BaseURL getBaseURL() throws IncorrectURLException {return null;}

    public SagaDataAdaptor(URI url, GSSCredential cred) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            Context context = ContextFactory.createContext();
            context.setAttribute("Type", "InMemoryProxy");
            context.setAttribute("UserProxy", InMemoryProxySecurityAdaptor.toBase64(cred));
            m_session = SessionFactory.createSession(false);
            m_session.addContext(context);
            m_rootUrl = URLFactory.createURL(url.resolve(".").toString());
            m_rootUrl.setFragment("InMemoryProxy");
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // do nothing
    }

    public void disconnect() throws NoSuccessException {
        // do nothing
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        NSEntry entry = this.getEntry(absolutePath);
        try {
            return entry.isDir() || entry.isEntry() || entry.isLink();
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        NSEntry entry;
        try {
            URL url = this.toURL(absolutePath);
            int flags = Flags.READ.getValue();
            entry = NSFactory.createNSEntry(m_session, url, flags);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        if (entry instanceof File) {
            return new SagaInputStream((File) entry);
        } else {
            throw new NoSuccessException("Tranfer URL is not a file");
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        NSEntry entry;
        try {
            URL url = this.toURL(parentAbsolutePath+"/"+fileName);
            int flags = (exclusive ? Flags.EXCL : Flags.NONE).or(append ? Flags.APPEND : Flags.NONE);
            entry = NSFactory.createNSEntry(m_session, url, flags);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        if (entry instanceof File) {
            return new SagaOutputStream((File) entry);
        } else {
            throw new NoSuccessException("Tranfer URL is not a file");
        }
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        try {
            String protocol = m_rootUrl.getScheme();
            URL targetUrl = URLFactory.createURL(getURLString(protocol, targetHost, targetPort, targetAbsolutePath));
            int flags = (overwrite ? Flags.OVERWRITE : Flags.NONE).getValue();
            this.getEntry(sourceAbsolutePath).copy(
                    targetUrl,
                    flags);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL sourceUrl;
        try {
            String protocol = m_rootUrl.getScheme();
            sourceUrl = URLFactory.createURL(getURLString(protocol, sourceHost, sourcePort, sourceAbsolutePath));
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
        NSEntry entry = this.getEntry(targetAbsolutePath);
        try {
            Method m = entry.getClass().getMethod("copyFrom", new Class[]{URL.class, int.class});
            if (m != null) {
                m.invoke(entry, new Object[]{sourceUrl, (overwrite ? Flags.OVERWRITE : Flags.NONE)});
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuccessException(e);
        } catch (IllegalAccessException e) {
            throw new NoSuccessException(e);
        } catch (InvocationTargetException e) {
            throw new NoSuccessException(e);
        }
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        try {
            int flags = (overwrite ? Flags.OVERWRITE : Flags.NONE).getValue();
            this.getEntry(sourceAbsolutePath).move(
                    URLFactory.createURL(targetAbsolutePath),
                    flags);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            this.getEntry(parentAbsolutePath+"/"+fileName).remove(Flags.NONE.getValue());
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NoSuccessException("[INTERNAL ERROR] This method is not supposed to be used");
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NoSuccessException("[INTERNAL ERROR] This method is not supposed to be used");
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        throw new NoSuccessException("[INTERNAL ERROR] This method is not supposed to be used");
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NoSuccessException("[INTERNAL ERROR] This method is not supposed to be used");
    }

    private NSEntry getEntry(String absolutePath) throws NoSuccessException {
        try {
            URL url = this.toURL(absolutePath);
            int flags = Flags.NONE.getValue();
            return NSFactory.createNSEntry(m_session, url, flags);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    private URL toURL(String absolutePath) throws NotImplementedException, BadParameterException, NoSuccessException {
        URL url = m_rootUrl.resolve(URLFactory.createURL(absolutePath));
        url.setFragment(m_rootUrl.getFragment());
        return url;
    }

    private static String getURLString(String scheme, String host, int port, String path) throws IncorrectURLException {
        try {
            return new java.net.URI(scheme, null, host, port, path, null, "InMemoryProxy[0]").toString();
        } catch (URISyntaxException e) {
            throw new IncorrectURLException(e);
        }
    }
}
