package fr.in2p3.jsaga.adaptor.data.http_socket;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.helpers.EntryPath;
import org.ogf.saga.error.NoSuccess;

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
    public HttpFileAttributesSockedBased(String path, HttpRequest request) throws NoSuccess {
        m_name = new EntryPath(path).getEntryName();
        m_type = path.endsWith("/") ? DIRECTORY_TYPE : FILE_TYPE;

        m_size = request.getContentLength();

        if (request.getStatus().contains("OK")) {
            m_permission = PermissionBytes.READ;
        } else {
            m_permission = PermissionBytes.NONE;
        }

        m_lastModified = request.getLastModified().getTime();
    }
}
