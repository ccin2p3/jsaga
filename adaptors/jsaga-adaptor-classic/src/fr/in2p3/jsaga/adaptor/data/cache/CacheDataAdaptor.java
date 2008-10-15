package fr.in2p3.jsaga.adaptor.data.cache;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.namespace.NSEntry;

import java.io.InputStream;
import java.lang.Exception;
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

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        String tmp = System.getProperty("java.io.tmpdir");
        if(System.getProperty("os.name").startsWith("Windows")) tmp = "/"+tmp.replaceAll("\\\\","/");
        return new Default[] {
                new Default(BASE_URL, "file://"+tmp+"/cache"),
                new Default(AUTO_REFRESH, "false")};
    }

    public Class[] getSupportedSecurityAdaptorClasses() {return null;}
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {}
    public BaseURL getBaseURL() throws IncorrectURL {return null;}

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        m_baseURL = new URL((String) attributes.get(BASE_URL));
        URL remoteUrl;
        try {
            String protocol = "http";   //todo: read protocol from attributes
            remoteUrl = new URL(new URI(protocol, userInfo, host, port, basePath, null, null).toString());
        }
        catch (URISyntaxException e) {throw new BadParameter(e);}
        try {
            m_cache = FileFactory.createDirectory(m_baseURL);
            m_connection = FileFactory.createDirectory(remoteUrl);
        }
        catch (IncorrectURL e) {throw new BadParameter(e);}
        catch (PermissionDenied e) {throw new AuthorizationFailed(e);}
        catch (DoesNotExist e) {throw new NoSuccess(e);}
        catch (AlreadyExists e) {throw new NoSuccess(e);}
    }

    public void disconnect() throws NoSuccess {
        try {
            m_connection.close();
            m_cache.close();
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {
        try {
            URL remoteURL = getURL(absolutePath, additionalArgs);
            m_connection.open(remoteURL);
            return true;
        } catch (DoesNotExist e) {
            return false;
        }
        catch (PermissionDenied e) {throw e;}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            URL remoteURL = getURL(absolutePath, additionalArgs);
            return m_connection.open(remoteURL).isDir();
        }
        catch (PermissionDenied e) {throw e;}
        catch (DoesNotExist e) {throw e;}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            URL remoteURL = getURL(absolutePath, additionalArgs);
            NSEntry entry = m_connection.open(remoteURL);
            return new CacheFileAttributes(entry);
        }
        catch (PermissionDenied e) {throw e;}
        catch (DoesNotExist e) {throw e;}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
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
        catch (PermissionDenied e) {throw e;}
        catch (DoesNotExist e) {throw e;}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        URL remoteURL = getURL(absolutePath, additionalArgs);
        URL cacheURL = this.getCacheURL(absolutePath, additionalArgs);
        try {
            if (!m_cache.exists(cacheURL)) {
                m_connection.copy(remoteURL, cacheURL);
            }
            return m_cache.openFileInputStream(cacheURL);
        }
        catch (PermissionDenied e) {throw e;}
        catch (BadParameter e) {throw e;}
        catch (DoesNotExist e) {throw e;}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
        URL cacheURL = this.getCacheURL(parentAbsolutePath+"/"+directoryName, additionalArgs);
        try {
            m_cache.makeDir(cacheURL);
        }
        catch (PermissionDenied e) {throw e;}
        catch (BadParameter e) {throw e;}
        catch (DoesNotExist e) {throw new ParentDoesNotExist(e);}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        URL cacheURL = this.getCacheURL(parentAbsolutePath+"/"+directoryName, additionalArgs);
        try {
            m_cache.remove(cacheURL);
        }
        catch (PermissionDenied e) {throw e;}
        catch (BadParameter e) {throw e;}
        catch (DoesNotExist e) {throw e;}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        URL cacheURL = this.getCacheURL(parentAbsolutePath+"/"+fileName, additionalArgs);
        try {
            m_cache.remove(cacheURL);
        }
        catch (PermissionDenied e) {throw e;}
        catch (BadParameter e) {throw e;}
        catch (DoesNotExist e) {throw e;}
        catch (Timeout e) {throw e;}
        catch (NoSuccess e) {throw e;}
        catch (Exception e) {throw new NoSuccess(e);}
    }

    ////////////////////////////////////////// private methods /////////////////////////////////////////

    private URL getCacheURL(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, Timeout, NoSuccess {
        return m_baseURL.resolve(getURL(absolutePath, additionalArgs));
    }

    private static URL getURL(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, Timeout, NoSuccess {
        try {
            return (additionalArgs!=null ? new URL(absolutePath+"?"+additionalArgs) : new URL(absolutePath));
        } catch (NotImplemented e) {
            throw new NoSuccess(e);
        }
    }
}
