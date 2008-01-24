package fr.in2p3.jsaga;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permission;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JSagaURL
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JSagaURL extends URL {
    private FileAttributes m_attributes;

    public JSagaURL(FileAttributes attributes) throws NotImplemented, BadParameter, NoSuccess {
        super(attributes.getName().replaceAll(" ", "%20"));
        m_attributes = attributes;
    }

    public String getName() {
        return m_attributes.getName();
    }

    public boolean isFile() {
        return m_attributes.getType() == FileAttributes.FILE_TYPE;
    }
    public boolean isDirectory() {
        return m_attributes.getType() == FileAttributes.DIRECTORY_TYPE;
    }
    public boolean isLink() {
        return m_attributes.getType() == FileAttributes.LINK_TYPE;
    }

    public long getSize() {
        return m_attributes.getSize();
    }

    public Permission[] getPermissions() {
        List<Permission> permissions = new ArrayList<Permission>();
        if (m_attributes.getPermission().contains(Permission.QUERY)) {
            permissions.add(Permission.QUERY);
        }
        if (m_attributes.getPermission().contains(Permission.READ)) {
            permissions.add(Permission.READ);
        }
        if (m_attributes.getPermission().contains(Permission.WRITE)) {
            permissions.add(Permission.WRITE);
        }
        if (m_attributes.getPermission().contains(Permission.EXEC)) {
            permissions.add(Permission.EXEC);
        }
        if (m_attributes.getPermission().contains(Permission.OWNER)) {
            permissions.add(Permission.OWNER);
        }
        return permissions.toArray(new Permission[permissions.size()]);
    }

    public String getOwner() {
        return m_attributes.getOwner();
    }

    public Date getLastModified() {
        return new Date(m_attributes.getLastModified());
    }

    public String getLongFormat() {
        return m_attributes.toString();
    }
}
