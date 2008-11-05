package fr.in2p3.jsaga.adaptor.data.read;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class FileAttributes {
    public static final int UNKNOWN_TYPE = 0;
    public static final int FILE_TYPE = 1;
    public static final int DIRECTORY_TYPE = 2;
    public static final int LINK_TYPE = 3;

    protected String m_relativePath = null;
    protected String m_name = null;
    protected int m_type = UNKNOWN_TYPE;
    protected long m_size = -1;
    protected PermissionBytes m_permission = PermissionBytes.UNKNOWN;
    protected String m_owner = null;
    protected String m_group = null;
    protected long m_lastModified = 0;

    public String getName() {
        String name = (m_relativePath!=null ? m_relativePath : m_name);
        switch(m_type) {
            case DIRECTORY_TYPE:
                return name+"/";
            default:
                return name;
        }
    }

    public String getNameOnly() {
        return m_name;
    }

    public int getType() {
        return m_type;
    }

    public long getSize() {
        return m_size;
    }

    public PermissionBytes getPermission() {
        return m_permission;
    }

    public String getOwner() {
        return m_owner;
    }

    public String getGroup() {
        return m_group;
    }

    public long getLastModified() {
        return m_lastModified;
    }
}
