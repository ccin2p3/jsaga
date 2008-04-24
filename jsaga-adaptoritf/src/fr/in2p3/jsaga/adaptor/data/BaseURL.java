package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.URL;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NotImplemented;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BaseURL
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BaseURL {
    private static final String NO_HOST = "NOHOST";
    private static final int NO_PORT = -1;
    private URI m_base;

    public BaseURL(String userInfo, String host, int port, String basePath, String additionalArgs) throws IncorrectURL {
        try {
            if (host == null && (port != NO_PORT || basePath == null)) {
                host = NO_HOST;
            }
            m_base = new URI("base", userInfo, host, port, basePath, additionalArgs, null);
        } catch (URISyntaxException e) {
            throw new IncorrectURL(e);
        }
    }

    public BaseURL(String host, int port) throws IncorrectURL {
        this(null, host, port, null, null);
    }

    public BaseURL(int port) throws IncorrectURL {
        this(null, null, port, null, null);
    }

    public BaseURL() throws IncorrectURL {
        this(null, null, NO_PORT, null, null);
    }

    public String getUserInfo(URL url) throws NotImplemented {
        if (url!=null && url.getUserInfo()!=null) {
            return url.getUserInfo();
        } else {
            return m_base.getUserInfo();
        }
    }

    public String getHost(URL url) throws NotImplemented {
        if (url!=null && url.getHost()!=null) {
            return url.getHost();
        } else if (NO_HOST.equals(m_base.getHost())) {
            return null;
        } else {
            return m_base.getHost();
        }
    }

    public int getPort(URL url) throws NotImplemented {
        if (url!=null && url.getPort()!=NO_PORT) {
            return url.getPort();
        } else {
            return m_base.getPort();
        }
    }

    public String getPath(URL url) throws NotImplemented {
        if (url!=null && url.getPath()!=null) {
            if (m_base.getPath() != null && !url.getPath().startsWith(m_base.getPath())) {
                return m_base.getPath() + "/" + url.getPath();
            } else {
                return url.getPath();
            }
        } else {
            return m_base.getPath();
        }
    }

    public void setAttributes(Map attributes) throws NotImplemented {
        if (m_base.getQuery()!=null) {
            setAttributes(m_base.getQuery(), attributes);
        }
    }
    public void setAttributes(URL url, Map attributes) throws NotImplemented {
        if (m_base.getQuery()!=null) {
            setAttributes(m_base.getQuery(), attributes);
        }
        if (url!=null && url.getQuery()!=null) {
            setAttributes(url.getQuery(), attributes);
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
}
