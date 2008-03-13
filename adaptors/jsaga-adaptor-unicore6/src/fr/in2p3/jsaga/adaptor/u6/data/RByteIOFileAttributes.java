package fr.in2p3.jsaga.adaptor.u6.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.DoesNotExist;

import com.intel.gpe.clients.api.GridFile;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Gsiftp1FileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class RByteIOFileAttributes extends FileAttributes {
   
	public RByteIOFileAttributes(GridFile file, String separator) throws DoesNotExist {
        // set name
        m_name = file.getPath().substring(file.getPath().lastIndexOf(separator)+separator.length(), file.getPath().length());
        // set type
        if (file.isDirectory()) {
            m_type = FileAttributes.DIRECTORY_TYPE;
        } else {
            m_type = FileAttributes.FILE_TYPE;
        } 

        // set size
        try {
            m_size = file.getSize();
        } catch(NumberFormatException e) {
            m_size = -1;
        }

        // set last modified
        m_lastModified = file.lastModified().getTimeInMillis();
        
        // set permission
        m_permission = PermissionBytes.NONE;
        if (file.getPermissions().getReadable()) {
            m_permission = m_permission.or(PermissionBytes.READ);
        }
        if (file.getPermissions().getWritable()) {
            m_permission = m_permission.or(PermissionBytes.WRITE);
        }
        if (file.getPermissions().getExecutable()) {
            m_permission = m_permission.or(PermissionBytes.EXEC);
        }
    }
}
