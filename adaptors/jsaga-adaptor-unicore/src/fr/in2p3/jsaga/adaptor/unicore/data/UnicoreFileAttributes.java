package fr.in2p3.jsaga.adaptor.unicore.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.DoesNotExistException;
import org.unigrids.services.atomic.types.GridFileType;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreFileAttributes
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   22 aout 2011
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UnicoreFileAttributes extends FileAttributes {
    private GridFileType m_gft;
    private String m_separator;
   
	public UnicoreFileAttributes(GridFileType gft, String separator) throws DoesNotExistException {
        m_gft = gft;
        m_separator = separator;
    }

    public String getName() {
        String path = m_gft.getPath();
        return path.substring(
                path.lastIndexOf(m_separator)+m_separator.length(),
                path.length());
    }

    public int getType() {
        if (m_gft.getIsDirectory()) {
            return TYPE_DIRECTORY;
        } else {
            return TYPE_FILE;
        }
    }

    public long getSize() {
        try {
            return m_gft.getSize();
        } catch(NumberFormatException e) {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        if (m_gft.getPermissions() != null) {
            PermissionBytes perms = PermissionBytes.NONE;
            if (m_gft.getPermissions().getReadable()) {
                perms = perms.or(PermissionBytes.READ);
            }
            if (m_gft.getPermissions().getWritable()) {
                perms = perms.or(PermissionBytes.WRITE);
            }
            if (m_gft.getPermissions().getExecutable()) {
                perms = perms.or(PermissionBytes.EXEC);
            }
            return perms;
        } else {
            return PERMISSION_UNKNOWN;
        }
    }

    public PermissionBytes getGroupPermission() {
    	// TODO
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getAnyPermission() {
    	// TODO
        return PERMISSION_UNKNOWN;
    }

    public String getOwner() {
    	// TODO
        return ID_UNKNOWN;
    }

    public String getGroup() {
    	// TODO
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        if (m_gft.getLastModified() != null) {
            return m_gft.getLastModified().getTimeInMillis();
        } else {
            return DATE_UNKNOWN;
        }
    }
}
