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
    private boolean m_isAbsolute;
    private boolean m_isDir;

    public UniversalFile(String path) {
        m_file = new File(path);
        m_isAbsolute = m_file.isAbsolute() || path.startsWith("/") || path.startsWith("\\");
        m_isDir = path.endsWith("/") || path.endsWith("\\");
    }

    public String getPath() {
        return m_file.getPath().replace("\\", "/") + (m_isDir?"/":"");
    }

    public String getParent() {
        String parent = m_file.getParent();
        if (parent == null) {
            return ".";
        }
        parent = parent.replace("\\", "/");
        if (! "/".equals(parent)) {
            parent = parent + "/";
        }
        return parent;
    }

    public String getCanonicalPath() throws IOException {
        String canon;
        if (m_file.isAbsolute()) {
            canon = m_file.getCanonicalPath();
        } else if (System.getProperty("os.name").startsWith("Windows") && m_isAbsolute) {
            canon = m_file.getCanonicalPath().substring(2);
        } else {
            String pwd = new File(System.getProperty("user.dir")).getCanonicalPath();
            canon = m_file.getCanonicalPath();
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

    public static void main(String[] args) throws IOException {
        System.out.println(new File("C:/path/to/file").getCanonicalPath());
        System.out.println(new File("/path/to/file").getCanonicalPath());
        System.out.println(new File("path/to/file").getCanonicalPath());
        System.out.println(new File("/dir/").getCanonicalPath());

    }
}
