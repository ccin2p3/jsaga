package fr.in2p3.jsaga.adaptor.data.http;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.helpers.EntryPath;
import org.ogf.saga.error.NoSuccessException;

import java.io.IOException;
import java.net.URLConnection;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpFileAttributesDefault
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpFileAttributesDefault extends FileAttributes {
    private URLConnection m_cnx;

    public HttpFileAttributesDefault(URLConnection cnx) throws NoSuccessException {
        m_cnx = cnx;
    }

    public String getName() {
        String path = m_cnx.getURL().getPath();
        return new EntryPath(path).getEntryName();
    }

    public int getType() {
//        String path = m_cnx.getURL().getPath();
//        return path.endsWith("/") ? TYPE_DIRECTORY : TYPE_FILE;
        return m_cnx.getLastModified()==0 ? TYPE_DIRECTORY : TYPE_FILE;
    }

    public long getSize() {
        return m_cnx.getContentLength();
    }

    public PermissionBytes getUserPermission() {
        try {
            if (m_cnx.getPermission().getActions().contains("connect")) {
                return PermissionBytes.READ;
            } else {
                return PermissionBytes.NONE;
            }
        } catch (IOException e) {
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
        return m_cnx.getLastModified();
    }
}
