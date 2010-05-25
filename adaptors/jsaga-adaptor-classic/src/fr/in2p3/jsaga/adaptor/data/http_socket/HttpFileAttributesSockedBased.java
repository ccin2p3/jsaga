package fr.in2p3.jsaga.adaptor.data.http_socket;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.helpers.EntryPath;
import org.ogf.saga.error.NoSuccessException;

import java.util.Date;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpFileAttributesSockedBased
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpFileAttributesSockedBased extends FileAttributes {
    private String m_path;
    private HttpRequest m_request;

    public HttpFileAttributesSockedBased(String path, HttpRequest request) {
        m_path = path;
        m_request = request;
    }

    public String getName() {
        return new EntryPath(m_path).getEntryName();
    }

    public int getType() {
//        return m_path.endsWith("/") ? TYPE_DIRECTORY : TYPE_FILE;
        try {
            return m_request.getLastModified()==null ? TYPE_DIRECTORY : TYPE_FILE;
        } catch (NoSuccessException e) {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        return m_request.getContentLength();
    }

    public PermissionBytes getUserPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getGroupPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getAnyPermission() {
        if (m_request.getStatus().contains("OK")) {
            return PermissionBytes.READ;
        } else {
            return PermissionBytes.NONE;
        }
    }

    public String getOwner() {
        return ID_UNKNOWN;
    }

    public String getGroup() {
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        try {
            Date lastModified = m_request.getLastModified();
            if (lastModified != null) {
                return lastModified.getTime();
            } else {
                return DATE_UNKNOWN;
            }
        } catch (NoSuccessException e) {
            return DATE_UNKNOWN;
        }
    }
}
