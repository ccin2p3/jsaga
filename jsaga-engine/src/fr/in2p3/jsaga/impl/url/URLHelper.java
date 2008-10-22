package fr.in2p3.jsaga.impl.url;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   URLHelper
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class URLHelper {
    public static boolean isDirectory(URL url) throws NotImplemented {
        return isDirectory(url.getPath());
    }
    public static boolean isDirectory(String path) throws NotImplemented {
        return path.endsWith("/") || path.endsWith("/.") || path.endsWith("/..") || path.equals(".") || path.equals("..");
    }

    public static URL toFileURL(URL url) throws NotImplemented, BadParameter {
        if (isDirectory(url)) {
            throw new BadParameter("File URL must not end with slash: "+url);
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
            throw new BadParameter("INTERNAL ERROR: path must be relative to a directory: "+base);
        }
        // resolve
        URLImpl url = new URLImpl(base, relativePath);
        if ( ((URLImpl)relativePath).hasCache() ) {
            FileAttributes cache = ((URLImpl)relativePath).getCache();
            url.setCache(cache);
        }
        return url;
    }

    public static URL createURL(URL base, String name) throws NotImplemented, BadParameter, NoSuccess {
        // check URL
        boolean isDir = base.getPath().endsWith("/");
        boolean isAbsolute = name.startsWith("/");
        if (!isDir && !isAbsolute) {
            throw new BadParameter("INTERNAL ERROR: path must be relative to a directory: "+base);
        }
        // resolve
        return new URLImpl(base, name);
    }

    public static URL getParentURL(URL base) throws NotImplemented, BadParameter, NoSuccess {
        // get parent directory
        String parent = (base.getPath().endsWith("/") ? ".." : ".");
        // resolve
        return new URLImpl(base, parent);
    }

    public static String getName(URL url) throws NotImplemented {
        String[] names = url.getPath().split("/");
        String name;
        if (names.length > 0) {
            name = names[names.length-1];
            if (name.equals("")) {
                name = null;
            }
        } else {
            name = null;
        }
        return name;
    }
}
