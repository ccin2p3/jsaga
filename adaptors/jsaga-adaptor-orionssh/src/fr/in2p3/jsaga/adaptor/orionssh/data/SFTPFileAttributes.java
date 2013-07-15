package fr.in2p3.jsaga.adaptor.orionssh.data;

import com.trilead.ssh2.SFTPv3FileAttributes;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SFTPFileAttributes
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   11 avril 2008
* ***************************************************/

public class SFTPFileAttributes extends FileAttributes {
	private static final int S_IRUSR = 00400; // read by owner
	private static final int S_IWUSR = 00200; // write by owner
	private static final int S_IXUSR = 00100; // execute/search by owner

	private static final int S_IRGRP = 00040; // read by group
	private static final int S_IWGRP = 00020; // write by group
	private static final int S_IXGRP = 00010; // execute/search by group

	private static final int S_IROTH = 00004; // read by others
	private static final int S_IWOTH = 00002; // write by others
	private static final int S_IXOTH = 00001; // execute/search by others

    private String m_filename;
    private SFTPv3FileAttributes m_attrs;

	public SFTPFileAttributes(String filename, SFTPv3FileAttributes attrs) {
        m_filename = filename;
        m_attrs = attrs;
    }

    public String getName() {
        return m_filename;
    }

    public int getType() {
        if(m_attrs.isDirectory()) {
            return TYPE_DIRECTORY;
        } else if(m_attrs.isSymlink()) {
            return TYPE_LINK;
        } else if (m_attrs.isRegularFile()) {
            return TYPE_FILE;
        } else {
        	return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        return m_attrs.size;
    }

    public PermissionBytes getUserPermission() {
        return this.getPermission(S_IRUSR, S_IWUSR, S_IXUSR);
    }

    public PermissionBytes getGroupPermission() {
        return this.getPermission(S_IRGRP, S_IWGRP, S_IXGRP);
    }

    public PermissionBytes getAnyPermission() {
        return this.getPermission(S_IROTH, S_IWOTH, S_IXOTH);
    }

    private PermissionBytes getPermission(int read, int write, int exec) {
        PermissionBytes perms = PermissionBytes.NONE;
        int sftpPerms = m_attrs.permissions;
        if((sftpPerms & read) != 0)
            perms = perms.or(PermissionBytes.READ);
        if((sftpPerms & write) != 0)
            perms = perms.or(PermissionBytes.WRITE);
        if((sftpPerms & exec) != 0)
            perms = perms.or(PermissionBytes.EXEC);
        return perms;
    }

    public String getOwner() {
        return String.valueOf(m_attrs.uid);
    }

    public String getGroup() {
        return String.valueOf(m_attrs.gid);
    }

    public long getLastModified() {
        return m_attrs.mtime.longValue() * 1000;
    }
}
