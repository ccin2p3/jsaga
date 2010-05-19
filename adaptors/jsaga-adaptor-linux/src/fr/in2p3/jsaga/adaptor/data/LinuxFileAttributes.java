package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.commons.filesystem.FileStat;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LinuxFileAttributes
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   19 mai 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LinuxFileAttributes extends FileAttributes {
    private FileStat m_stat;

    public LinuxFileAttributes(FileStat stat) {
        m_stat = stat;
    }

    public String getName() {
        return m_stat.name;
    }

    public int getType() {
        if (m_stat.isdir) {
            return TYPE_DIRECTORY;
        } else if (m_stat.isfile) {
            return TYPE_FILE;
        } else if (m_stat.islink) {
            return TYPE_LINK;
        } else {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        return m_stat.size;
    }

    public PermissionBytes getUserPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if(FileStat.isReadable(m_stat.user_perms)) perms=perms.or(PermissionBytes.READ);
        if(FileStat.isWritable(m_stat.user_perms)) perms=perms.or(PermissionBytes.WRITE);
        if(FileStat.isExecutable(m_stat.user_perms)) perms=perms.or(PermissionBytes.EXEC);
        return perms;
    }

    public PermissionBytes getGroupPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if(FileStat.isReadable(m_stat.group_perms)) perms=perms.or(PermissionBytes.READ);
        if(FileStat.isWritable(m_stat.group_perms)) perms=perms.or(PermissionBytes.WRITE);
        if(FileStat.isExecutable(m_stat.group_perms)) perms=perms.or(PermissionBytes.EXEC);
        return perms;
    }

    public PermissionBytes getAnyPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if(FileStat.isReadable(m_stat.other_perms)) perms=perms.or(PermissionBytes.READ);
        if(FileStat.isWritable(m_stat.other_perms)) perms=perms.or(PermissionBytes.WRITE);
        if(FileStat.isExecutable(m_stat.other_perms)) perms=perms.or(PermissionBytes.EXEC);
        return perms;
    }

    public String getOwner() {
        return m_stat.owner;
    }

    public String getGroup() {
        return m_stat.group;
    }

    public long getLastModified() {
        return m_stat.getModifiedDate();
    }
}
