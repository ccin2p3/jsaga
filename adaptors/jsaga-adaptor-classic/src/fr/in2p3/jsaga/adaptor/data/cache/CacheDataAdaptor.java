package fr.in2p3.jsaga.adaptor.data.cache;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.*;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CacheDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CacheDataAdaptor implements FileReaderStreamFactory, FileWriter {
    private static final String BASE_URL = "BaseUrl";
    private static final String AUTO_REFRESH = "AutoRefresh";

    private URL m_baseURL;
    private Directory m_cache;
    private Directory m_connection;

    public String getType() {
        return "cache";
    }

    public Usage getUsage() {
        return new UOr(new Usage[]{new U(BASE_URL), new U(AUTO_REFRESH)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        String tmp = System.getProperty("java.io.tmpdir");
        if(System.getProperty("os.name").startsWith("Windows")) tmp = "/"+tmp.replaceAll("\\\\","/");
        return new Default[] {
                new Default(BASE_URL, "file://"+tmp+"/cache"),
                new Default(AUTO_REFRESH, "false")};
    }

    public Class[] getSupportedSecurityCredentialClasses() {return null;}
    public void setSecurityCredential(SecurityCredential credential) {}
    public int getDefaultPort() {return NO_PORT;}

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_baseURL = URLFactory.createURL((String) attributes.get(BASE_URL));
        URL remoteUrl;
        try {
            String protocol = "http";   //todo: read protocol from attributes
            remoteUrl = URLFactory.createURL(new URI(protocol, userInfo, host, port, basePath, null, null).toString());
        }
        catch (URISyntaxException e) {throw new BadParameterException(e);}
        try {
            m_cache = FileFactory.createDirectory(m_baseURL);
            m_connection = FileFactory.createDirectory(remoteUrl);
        }
        catch (IncorrectURLException e) {throw new BadParameterException(e);}
        catch (PermissionDeniedException e) {throw new AuthorizationFailedException(e);}
        catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        catch (AlreadyExistsException e) {throw new NoSuccessException(e);}
    }

    public void disconnect() throws NoSuccessException {
        try {
            m_connection.close();
            m_cache.close();
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            URL remoteURL = getURL(absolutePath, additionalArgs);
            m_connection.open(remoteURL);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (Exception e) {throw new NoSuccessException(e);}
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            URL remoteURL = getURL(absolutePath, additionalArgs);
            NSEntry entry = m_connection.open(remoteURL);
            return new CacheFileAttributes(entry);
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (DoesNotExistException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (Exception e) {throw new NoSuccessException(e);}
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            URL remoteURL = getURL(absolutePath, additionalArgs);
            List list = m_connection.openDir(remoteURL).list();
            FileAttributes[] attrs = new FileAttributes[list.size()];
            for (int i=0; i<list.size(); i++) {
                URL url = (URL) list.get(i);
                NSEntry entry = m_connection.open(url);
                attrs[i] = new CacheFileAttributes(entry);
            }
            return attrs;
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (DoesNotExistException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (Exception e) {throw new NoSuccessException(e);}
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL remoteURL = getURL(absolutePath, additionalArgs);
        URL cacheURL = this.getCacheURL(absolutePath, additionalArgs);
        try {
            if (!m_cache.exists(cacheURL)) {
                m_connection.copy(remoteURL, cacheURL);
            }
            return m_cache.openFileInputStream(cacheURL);
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (BadParameterException e) {throw e;}
        catch (DoesNotExistException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (Exception e) {throw new NoSuccessException(e);}
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        URL cacheURL = this.getCacheURL(parentAbsolutePath+"/"+directoryName, additionalArgs);
        try {
            m_cache.makeDir(cacheURL);
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (BadParameterException e) {throw e;}
        catch (DoesNotExistException e) {throw new ParentDoesNotExist(e);}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (Exception e) {throw new NoSuccessException(e);}
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL cacheURL = this.getCacheURL(parentAbsolutePath+"/"+directoryName, additionalArgs);
        try {
            m_cache.remove(cacheURL);
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (BadParameterException e) {throw e;}
        catch (DoesNotExistException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (Exception e) {throw new NoSuccessException(e);}
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        URL cacheURL = this.getCacheURL(parentAbsolutePath+"/"+fileName, additionalArgs);
        try {
            m_cache.remove(cacheURL);
        }
        catch (PermissionDeniedException e) {throw e;}
        catch (BadParameterException e) {throw e;}
        catch (DoesNotExistException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (Exception e) {throw new NoSuccessException(e);}
    }

    ////////////////////////////////////////// private methods /////////////////////////////////////////

    private URL getCacheURL(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return m_baseURL.resolve(getURL(absolutePath, additionalArgs));
    }

    private static URL getURL(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            return URLFactory.createURL(additionalArgs!=null
                    ? absolutePath+"?"+additionalArgs
                    : absolutePath);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        }
    }
}
