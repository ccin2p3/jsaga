package fr.in2p3.jsaga.adaptor.data.file;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LocalFileAttributes extends FileAttributes {
    private File m_entry;

    public LocalFileAttributes(File entry) {
        m_entry = entry;
    }

    public String getName() {
        if (m_entry.getParentFile() != null) {
            return m_entry.getName();
        } else {
            // m_entry.getName() is empty for Windows drives
            return m_entry.getPath().substring(0,2);
        }
    }

    public int getType() {
        if (m_entry.isDirectory()) {
            return TYPE_DIRECTORY;
        } else if (m_entry.isFile()) {
            return TYPE_FILE;
        } else {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        if (m_entry.isFile()) {
            return m_entry.length();
        } else {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getGroupPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getAnyPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if(m_entry.canRead()) {
            perms = perms.or(PermissionBytes.READ);
        }
        if(m_entry.canWrite()) {
            perms = perms.or(PermissionBytes.WRITE);
        }
        return perms;
    }

    public String getOwner() {
        return ID_UNKNOWN;
    }

    public String getGroup() {
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        return m_entry.lastModified();
    }
}
