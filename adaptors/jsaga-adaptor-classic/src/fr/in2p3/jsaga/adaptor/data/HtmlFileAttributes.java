package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HtmlFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HtmlFileAttributes extends FileAttributes {
    public HtmlFileAttributes(String entryName, boolean isDir) {
        m_name = entryName;
        m_type = isDir
                ? FileAttributes.DIRECTORY_TYPE
                : FileAttributes.FILE_TYPE;
    }
}
