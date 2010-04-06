package fr.in2p3.jsaga.adaptor.u6.data;

import com.intel.gpe.clients.api.GridFile;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.DoesNotExistException;


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
    private GridFile m_file;
    private String m_separator;
   
	public RByteIOFileAttributes(GridFile file, String separator) throws DoesNotExistException {
        m_file = file;
        m_separator = separator;
    }

    public String getName() {
        String path = m_file.getPath();
        return path.substring(
                path.lastIndexOf(m_separator)+m_separator.length(),
                path.length());
    }

    public int getType() {
        if (m_file.isDirectory()) {
            return TYPE_DIRECTORY;
        } else {
            return TYPE_FILE;
        }
    }

    public long getSize() {
        try {
            return m_file.getSize();
        } catch(NumberFormatException e) {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        if (m_file.getPermissions() != null) {
            PermissionBytes perms = PermissionBytes.NONE;
            if (m_file.getPermissions().getReadable()) {
                perms = perms.or(PermissionBytes.READ);
            }
            if (m_file.getPermissions().getWritable()) {
                perms = perms.or(PermissionBytes.WRITE);
            }
            if (m_file.getPermissions().getExecutable()) {
                perms = perms.or(PermissionBytes.EXEC);
            }
            return perms;
        } else {
            return PERMISSION_UNKNOWN;
        }
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
        if (m_file.lastModified() != null) {
            return m_file.lastModified().getTimeInMillis();
        } else {
            return DATE_UNKNOWN;
        }
    }
}
