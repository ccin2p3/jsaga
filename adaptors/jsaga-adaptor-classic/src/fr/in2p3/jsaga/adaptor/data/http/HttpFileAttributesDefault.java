package fr.in2p3.jsaga.adaptor.data.http;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.helpers.EntryPath;
import org.ogf.saga.error.NoSuccess;

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
    public HttpFileAttributesDefault(URLConnection cnx) throws NoSuccess {
        String path = cnx.getURL().getPath();
        m_name = new EntryPath(path).getEntryName();
        m_type = path.endsWith("/") ? DIRECTORY_TYPE : FILE_TYPE;

        m_size = cnx.getContentLength();

        try {
            if (cnx.getPermission().getActions().contains("connect")) {
                m_permission = PermissionBytes.READ;
            } else {
                m_permission = PermissionBytes.NONE;
            }
        } catch (IOException e) {
            throw new NoSuccess(e);
        }

        m_lastModified = cnx.getLastModified();
    }
}
