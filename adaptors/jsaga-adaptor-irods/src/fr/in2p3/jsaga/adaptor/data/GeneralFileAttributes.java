package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.GeneralFile;
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

    public GeneralFileAttributes(GeneralFile generalFile) {
        // set name
		m_name = generalFile.getName();

        // set type        
		if (generalFile.isDirectory()) {
			m_type = FileAttributes.DIRECTORY_TYPE;
		} else  {
			m_type = FileAttributes.FILE_TYPE;
		} 

        // set size
        if (generalFile.isFile()) {
            m_size = generalFile.length() ;
        } else {
            m_size = 0;
        }

        // set permission
        if (generalFile.canRead()) {
			m_permission = m_permission.or(PermissionBytes.READ);
        }
		if (generalFile.canWrite()) {
			m_permission = m_permission.or(PermissionBytes.WRITE);
		}
	
        // set last modified
		m_lastModified = generalFile.lastModified();
	}
}