package fr.in2p3.jsaga.adaptor.data;

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
    public CatalogFileAttributes(EntryType entry) {
        m_name = entry.getName();
        if (entry instanceof DirectoryType) {
            m_type = FileAttributes.DIRECTORY_TYPE;
            m_size = 0;
        } else if (entry instanceof FileType) {
            FileType file = (FileType) entry;
            if (file.getLink() != null) {
                m_type = FileAttributes.LINK_TYPE;
                m_size = 0;
            } else {
                m_type = FileAttributes.FILE_TYPE;
                m_size = file.getReplicaCount();
            }
        } else {
            m_type = FileAttributes.UNKNOWN_TYPE;
            m_size = -1;
        }
    }
}
