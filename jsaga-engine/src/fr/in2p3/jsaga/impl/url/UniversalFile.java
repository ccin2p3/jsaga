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
     * and with a trailing / if the file is a directory
     * @return the canonical path of the file
     * @throws IOException
     * 
     * To compute the canonical path, the method java.io.File.getCanonicalPath() is used.
     * This method first converts this pathname to absolute form if necessary, as if by 
     * invoking the getAbsolutePath() method.
     * 
     * Description of the getCanonicalPath() on Windows:
     * 1) c:/path/to/file -> C:\path\to\file
     * 2) /path/to/file   -> E:\path\to\file (where E: is the drive containing the current directory)
     * 3) path/to/file    -> E:\currentdir\path\to\file (where E:currentdir is the current directory)
     * 
     * Description of the getCanonicalPath() on Unix:
     * 4) /path/to/file   -> /path/to/file
     * 5) path/to/file    -> /currentdir/path/to/file (where /currentdir is the current directory)
     * 
     * the current directory is defined in the "user.dir" property
     */
    public String getCanonicalPath() throws IOException {
        String canon;
        if (m_file.isAbsolute()) { // cases 1) and 4)
            canon = m_file.getCanonicalPath();
        } else if (System.getProperty("os.name").startsWith("Windows") && m_isAbsolute) { // case 2)
            canon = m_file.getCanonicalPath().substring(2);
        } else { // cases 3) and 5)
        	// get the canonical path of the current directory
            String pwd = new File(System.getProperty("user.dir")).getCanonicalPath();
            // get the canonical (absolute) path of the file
            canon = m_file.getCanonicalPath();
        	// remove the canonicalized part of the current directory
            canon = canon.substring(pwd.length()+1);
        }
        return canon.replace("\\", "/") + (m_isDir?"/":"");
    }

    public boolean isAbsolute() {
        return m_isAbsolute;
    }

    public boolean isDirectory() {
        return m_isDir;
    }

}
