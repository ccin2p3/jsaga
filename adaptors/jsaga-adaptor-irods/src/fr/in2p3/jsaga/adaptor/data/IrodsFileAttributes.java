package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.GeneralFile;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.DoesNotExist;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsFileAttributes
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class IrodsFileAttributes extends FileAttributes {

    public IrodsFileAttributes(GeneralFile entry) throws DoesNotExist {
        // set name
        m_name = entry.getName();
        if (m_name ==null || m_name.equals(".") || m_name.equals("..")) {
            throw new DoesNotExist("Ignore this entry");
        }

        // set type        
		if (entry.isDirectory()) {
			m_type = FileAttributes.DIRECTORY_TYPE;
		} else if (entry.isFile()) {
			m_type = FileAttributes.FILE_TYPE;
		} else {
			m_type = FileAttributes.UNKNOWN_TYPE;
		}
	
        // set size
        try {
            m_size = entry.length();
        } catch(NumberFormatException e) {
            m_size = -1;
        }

        // set permission
		m_permission = PermissionBytes.NONE;
        if (entry.canRead()) {
			m_permission = m_permission.or(PermissionBytes.READ);
        }
		if (entry.canWrite()) {
			m_permission = m_permission.or(PermissionBytes.WRITE);
		}

        // set last modified
        m_lastModified = entry.lastModified();
    }
}