package fr.in2p3.jsaga.engine.workflow.task;

import org.ogf.saga.error.NoSuccessException;

import java.net.URI;
import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   URLDecomposer
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class URLDecomposer {
    private URI m_uri;
    
    public URLDecomposer(String url) throws NoSuccessException {
        try {
            m_uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new NoSuccessException(e);
        }
    }

    public String getGroup() {
        if ("tar".equals(m_uri.getScheme())) {
            return getGroup(m_uri.getScheme(), m_uri.getPath(), ".tar/");
        } else if ("tgz".equals(m_uri.getScheme())) {
            return getGroup(m_uri.getScheme(), m_uri.getPath(), ".tgz/");
        } else if ("zip".equals(m_uri.getScheme())) {
            return getGroup(m_uri.getScheme(), m_uri.getPath(), ".zip/");
        } else {
            return getGroup(m_uri.getScheme(), m_uri.getHost());
        }
    }
    public static String getGroup(String scheme, String path, String separator) {
        return scheme+"://"+path.substring(0, path.lastIndexOf(separator));
    }
    public static String getGroup(String scheme, String host) {
        return scheme+"://"+(host!=null ? host : "");
    }

    public String getLabel() {
        if ("tar".equals(m_uri.getScheme())) {
            return getLabel(m_uri.getPath(), ".tar/");
        } else if ("tgz".equals(m_uri.getScheme())) {
            return getLabel(m_uri.getPath(), ".tgz/");
        } else if ("zip".equals(m_uri.getScheme())) {
            return getLabel(m_uri.getPath(), ".zip/");
        } else {
            return getLabel(m_uri.getPath(), '/');
        }
    }
    private static String getLabel(String path, String separator) {
        String file = path.substring(path.indexOf(separator)+separator.length());
        if (file.lastIndexOf(',') > -1) {
            return getLabel(file, ',');
        } else if (file.lastIndexOf('/') > -1) {
            return getLabel(file, '/');
        } else {
            return file;
        }
    }
    private static String getLabel(String path, char separator) {
        return path.substring(path.lastIndexOf(separator)+1);
    }

    public String getContext() {
        return m_uri.getFragment();
    }
}
