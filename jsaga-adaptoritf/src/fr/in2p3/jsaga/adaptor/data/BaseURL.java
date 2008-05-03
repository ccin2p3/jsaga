package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.URL;
import org.ogf.saga.error.IncorrectURL;
import org.ogf.saga.error.NotImplemented;

import java.net.URI;
import java.net.URISyntaxException;

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
    public static final String NO_HOST = "NOHOST";
    public static final int NO_PORT = -1;
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

    public int getPort(URL url) throws NotImplemented {
        if (url!=null && url.getPort()!=NO_PORT) {
            return url.getPort();
        } else {
            return m_base.getPort();
        }
    }

    public String toString() {
        return m_base.toString();
    }
}
