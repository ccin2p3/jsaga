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
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_DIRECTORY = 2;
    public static final int TYPE_LINK = 3;

    public static final long SIZE_UNKNOWN = -1;
    public static final long DATE_UNKNOWN = 0;
    public static final PermissionBytes PERMISSION_UNKNOWN = null;
    public static final String ID_UNKNOWN = null;

    /** should be set only by method findAttributes() */
    protected String m_relativePath = null;

    /**
     * @return the relative path
     */
    public final String getRelativePath() {
        String relativePath = (m_relativePath!=null ? m_relativePath : this.getName());
        switch(this.getType()) {
            case TYPE_DIRECTORY:
                return relativePath+"/";
            default:
                return relativePath;
        }
    }

    /**
     * @return the name of entry (no default value)
     */
    public abstract String getName();

    /**
     * @return the type of entry (or TYPE_UNKNOWN)
     */
    public abstract int getType();

    /**
     * This method is invoked only on entries of type File.
     * @return the size of entry (or SIZE_UNKNOWN)
     */
    public abstract long getSize();

    /**
     * @return the permissions of entry for current user (or PERMISSION_UNKNOWN)
     */
    public abstract PermissionBytes getUserPermission();

    /**
     * @return the permissions of entry for group of current user (or PERMISSION_UNKNOWN)
     */
    public abstract PermissionBytes getGroupPermission();

    /**
     * @return the permissions of entry for any (or PERMISSION_UNKNOWN)
     */
    public abstract PermissionBytes getAnyPermission();

    /**
     * @return the owner of entry (or ID_UNKNOWN)
     */
    public abstract String getOwner();

    /**
     * @return the group of entry (or ID_UNKNOWN)
     */
    public abstract String getGroup();

    /**
     * @return the last modified date of entry (or DATE_UNKNOWN)
     */
    public abstract long getLastModified();
}
