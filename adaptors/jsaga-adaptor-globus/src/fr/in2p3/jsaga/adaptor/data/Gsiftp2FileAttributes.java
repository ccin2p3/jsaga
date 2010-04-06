package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.MlsxEntry;
import org.ogf.saga.error.DoesNotExistException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Gsiftp2FileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class Gsiftp2FileAttributes extends FileAttributes {
    private static final int POSITION_USER = 1;
    private static final int POSITION_GROUP = 2;
    private static final int POSITION_ANY = 3;

    private static final int UNIX_READ = 4;
    private static final int UNIX_WRITE = 2;
    private static final int UNIX_EXEC = 1;

    private MlsxEntry m_entry;

    public Gsiftp2FileAttributes(MlsxEntry entry) throws DoesNotExistException {
        // check if entry must be ignored
        String name = entry.getFileName();
        if (name ==null || name.equals(".") || name.equals("..")) {
            throw new DoesNotExistException("Ignore this entry");
        }

        // set entry
        m_entry = entry;
    }

    public String getName() {
        return m_entry.getFileName();
    }

    public int getType() {
        String _type = m_entry.get("type");
        if (_type==null) return TYPE_UNKNOWN;
        if (_type.equals("file")) {
            return TYPE_FILE;
        } else if (_type.endsWith("dir")) {
            return TYPE_DIRECTORY;
        } else {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        String _size = m_entry.get("size");
        if (_size==null) return SIZE_UNKNOWN;
        try {
            return Long.parseLong(m_entry.get("size"));
        } catch(NumberFormatException e) {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        return this.getPermission(POSITION_USER);
    }

    public PermissionBytes getGroupPermission() {
        return this.getPermission(POSITION_GROUP);
    }

    public PermissionBytes getAnyPermission() {
        return this.getPermission(POSITION_ANY);
    }

    private PermissionBytes getPermission(int position) {
        String _perm = m_entry.get("unix.mode");
        if (_perm==null) return PERMISSION_UNKNOWN;
        PermissionBytes perms = PermissionBytes.NONE;
        int userPerm = _perm.charAt(position) - '0';
        if ((userPerm & UNIX_READ) != 0) {
            perms = perms.or(PermissionBytes.READ);
        }
        if ((userPerm & UNIX_WRITE) != 0) {
            perms = perms.or(PermissionBytes.WRITE);
        }
        if ((userPerm & UNIX_EXEC) != 0) {
            perms = perms.or(PermissionBytes.EXEC);
        }
        return perms;
    }

    public String getOwner() {
        return ID_UNKNOWN;
    }

    public String getGroup() {
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        String _date = m_entry.get("modify");
        if (_date==null) return DATE_UNKNOWN;
        try {
            Date date = new SimpleDateFormat("yyyyMMddhhmmss", Locale.ENGLISH).parse(_date);
            return date.getTime()+7200000;
        } catch (ParseException e) {
            return DATE_UNKNOWN;
        }
    }
}
