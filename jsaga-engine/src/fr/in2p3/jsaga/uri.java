package fr.in2p3.jsaga;

import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   uri
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class uri {
    public static String protocol(String uri) throws Exception {
        return URLFactory.createURL(uri).getScheme();
    }

    public static String host(String uri) throws Exception {
        String host = URLFactory.createURL(uri).getHost();
        if (host != null) {
            return host;
        } else {
            return "localhost";
        }
    }

    public static String basedirURI(String uri) throws Exception {
        URL u = URLFactory.createURL(uri);
        return new java.net.URI(u.getScheme(), u.getUserInfo(), u.getHost(), u.getPort(), _basedirPath(u), null, null).toString();
    }

    public static String basedirPath(String uri) throws Exception {
        return _basedirPath(URLFactory.createURL(uri));
    }

    public static String filename(String uri) throws Exception {
        return _filename(URLFactory.createURL(uri));
    }

    public static String context(String uri) throws Exception {
        return URLFactory.createURL(uri).getFragment();
    }

    public static boolean isDirectory(String uri) throws Exception {
        return URLFactory.createURL(uri).getPath().endsWith("/");
    }

    public static boolean isRelative(String uri) throws Exception {
        return URLFactory.createURL(uri).getPath().startsWith("/");
    }

    private static String _basedirPath(URL uri) throws Exception {
        String[] array = uri.getPath().split("/");
        if (array.length > 0) {
            StringBuffer buffer = new StringBuffer();
            // root
            if (array[0].equals("")) {
                buffer.append('/');
            }
            // baseDir
            for (int i=0; i<array.length-1; i++) {
                if (!array[i].equals("")) {
                    buffer.append(array[i]);
                    buffer.append('/');
                }
            }
            return buffer.toString();
        } else {
            return "/";
        }
    }

    private static String _filename(URL uri) throws Exception {
        String[] array = uri.getPath().split("/");
        if (array.length > 0) {
            return array[array.length-1];
        } else {
            return "";
        }
    }
}
