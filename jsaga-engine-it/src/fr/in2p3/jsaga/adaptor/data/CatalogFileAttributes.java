package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.schema.data.catalog.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CatalogFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CatalogFileAttributes extends FileAttributes {
    private EntryType m_entry;

    public CatalogFileAttributes(EntryType entry) {
        m_entry = entry;
    }

    public CatalogFileAttributes(EntryType entry, String relativePath) {
        this(entry);
        m_relativePath = relativePath;
    }

    public String getName() {
        return m_entry.getName();
    }

    public int getType() {
        if (m_entry instanceof DirectoryType) {
            return TYPE_DIRECTORY;
        } else if (m_entry instanceof FileType) {
            FileType file = (FileType) m_entry;
            if (file.getLink() != null) {
                return TYPE_LINK;
            } else {
                return TYPE_FILE;
            }
        } else {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        if (m_entry instanceof FileType) {
            FileType file = (FileType) m_entry;
            return file.getReplicaCount();
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
