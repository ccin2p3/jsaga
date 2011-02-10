package fr.in2p3.jsaga.impl.url;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   RelativeURLImpl
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 fév 2011
* ***************************************************
* Description:                                      */
/**
 *
 */
public class RelativeURLImpl extends AbstractSagaObjectImpl implements URL {

	protected String url_path;
	protected String url_query;
	protected String url_fragment;
	
    protected FileAttributes m_cache;
	
	RelativeURLImpl(String url) throws BadParameterException {
		// url is considered as path only (even if contains ? and #)
		setPath(url);
	}

    /** Encode the relative path + set the cache */
	RelativeURLImpl(FileAttributes cache) throws BadParameterException {
        this(cache.getRelativePath());
        m_cache = cache;
    }

    public SagaObject clone() throws CloneNotSupportedException {
    	RelativeURLImpl clone = (RelativeURLImpl) super.clone();
        clone.url_path = url_path;
        clone.url_query = url_query;
        clone.url_fragment = url_fragment;
        clone.m_cache = m_cache;
        return clone;
    }

    public URL resolve(URL url) throws NoSuccessException {
    	//TODO: implement Relative
    	// if absolute: throw exception
      	throw new NoSuccessException("The URL cannot be resolved against this relative URL");
    }

	public void setString(String url) throws BadParameterException {
		this.setPath(url);
		url_query = url_fragment = null;
	}

	public void setString() throws BadParameterException {
		this.setString(null);
	}

	public String getString() {
		return url_path + (url_query == null?"":"?"+url_query) + (url_fragment == null?"":"#"+url_fragment);
	}

	public String getEscaped() {
      	// TODO
		return null;
	}

	public String getFragment() {
		if (url_fragment == null) {
			throw new RuntimeException("URL not resolved yet");
		}
		return url_fragment;
	}

	public void setFragment(String fragment) throws BadParameterException {
		url_fragment = fragment;
	}

	public void setFragment() throws BadParameterException {
		this.setFragment(null);
	}

	public String getHost() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setHost(String host) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
	}

	public void setHost() throws BadParameterException {
		this.setHost(null);
	}

	public String getPath() {
		return url_path;
	}

	public void setPath(String path) throws BadParameterException {
		if (path != null) {
			// convert '\' to '/'
			if (System.getProperty("file.separator") != "/")
				path = path.replace(System.getProperty("file.separator"), "/");
			if (Pattern.matches("^[^/]{2,}\\:.*", path)) {
				throw new BadParameterException("path must be relative");
			}
		}
		url_path = (path == null?"":path);
		// remove leading duplicated / 
        while (url_path.startsWith("//")) {
        	url_path = url_path.substring(1);
        }
	}

	public void setPath() throws BadParameterException {
		this.setPath(null);
	}

	public int getPort() {
		// TODO Auto-generated method stub
		return -1;
	}

	public void setPort(int port) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
	}

	public void setPort() throws BadParameterException {
		this.setPort(-1);
	}

	public String getQuery() {
		if (url_query == null) {
			throw new RuntimeException("URL not resolved yet");
		}
		return url_query;
	}

	public void setQuery(String query) throws BadParameterException {
		url_query = query;
	}

	public void setQuery() throws BadParameterException {
		this.setQuery(null);
	}

	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setScheme(String scheme) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
	}

	public void setScheme() throws BadParameterException {
		this.setScheme(null);
	}

	public String getUserInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setUserInfo(String userInfo) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
	}

	public void setUserInfo() throws BadParameterException {
		this.setUserInfo(null);
	}

	public URL translate(String scheme) throws BadParameterException,
			NoSuccessException {
      	throw new BadParameterException("Operation not supported");
	}

	public URL translate(Session session, String scheme)
			throws BadParameterException, NoSuccessException {
		return this.translate(scheme);
	}

	public boolean isAbsolute() {
		return false;
	}

	public URL normalize() {
		return this;
	}
	
    ////////////////////////////////////////// cache methods //////////////////////////////////////////

    public void setCache(FileAttributes cache) {
        m_cache = cache;
    }

    public FileAttributes getCache() {
        return m_cache;
    }

    public boolean hasCache() {
        return (m_cache != null);
    }


}
