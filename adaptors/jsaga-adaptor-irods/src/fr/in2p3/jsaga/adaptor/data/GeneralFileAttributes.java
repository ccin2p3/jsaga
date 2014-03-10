package fr.in2p3.jsaga.adaptor.data;

import java.io.File;

import org.irods.jargon.core.pub.io.IRODSFile;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GeneralFileAttributes
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class GeneralFileAttributes extends FileAttributes {
    private File m_generalFile;
    
    public GeneralFileAttributes(File generalFile) {
        m_generalFile = generalFile;
    }

    public String getName() {
        return m_generalFile.getName();
    }

    public int getType() {
        if (m_generalFile.isDirectory()) {
            return TYPE_DIRECTORY;
        } else if (m_generalFile.isFile()) {
            return TYPE_FILE;
        } else {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        if (m_generalFile.isFile()) {
            return m_generalFile.length() ;
        } else {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if (m_generalFile.canRead()) {
			perms = perms.or(PermissionBytes.READ);
        }
		if (m_generalFile.canWrite()) {
			perms = perms.or(PermissionBytes.WRITE);
		}
        return perms;
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
        return m_generalFile.lastModified();
    }
}
