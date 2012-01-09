package fr.in2p3.jsaga.impl.url;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
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
public abstract class AbstractURLImpl extends AbstractSagaObjectImpl implements URL {

    protected FileAttributes m_cache;
    protected long m_cache_creation_time;
	
	AbstractURLImpl() throws BadParameterException {
	}

    public SagaObject clone() throws CloneNotSupportedException {
    	AbstractURLImpl clone = (AbstractURLImpl) super.clone();
        clone.m_cache = m_cache;
        clone.m_cache_creation_time = m_cache_creation_time;
        return clone;
    }

    /** Encode the relative path + set the cache */
	/*AbstractURLImpl(FileAttributes cache) throws BadParameterException {
        this(cache.getRelativePath());
        m_cache = cache;
    }*/

    public abstract URL resolve(URL url) throws NoSuccessException ;

	public abstract void setString(String url) throws BadParameterException;

	public void setString() throws BadParameterException {
		this.setString(null);
	}

	public abstract String getString() ;

	public abstract String getEscaped() ;

	public abstract String getFragment() ;

	public abstract void setFragment(String fragment) throws BadParameterException ;

	public void setFragment() throws BadParameterException {
		this.setFragment(null);
	}

	public abstract String getHost() ;

	public abstract void setHost(String host) throws BadParameterException ;

	public void setHost() throws BadParameterException {
		this.setHost(null);
	}

	public abstract String getPath() ;

	public abstract void setPath(String path) throws BadParameterException ;

	public void setPath() throws BadParameterException {
		this.setPath(null);
	}

	public abstract int getPort() ;

	public abstract void setPort(int port) throws BadParameterException ;

	public void setPort() throws BadParameterException {
		this.setPort(-1);
	}

	public abstract String getQuery() ;

	public abstract void setQuery(String query) throws BadParameterException ;

	public void setQuery() throws BadParameterException {
		this.setQuery(null);
	}

	public abstract String getScheme();

	public abstract void setScheme(String scheme) throws BadParameterException ;

	public void setScheme() throws BadParameterException {
		this.setScheme(null);
	}

	public abstract String getUserInfo() ;

	public abstract void setUserInfo(String userInfo) throws BadParameterException ;

	public void setUserInfo() throws BadParameterException {
		this.setUserInfo(null);
	}

	public abstract URL translate(String scheme) throws BadParameterException,	NoSuccessException ;

	public URL translate(Session session, String scheme) throws BadParameterException, NoSuccessException {
		return this.translate(scheme);
	}

	public abstract boolean isAbsolute() ;

	public abstract URL normalize() ;
	
    ////////////////////////////////////////// cache methods //////////////////////////////////////////

    public void setCache(FileAttributes cache) {
        m_cache = cache;
        this.m_cache_creation_time = System.currentTimeMillis();
    }

    public FileAttributes getCache() {
        return m_cache;
    }

    public boolean hasCache() {
    	if (this.m_cache == null) return false;
    	return (System.currentTimeMillis() - this.m_cache_creation_time
    			< EngineProperties.getInteger(EngineProperties.DATA_ATTRIBUTES_CACHE_LIFETIME).longValue());
    }


}
