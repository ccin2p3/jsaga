package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.NoSuccess;
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
    public SRM22FileAttributes(TMetaDataPathDetail entry) throws NoSuccess {
        // set name
        String path = entry.getPath();
        int pos = path.lastIndexOf("/");
        if (pos > 0) {
            m_name = path.substring(pos+1);
        } else {
            throw new NoSuccess("unexpected exception");
        }

        // set type
        TFileType type = entry.getType();
        if (type.equals(TFileType.FILE)) {
            m_type = FileAttributes.FILE_TYPE;
        } else if (type.equals(TFileType.DIRECTORY)) {
            m_type = FileAttributes.DIRECTORY_TYPE;
        } else if (type.equals(TFileType.LINK)) {
            m_type = FileAttributes.LINK_TYPE;
        } else {
            m_type = FileAttributes.UNKNOWN_TYPE;
        }

        // set size
        m_size = entry.getSize().intValue();

        // set permission
        if (entry.getOwnerPermission() != null) {
            TPermissionMode perm = entry.getOwnerPermission().getMode();
            if (perm.equals(TPermissionMode.NONE)) {
                m_permission = PermissionBytes.NONE;
            } else if (perm.equals(TPermissionMode.X)) {
                m_permission = PermissionBytes.EXEC;
            } else if (perm.equals(TPermissionMode.W)) {
                m_permission = PermissionBytes.WRITE;
            } else if (perm.equals(TPermissionMode.R)) {
                m_permission = PermissionBytes.READ;
            } else if (perm.equals(TPermissionMode.WX)) {
                m_permission = PermissionBytes.WRITE.or(PermissionBytes.EXEC);
            } else if (perm.equals(TPermissionMode.RX)) {
                m_permission = PermissionBytes.READ.or(PermissionBytes.EXEC);
            } else if (perm.equals(TPermissionMode.RW)) {
                m_permission = PermissionBytes.READ.or(PermissionBytes.WRITE);
            } else if (perm.equals(TPermissionMode.RWX)) {
                m_permission = PermissionBytes.READ.or(PermissionBytes.WRITE).or(PermissionBytes.EXEC);
            }
        } else {
            m_permission = PermissionBytes.UNKNOWN;
        }

        // set last modified
        m_lastModified = entry.getLastModificationTime().getTimeInMillis();
    }
}
