package fr.in2p3.jsaga.adaptor.data;

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
    public ZipFileAttributes(ZipEntry entry, String basePath) {
        String name = entry.getName().substring(basePath.length());
        m_name = entry.isDirectory()
                ? name.substring(0, name.length()-1)
                : name;

        m_type = entry.isDirectory()
                ? FileAttributes.DIRECTORY_TYPE
                : FileAttributes.FILE_TYPE;

        m_size = entry.getSize();

        m_permission = PermissionBytes.READ.or(PermissionBytes.WRITE);

        m_lastModified = entry.getTime();
    }
}
