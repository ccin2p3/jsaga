package fr.in2p3.jsaga.helpers;

import fr.in2p3.jsaga.JSagaURL;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   URLFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class URLFactory {
    public static boolean isDirectory(URL url) throws NotImplemented {
        return isDirectory(url.getPath());
    }
    public static boolean isDirectory(String path) throws NotImplemented {
        return path.endsWith("/") || path.endsWith("/.") || path.endsWith("/..") || path.equals(".") || path.equals("..");
    }

    public static URL toFileURL(URL url) throws NotImplemented, BadParameter {
        if (isDirectory(url)) {
            throw new BadParameter("File URL must not end with slash: "+url.getURL());
        }
        return url;
    }
    public static String toFilePath(String path) throws NotImplemented, BadParameter {
        if (isDirectory(path)) {
            throw new BadParameter("File path must not end with slash: "+path);
        }
        return path;
    }

    public static URL toDirectoryURL(URL url) throws NotImplemented, BadParameter {
        String path = url.getPath();
        if (!path.endsWith("/")) {
            url.setPath(path+"/");
        }
        return url;
    }
    public static String toDirectoryPath(String path) throws NotImplemented, BadParameter {
        if (!path.endsWith("/")) {
            return path+"/";
        } else {
            return path;
        }
    }

    public static URL createURL(URL base, URL relativePath) throws NotImplemented, BadParameter, NoSuccess {
        // check URL
        boolean isDir = base.getPath().endsWith("/");
        boolean isAbsolute = relativePath.getPath().startsWith("/");
        if (!isDir && !isAbsolute) {
            throw new BadParameter("INTERNAL ERROR: path must be relative to a directory: "+base.getURL());
        }
        // resolve
        String absolutePath = resolve(base, relativePath.getPath());
        if (relativePath instanceof JSagaURL) {
            return new JSagaURL(((JSagaURL)relativePath).getAttributes(), absolutePath);
        } else {
            return new URL(absolutePath);
        }
    }

    public static URL createURL(URL base, String name) throws NotImplemented, BadParameter, NoSuccess {
        // check URL
        boolean isDir = base.getPath().endsWith("/");
        boolean isAbsolute = name.startsWith("/");
        if (!isDir && !isAbsolute) {
            throw new BadParameter("INTERNAL ERROR: path must be relative to a directory: "+base.getURL());
        }
        // resolve
        return new URL(resolve(base, name));
    }

    public static URL createParentURL(URL base) throws NotImplemented, BadParameter, NoSuccess {
        // get parent directory
        String parent = (base.getPath().endsWith("/") ? ".." : ".");
        // resolve
        return new URL(resolve(base, parent));
    }

    private static String resolve(URL base, String name) throws NotImplemented, BadParameter, NoSuccess {
        java.net.URI baseUri;
        try {
            baseUri = new java.net.URI(base.getURL());
        } catch (URISyntaxException e) {
            throw new BadParameter(e);
        }
        String relativePath = JSagaURL.encodePath(name)
                + (baseUri.getQuery()!=null ? "?"+baseUri.getQuery() : "")
                + (baseUri.getFragment()!=null ? "#"+baseUri.getFragment() : "");
        java.net.URI uri = baseUri.resolve(relativePath);
        return uri.toString();
    }
}
