package fr.in2p3.jsaga.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EntryPath
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EntryPath {
    private static final Pattern BASEDIR_PATTERN = Pattern.compile("(.*/)[^/]+/*");
    private static final Pattern ENTRYNAME_PATTERN = Pattern.compile(".*/([^/]+)/*");
    private String m_path;

    public EntryPath(String path) {
        m_path = path;
    }

    public boolean isAbsolute() {
        return m_path.startsWith("/");
    }

    public String getBaseDir() {
        Matcher m = BASEDIR_PATTERN.matcher(m_path);
        if (m.find() && m.groupCount()>0) {
            return m.group(1);
        } else if (this.isAbsolute()) {
            // absolute: root directory is "/"
            return "/";
        } else {
            // relative: there is no parent directory
            return "";
        }
    }

    public String getEntryName() {
        Matcher m = ENTRYNAME_PATTERN.matcher(m_path);
        if (m.find() && m.groupCount()>0) {
            return m.group(1);
        } else if (this.isAbsolute()) {
            // absolute: root directory has no name
            return "";
        } else {
            // relative: there is no parent directory
            int pos = m_path.indexOf('/');
            if (pos > -1) {
                return m_path.substring(0, pos);
            } else {
                return m_path;
            }
        }
    }
}
