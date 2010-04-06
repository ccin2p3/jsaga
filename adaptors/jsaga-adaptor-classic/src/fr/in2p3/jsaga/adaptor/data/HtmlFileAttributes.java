package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HtmlFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HtmlFileAttributes extends FileAttributes {
    private String m_entryName;
    private boolean m_isDir;

    public HtmlFileAttributes(String entryName, boolean isDir) {
        m_entryName = entryName;
        m_isDir = isDir;
    }

    public String getName() {
        return m_entryName;
    }

    public int getType() {
        if (m_isDir) {
            return TYPE_DIRECTORY;
        } else {
            return TYPE_FILE;
        }
    }

    public long getSize() {
        return SIZE_UNKNOWN;
    }

    public PermissionBytes getUserPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getGroupPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getAnyPermission() {
        return PERMISSION_UNKNOWN;
    }

    public String getOwner() {
        return ID_UNKNOWN;
    }

    public String getGroup() {
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        return DATE_UNKNOWN;
    }
}
