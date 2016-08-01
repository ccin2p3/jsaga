package fr.in2p3.jsaga.impl.url;

import java.io.File;
import java.io.IOException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UniversalFile
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class UniversalFile {
    private File m_file;
    /**
     * Indicate if this file is absolute
     */
    private boolean m_isAbsolute;
    
    /**
     * Indicate if this file is a directory
     */
    private boolean m_isDir;

    /**
     * Build an universal file (Windows and Unix)
     * @param path
     * 
     * If the path ends with / or \, it is a directory
     * On Unix, if the path starts with /, the file is considered as absolute
     * On Windows, if the path starts with / or \ or X:/, the file is considered as absolute
     */
    public UniversalFile(String path) {
		// remove leading duplicated / 
        while (path.startsWith("//")) {
        	path = path.substring(1);
        }
        m_file = new File(path);
        m_isAbsolute = m_file.isAbsolute() || path.startsWith("/") || path.startsWith("\\");
        m_isDir = path.endsWith("/") || path.endsWith("\\");
    }

    /**
     * returns the path of the file with all separators converted as / 
     * and with a trailing / if the file is a directory 
     * @return the path of the file
     */
    public String getPath() {
        return m_file.getPath().replace("\\", "/") + (m_isDir?"/":"");
    }

    /**
     * returns the parent directory of the file with all separators converted as / and with a trailing /
     * @return the path of the parent directory
     */
    public String getParent() {
        String parent = m_file.getParent();
        if (parent == null) {
            return "./";
        }
        parent = parent.replace("\\", "/");
        if (! "/".equals(parent)) {
            parent = parent + "/";
        }
        return parent;
    }

    /**
     * returns the canonical path of the file with all separators converted as / 
     * and with a trailing / if the file is a directory. 
     * It removes all "./" and remove recursively all "dir/../" sets
     * @return the canonical path of the file
     * 
     * To compute the canonical path, the method java.io.File.getCanonicalPath() is not used
     * because this method first converts this pathname to absolute form if necessary, as if by 
     * invoking the getAbsolutePath() method and some characters are not supported by Windows
     * 
     */
    public String getCanonicalPath() {
    	String canon;
    	java.util.Stack path = new java.util.Stack();
    	for (String pathElement : getPath().split("/")) {
    		if (pathElement.equals("..")) {  
    			// remove last element if stack is not empty and last element is not ".." itself
    			if (path.empty() || path.peek().equals("..")) {
    				path.add(pathElement);
    			} else {
    				path.pop();
    			}
    		} else if (! pathElement.equals(".") && !pathElement.equals("")) { // NOT "." and not ""
    			path.add(pathElement);
    		} // otherwise (".." or ""): nothing to do
    	}
    	if (getPath().startsWith("/")) {
    		canon = "/";
    	} else {
    		canon = "";
    	}
    	for (int i = 0; i < path.size(); i++) {
    		canon += (String)path.get(i);
    		if (i == path.size()-1) { // last element : add / only for Dir
    			canon += m_isDir?"/":"";
    		} else {
    			canon += "/";
    		}
    	}
    	// if everything was removed, it means the path is "./"
        if (canon.equals("")) { canon = "./";}
        return canon;
    }

    public boolean isAbsolute() {
        return m_isAbsolute;
    }

    public boolean isDirectory() {
        return m_isDir;
    }

}
