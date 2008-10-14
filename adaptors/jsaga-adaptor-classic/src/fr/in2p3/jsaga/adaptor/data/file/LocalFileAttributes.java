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
    public LocalFileAttributes(File entry) {
        m_name = (entry.getParentFile()!=null
                ? entry.getName()
                : "/"+entry.getPath().substring(0,2));  // format Windows drive to enable inclusion in URL

        m_type = entry.isDirectory()
                ? FileAttributes.DIRECTORY_TYPE
                : entry.isFile()
                    ? FileAttributes.FILE_TYPE
                    : FileAttributes.UNKNOWN_TYPE;

        m_size = (entry.isFile() ? entry.length() : 0);

        m_permission = PermissionBytes.NONE;
        if(entry.canRead()) {
            m_permission = m_permission.or(PermissionBytes.READ);
        }
        if(entry.canWrite()) {
            m_permission = m_permission.or(PermissionBytes.WRITE);
        }

        m_lastModified = entry.lastModified();
    }
}
