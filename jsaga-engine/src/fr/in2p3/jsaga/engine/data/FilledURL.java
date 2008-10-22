package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.DataService;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FilledURL
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FilledURL {
    private URI m_base;
    private URL m_url;

    public FilledURL(String url) throws NotImplemented, BadParameter, NoSuccess {
        this(URLFactory.createURL(url));
    }
    
    public FilledURL(URL url) throws NotImplemented, NoSuccess {
        this(url, Configuration.getInstance().getConfigurations().getProtocolCfg().findDataService(url));
    }

    public FilledURL(URL url, DataService config) throws NotImplemented, NoSuccess {
        // set base URL
        try {
            if (config.getBase() != null) {
                m_base = new URI(config.getBase());
            } else {
                m_base = new URI("base", null, BaseURL.NO_HOST, BaseURL.NO_PORT, null, null, null);
            }
        } catch (URISyntaxException e) {
            throw new NoSuccess(e);
        }

        // set URL
        m_url = url;
    }

    public String getUserInfo() {
        if (m_url !=null && m_url.getUserInfo()!=null) {
            return m_url.getUserInfo();
        } else {
            return m_base.getUserInfo();
        }
    }

    public String getHost() {
        if (m_url !=null && m_url.getHost()!=null) {
            return m_url.getHost();
        } else if (BaseURL.NO_HOST.equals(m_base.getHost())) {
            return null;
        } else {
            return m_base.getHost();
        }
    }

    public int getPort() {
        if (m_url !=null && m_url.getPort()!=BaseURL.NO_PORT) {
            return m_url.getPort();
        } else {
            return m_base.getPort();
        }
    }

    public String getPath() {
        if (m_url !=null && m_url.getPath()!=null) {
            if (m_base.getPath() != null && !m_url.getPath().startsWith(m_base.getPath())) {
                return m_base.getPath() + "/" + m_url.getPath();
            } else {
                return m_url.getPath();
            }
        } else {
            return m_base.getPath();
        }
    }

    public void setAttributes(Map attributes) {
        if (m_base.getQuery()!=null) {
            setAttributes(m_base.getQuery(), attributes);
        }
        if (m_url !=null && m_url.getQuery()!=null) {
            setAttributes(m_url.getQuery(), attributes);
        }
    }
    private static void setAttributes(String query, Map attributes) {
        int unnamed = 1;
        String[] pairs = query.split("&");
        for (int i=0; i<pairs.length; i++) {
            int pos = pairs[i].indexOf("=");
            if (pos > 0) {
                attributes.put(pairs[i].substring(0, pos), pairs[i].substring(pos+1));
            } else {
                attributes.put("unnamed"+(unnamed++), pairs[i]);
            }
        }
    }

    /** Decodes the URL */
    public String getString() {
        StringBuffer buf = new StringBuffer();
        if (this.getScheme() != null) {
            buf.append(this.getScheme());
            buf.append("://");
        }
        if (this.getHost() != null) {
            if (this.getUserInfo() != null) {
                buf.append(this.getUserInfo());
                buf.append("@");
            }
            buf.append(this.getHost());
            if (this.getPort() > -1) {
                buf.append(':');
                buf.append(this.getPort());
            }
        }
        buf.append(this.getPath());
        if (this.getQuery() != null) {
            buf.append('?');
            buf.append(this.getQuery());
        }
        if (this.getFragment() != null) {
            buf.append('#');
            buf.append(this.getFragment());
        }
        return buf.toString();
    }

    private String getScheme() {
        return m_url.getScheme();
    }

    private String getQuery() {
        if (m_base.getQuery()!=null && m_url.getQuery()!=null) {
            return m_base.getQuery()+"&"+m_url.getQuery();
        } else if (m_base.getQuery()!=null) {
            return m_base.getQuery();
        } else if (m_url.getQuery()!=null) {
            return m_url.getQuery();
        } else {
            return null;
        }
    }

    private String getFragment() {
        return m_url.getFragment();
    }
}
