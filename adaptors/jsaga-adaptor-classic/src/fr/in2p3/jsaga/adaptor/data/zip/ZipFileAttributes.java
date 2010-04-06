package fr.in2p3.jsaga.adaptor.data.zip;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

import java.util.zip.ZipEntry;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ZipFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ZipFileAttributes extends FileAttributes {
    private ZipEntry m_entry;
    private String m_basePath;

    public ZipFileAttributes(ZipEntry entry, String basePath) {
        m_entry = entry;
        m_basePath = basePath;
    }

    public String getName() {
        String name = m_entry.getName().substring(m_basePath.length());
        if (m_entry.isDirectory()) {
            return name.substring(0, name.length()-1);
        } else {
            return name;
        }
    }

    public int getType() {
        if (m_entry.isDirectory()) {
            return TYPE_DIRECTORY;
        } else {
            return TYPE_FILE;
        }
    }

    public long getSize() {
        return m_entry.getSize();
    }

    public PermissionBytes getUserPermission() {
        return PermissionBytes.READ;    // always readable, but writable not yet supported
    }

    public PermissionBytes getGroupPermission() {
        return PermissionBytes.READ;    // always readable, but writable not yet supported
    }

    public PermissionBytes getAnyPermission() {
        return PermissionBytes.READ;    // always readable, but writable not yet supported
    }

    public String getOwner() {
        return ID_UNKNOWN;
    }

    public String getGroup() {
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        return m_entry.getTime();
    }
}
