package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.srm22.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRM22FileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SRM22FileAttributes extends FileAttributes {
    private TMetaDataPathDetail m_entry;

    public SRM22FileAttributes(TMetaDataPathDetail entry) throws NoSuccessException {
        m_entry = entry;
    }

    public String getName() {
        String path = m_entry.getPath();
        int pos = path.lastIndexOf("/");
        if (pos > 0) {
            return path.substring(pos+1);
        } else {
            throw new RuntimeException("unexpected exception");
        }
    }

    public int getType() {
        TFileType type = m_entry.getType();
        if (TFileType.FILE.equals(type)) {
            return TYPE_FILE;
        } else if (TFileType.DIRECTORY.equals(type)) {
            return TYPE_DIRECTORY;
        } else if (TFileType.LINK.equals(type)) {
            return TYPE_LINK;
        } else {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        if (m_entry.getSize() != null) {
            return m_entry.getSize().longValue();
        } else {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        if (m_entry.getOwnerPermission() != null) {
            return getPermission(m_entry.getOwnerPermission().getMode());
        } else {
            return PERMISSION_UNKNOWN;
        }
    }

    public PermissionBytes getGroupPermission() {
        if (m_entry.getGroupPermission() != null) {
            return getPermission(m_entry.getGroupPermission().getMode());
        } else {
            return PERMISSION_UNKNOWN;
        }
    }

    public PermissionBytes getAnyPermission() {
        if (m_entry.getOtherPermission() != null) {
            return getPermission(m_entry.getOtherPermission());
        } else {
            return PERMISSION_UNKNOWN;
        }
    }

    private static PermissionBytes getPermission(TPermissionMode perm) {
        if (TPermissionMode.NONE.equals(perm)) {
            return PermissionBytes.NONE;
        } else if (TPermissionMode.X.equals(perm)) {
            return PermissionBytes.EXEC;
        } else if (TPermissionMode.W.equals(perm)) {
            return PermissionBytes.WRITE;
        } else if (TPermissionMode.R.equals(perm)) {
            return PermissionBytes.READ;
        } else if (TPermissionMode.WX.equals(perm)) {
            return PermissionBytes.WRITE.or(PermissionBytes.EXEC);
        } else if (TPermissionMode.RX.equals(perm)) {
            return PermissionBytes.READ.or(PermissionBytes.EXEC);
        } else if (TPermissionMode.RW.equals(perm)) {
            return PermissionBytes.READ.or(PermissionBytes.WRITE);
        } else if (TPermissionMode.RWX.equals(perm)) {
            return PermissionBytes.READ.or(PermissionBytes.WRITE).or(PermissionBytes.EXEC);
        } else {
            return PERMISSION_UNKNOWN;
        }
    }

    public String getOwner() {
        if (m_entry.getOwnerPermission() != null) {
            return m_entry.getOwnerPermission().getUserID();
        } else {
            return ID_UNKNOWN;
        }
    }

    public String getGroup() {
        if (m_entry.getGroupPermission() != null) {
            return m_entry.getGroupPermission().getGroupID();
        } else {
            return ID_UNKNOWN;
        }
    }

    public long getLastModified() {
        if (m_entry.getLastModificationTime() != null) {
            return m_entry.getLastModificationTime().getTimeInMillis();
        } else {
            return DATE_UNKNOWN;
        }
    }
}
