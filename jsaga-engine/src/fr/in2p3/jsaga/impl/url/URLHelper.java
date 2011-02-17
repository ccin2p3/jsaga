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
	
	private static String[] localSchemes={"file","zip"};
	
    public static boolean isDirectory(URL url) throws NotImplementedException {
        return isDirectory(url.getPath());
    }
    public static boolean isDirectory(String path) throws NotImplementedException {
        return path.endsWith("/") || path.endsWith("/.") || path.endsWith("/..") || path.equals(".") || path.equals("..");
    }

    public static URL toFileURL(URL url) throws NotImplementedException, BadParameterException {
        if (isDirectory(url)) {
            throw new BadParameterException("File URL must not end with slash: "+url);
        }
        return url;
    }
    public static String toFilePath(String path) throws NotImplementedException, BadParameterException {
        if (isDirectory(path)) {
            throw new BadParameterException("File path must not end with slash: "+path);
        }
        return path;
    }

    public static String toDirectoryPath(String path) throws NotImplementedException, BadParameterException {
        if (!path.endsWith("/")) {
            return path+"/";
        } else {
            return path;
        }
    }

    public static URL createURL(URL base, URL relativePath) throws NotImplementedException, BadParameterException, NoSuccessException {
        // check URL
        boolean isDir = base.getPath().endsWith("/");
        boolean isAbsolute = relativePath.getPath().startsWith("/");
        if (!isDir && !isAbsolute) {
            throw new BadParameterException("INTERNAL ERROR: path must be relative to a directory: "+base);
        }
        URL url;
        url = base.resolve(relativePath);
        if ( ((AbstractURLImpl)relativePath).hasCache() ) {
            FileAttributes cache = ((AbstractURLImpl)relativePath).getCache();
            ((AbstractURLImpl)url).setCache(cache);
        }
        return url;
    }

    public static URL createURL(URL base, String name) throws NotImplementedException, BadParameterException, NoSuccessException {
    	return createURL(base, new RelativeURLImpl(name));
    }

    public static URL getParentURL(URL base) throws NotImplementedException, BadParameterException, NoSuccessException {
        // get parent directory
        String parent = (base.getPath().endsWith("/") ? ".." : ".");
        // resolve
        return base.resolve(new RelativeURLImpl(parent));
    }

    public static String getName(URL url) throws NotImplementedException {
        String[] names = url.getPath().split("/");
        String name;
        if (names.length > 0) {
            name = names[names.length-1];
            if (name.equals("")) {
                name = null;
            }
        } else {
            name = "/";
        }
        return name;
    }
    
    public static boolean startsWithLocalScheme(String url) {
    	if (url == null || url == "") {
    		return false;
    	}
    	for (String scheme : localSchemes) {
    		if (url.startsWith(scheme+":/")) return true;
    	}
    	return false;
    }

}
