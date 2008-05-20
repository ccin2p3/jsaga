package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.DataService;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

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
        this(new URL(url));
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

    public String getUserInfo() throws NotImplemented {
        if (m_url !=null && m_url.getUserInfo()!=null) {
            return m_url.getUserInfo();
        } else {
            return m_base.getUserInfo();
        }
    }

    public String getHost() throws NotImplemented {
        if (m_url !=null && m_url.getHost()!=null) {
            return m_url.getHost();
        } else if (BaseURL.NO_HOST.equals(m_base.getHost())) {
            return null;
        } else {
            return m_base.getHost();
        }
    }

    public int getPort() throws NotImplemented {
        if (m_url !=null && m_url.getPort()!=BaseURL.NO_PORT) {
            return m_url.getPort();
        } else {
            return m_base.getPort();
        }
    }

    public String getPath() throws NotImplemented {
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

    public void setAttributes(Map attributes) throws NotImplemented {
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

    public URI getURI() {
        try {
            String host = this.getHost();
            String path = this.getPath();
            return new URI(this.getScheme(), this.getUserInfo(), (host!=null ? host : ""),
                    this.getPort(), (path.startsWith("./") ? "/"+path : path),
                    this.getQuery(), this.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (NotImplemented e) {
            throw new RuntimeException(e);
        }
    }

    private String getScheme() throws NotImplemented {
        return m_url.getScheme();
    }

    private String getQuery() throws NotImplemented {
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

    private String getFragment() throws NotImplemented {
        return m_url.getFragment();
    }
}
