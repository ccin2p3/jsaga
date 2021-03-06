package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyMonitor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
    private static Logger s_logger = Logger.getLogger(SagaDataAdaptor.class);

    private Session m_session;
    private URL m_rootUrl;

    // for releasing SRM file
    private String m_token;
    private String m_srmPath;
    private StreamCallback m_callback;

    public String getType() {return null;}
    public Usage getUsage() {return null;}
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {return null;}
    public Class[] getSupportedSecurityCredentialClasses() {return new Class[]{InMemoryProxySecurityCredential.class};}
    public void setSecurityCredential(SecurityCredential credential) {}
    public int getDefaultPort() {return NO_PORT;}

    public SagaDataAdaptor(URI url, GSSCredential cred, java.io.File certRepository, String token, String srmPath, StreamCallback callback) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            Context context = ContextFactory.createContext(JSAGA_FACTORY, "InMemoryProxy");
            context.setAttribute(Context.USERPROXY, InMemoryProxySecurityCredential.toBase64(cred));
            context.setAttribute(Context.CERTREPOSITORY, certRepository.getAbsolutePath());
            m_session = SessionFactory.createSession(JSAGA_FACTORY, false);
            m_session.addContext(context);
            m_rootUrl = URLFactory.createURL(JSAGA_FACTORY, url.resolve(".").toString());
            m_rootUrl.setScheme(context.getAttribute("UrlPrefix") + "-" + m_rootUrl.getScheme());

            // for releasing SRM file
            m_token = token;
            m_srmPath = srmPath;
            m_callback = callback;
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
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } finally {
            closeEntry(entry);
        }
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        NSEntry entry;
        try {
            URL url = this.toURL(absolutePath);
            int flags = Flags.READ.getValue();
            entry = NSFactory.createNSEntry(JSAGA_FACTORY, m_session, url, flags);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        if (entry instanceof File) {
            return new SagaInputStream((File) entry, m_token, m_srmPath, m_callback);
        } else {
            closeEntry(entry);
            throw new NoSuccessException("Tranfer URL is not a file");
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
    	NSEntry entry;
        try {
            URL url = this.toURL(parentAbsolutePath+"/"+fileName);
            int flags = Flags.CREATE
                    .or((exclusive ? Flags.EXCL : Flags.NONE)
                    .or(append ? Flags.APPEND : Flags.NONE));
            entry = NSFactory.createNSEntry(JSAGA_FACTORY, m_session, url, flags);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        if (entry instanceof File) {
            return new SagaOutputStream((File) entry, m_token, m_srmPath, m_callback);
        } else {
            closeEntry(entry);
            throw new NoSuccessException("Tranfer URL is not a file");
        }
    }

    public void copy(String sourceAbsolutePath, String targetHost, int targetPort, String targetAbsolutePath, boolean overwrite, String additionalArgs, DataCopyMonitor progressMonitor) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        URL targetUrl;
        try {
            String protocol = m_rootUrl.getScheme();
            targetUrl = URLFactory.createURL(JSAGA_FACTORY, getURLString(protocol, targetHost, targetPort, targetAbsolutePath));
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
        NSEntry entry = this.getEntry(sourceAbsolutePath);
        try {
            int flags = (overwrite ? Flags.OVERWRITE : Flags.NONE).getValue();
            entry.copy(targetUrl, flags);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        } finally {
            closeEntry(entry);
        }
    }

    public void copyFrom(String sourceHost, int sourcePort, String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL sourceUrl;
        try {
            String protocol = m_rootUrl.getScheme();
            sourceUrl = URLFactory.createURL(JSAGA_FACTORY, getURLString(protocol, sourceHost, sourcePort, sourceAbsolutePath));
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
        } finally {
            closeEntry(entry);
        }
    }

    public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        NSEntry entry = this.getEntry(sourceAbsolutePath);
        try {
            int flags = (overwrite ? Flags.OVERWRITE : Flags.NONE).getValue();
            entry.move(
                    URLFactory.createURL(JSAGA_FACTORY, targetAbsolutePath),
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
        } finally {
            closeEntry(entry);
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        NSEntry entry = this.getEntry(parentAbsolutePath+"/"+fileName);
        try {
            entry.remove(Flags.NONE.getValue());
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        } finally {
            closeEntry(entry);
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
            return NSFactory.createNSEntry(JSAGA_FACTORY, m_session, url, flags);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }
    private void closeEntry(NSEntry entry) {
        if (entry != null) {
            try {
                entry.close();
            } catch (SagaException e) {
                s_logger.warn("Failed to close entry");
            }
        }
    }

    private URL toURL(String absolutePath) throws NotImplementedException, BadParameterException, NoSuccessException {
        URL url = m_rootUrl.resolve(URLFactory.createURL(JSAGA_FACTORY, absolutePath));
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
