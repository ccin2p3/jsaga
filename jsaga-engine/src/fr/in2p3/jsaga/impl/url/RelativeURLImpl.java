package fr.in2p3.jsaga.impl.url;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.helpers.URLEncoder;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.IOException;
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
public class RelativeURLImpl extends AbstractURLImpl implements URL {

	protected UniversalFile m_file;
	protected String url_query;
	protected String url_fragment;
	
	RelativeURLImpl(String url) throws BadParameterException {
		// url is considered as path only (even if contains ? and #)
		setPath(url);
	}

    /** Encode the relative path + set the cache */
	RelativeURLImpl(FileAttributes cache) throws BadParameterException {
        this(cache.getRelativePath());
        this.setCache(cache);
    }

    public SagaObject clone() throws CloneNotSupportedException {
    	RelativeURLImpl clone = (RelativeURLImpl) super.clone();
        clone.m_file = m_file;
        clone.url_query = url_query;
        clone.url_fragment = url_fragment;
        clone.m_cache = m_cache;
        clone.m_cache_creation_time = m_cache_creation_time;
        return clone;
    }

    public String getEncodedPathOnly() {
    	String encoded_path = URLEncoder.encodePathOnly(getPath());
		return encoded_path + (url_query == null?"":"?"+url_query) + (url_fragment == null?"":"#"+url_fragment);
    	
    }
    
    public URL resolve(URL url) throws NoSuccessException {
    	// if absolute: throw exception
    	if (url instanceof AbsoluteURLImpl) {
    		throw new NoSuccessException("The URL cannot be resolved against this relative URL");
    	}
    	RelativeURLImpl rel_url = (RelativeURLImpl)url;
    	// If url is absolute file, return url
    	if (rel_url.m_file.isAbsolute()) {
    		return url;
    	}
    	String new_url;
    	// path or parent path
    	new_url = m_file.isDirectory()?m_file.getPath():m_file.getParent();
    	// add new path
    	new_url += rel_url.m_file.getPath();
    	// add new query or current query
    	if (rel_url.url_query != null) {
    		new_url += "?" + rel_url.url_query;
        	if (rel_url.url_fragment != null) {
        		new_url += "#" + rel_url.url_fragment;
        	}
    	} else {
    		if (url_query != null) {
    			new_url += "?" + url_query;
    		}
        	if (rel_url.url_fragment != null) {
        		new_url += "#" + rel_url.url_fragment;
        	} else if (url_fragment != null) {
        		new_url += "#" + url_fragment;
        	}
    		
    	}
    	// return resolved URL
    	try {
			return new RelativeURLImpl(new_url);
		} catch (BadParameterException e) {
			throw new NoSuccessException(e);
		}
    }

	public void setString(String url) throws BadParameterException {
		// url is considered as path only (even if contains ? and #)
		this.setPath(url);
		url_query = url_fragment = null;
	}

	public String getString() {
		return getPath() + (url_query == null?"":"?"+url_query) + (url_fragment == null?"":"#"+url_fragment);
	}

	public String getEscaped() {
		return null;
	}

    public String toString() {
        return this.normalize().getString();
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

	public String getHost() {
		return null;
	}

	public void setHost(String host) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
	}

	public String getPath() {
		return m_file.getPath();
	}

	public void setPath(String path) throws BadParameterException {
		if (path != null) {
			if (Pattern.matches(AbsoluteURLImpl.ABSOLUTE_URL_REGEXP, path)) {
				throw new BadParameterException("path must be relative");
			}
		} else {
			path = "";
		}
		m_file = new UniversalFile(path);
	}

	public int getPort() {
		return -1;
	}

	public void setPort(int port) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
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

	public String getScheme() {
		return null;
	}

	public void setScheme(String scheme) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
	}

	public String getUserInfo() {
		return null;
	}

	public void setUserInfo(String userInfo) throws BadParameterException {
      	throw new BadParameterException("Operation not supported");
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
		try {
			String canon = m_file.getCanonicalPath();
			RelativeURLImpl newURL = new RelativeURLImpl(canon);
			newURL.setQuery(url_query);
			newURL.setFragment(url_fragment);
			return newURL;
		} catch (BadParameterException e) {
            throw new RuntimeException(e);
		}
	}


}
