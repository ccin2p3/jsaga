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
    public SRM22FileAttributes(TMetaDataPathDetail entry) throws NoSuccessException {
        // set name
        String path = entry.getPath();
        int pos = path.lastIndexOf("/");
        if (pos > 0) {
            m_name = path.substring(pos+1);
        } else {
            throw new NoSuccessException("unexpected exception");
        }

        // set type
        TFileType type = entry.getType();
        if (TFileType.FILE.equals(type)) {
            m_type = FileAttributes.FILE_TYPE;
        } else if (TFileType.DIRECTORY.equals(type)) {
            m_type = FileAttributes.DIRECTORY_TYPE;
        } else if (TFileType.LINK.equals(type)) {
            m_type = FileAttributes.LINK_TYPE;
        } else {
            m_type = FileAttributes.UNKNOWN_TYPE;
        }

        // set size
        if (entry.getSize() != null) {
            m_size = entry.getSize().intValue();
        }

        // set permission
        if (entry.getOwnerPermission() != null) {
            TPermissionMode perm = entry.getOwnerPermission().getMode();
            if (TPermissionMode.NONE.equals(perm)) {
                m_permission = PermissionBytes.NONE;
            } else if (TPermissionMode.X.equals(perm)) {
                m_permission = PermissionBytes.EXEC;
            } else if (TPermissionMode.W.equals(perm)) {
                m_permission = PermissionBytes.WRITE;
            } else if (TPermissionMode.R.equals(perm)) {
                m_permission = PermissionBytes.READ;
            } else if (TPermissionMode.WX.equals(perm)) {
                m_permission = PermissionBytes.WRITE.or(PermissionBytes.EXEC);
            } else if (TPermissionMode.RX.equals(perm)) {
                m_permission = PermissionBytes.READ.or(PermissionBytes.EXEC);
            } else if (TPermissionMode.RW.equals(perm)) {
                m_permission = PermissionBytes.READ.or(PermissionBytes.WRITE);
            } else if (TPermissionMode.RWX.equals(perm)) {
                m_permission = PermissionBytes.READ.or(PermissionBytes.WRITE).or(PermissionBytes.EXEC);
            }
        }

        // set owner
        if (entry.getOwnerPermission() != null) {
            m_owner = entry.getOwnerPermission().getUserID();
        }

        // set last modified
        if (entry.getLastModificationTime() != null) {
            m_lastModified = entry.getLastModificationTime().getTimeInMillis();
        }
    }
}
