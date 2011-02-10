package fr.in2p3.jsaga.impl.url;

import java.util.regex.Pattern;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   URLFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class URLFactoryImpl extends URLFactory {
    protected URL doCreateURL(String url) throws BadParameterException, NoSuccessException {
    	return (Pattern.matches("^[^/\\\\]{2,}\\:.*", url))?new AbsoluteURLImpl(url):new RelativeURLImpl(url);
    }

    public static URL createUnencodedURL(String url) throws BadParameterException {
    	// TODO: check why no encoding
        return new URLImpl(url, false); // encode = false
    }

    public static URL createRelativePath(String relativePath) throws BadParameterException {
        return new RelativeURLImpl(relativePath);
    }

    public static URL createURLWithCache(FileAttributes cache) throws BadParameterException {
        return new RelativeURLImpl(cache);
    }
    
}
