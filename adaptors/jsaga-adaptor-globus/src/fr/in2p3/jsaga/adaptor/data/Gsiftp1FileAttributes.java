package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.FileInfo;
import org.ogf.saga.error.DoesNotExistException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Gsiftp1FileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class Gsiftp1FileAttributes extends FileAttributes {
    private FileInfo m_entry;

    public Gsiftp1FileAttributes(FileInfo entry) throws DoesNotExistException {
        // check if entry must be ignored
        String name = entry.getName();
        if (name ==null || name.equals(".") || name.equals("..")) {
            throw new DoesNotExistException("Ignore this entry");
        }

        // set entry
        m_entry = entry;
    }

    public String getName() {
        return m_entry.getName();
    }

    public int getType() {
        if (m_entry.isFile()) {
            return FileAttributes.TYPE_FILE;
        } else if (m_entry.isDirectory()) {
            return FileAttributes.TYPE_DIRECTORY;
        } else if (m_entry.isSoftLink()) {
            return FileAttributes.TYPE_LINK;
        } else {
            return FileAttributes.TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        try {
            return m_entry.getSize();
        } catch(NumberFormatException e) {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if (m_entry.userCanRead()) {
            perms = perms.or(PermissionBytes.READ);
        }
        if (m_entry.userCanWrite()) {
            perms = perms.or(PermissionBytes.WRITE);
        }
        if (m_entry.userCanExecute()) {
            perms = perms.or(PermissionBytes.EXEC);
        }
        return perms;
    }

    public PermissionBytes getGroupPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if (m_entry.groupCanRead()) {
            perms = perms.or(PermissionBytes.READ);
        }
        if (m_entry.groupCanWrite()) {
            perms = perms.or(PermissionBytes.WRITE);
        }
        if (m_entry.groupCanExecute()) {
            perms = perms.or(PermissionBytes.EXEC);
        }
        return perms;
    }

    public PermissionBytes getAnyPermission() {
        PermissionBytes perms = PermissionBytes.NONE;
        if (m_entry.allCanRead()) {
            perms = perms.or(PermissionBytes.READ);
        }
        if (m_entry.allCanWrite()) {
            perms = perms.or(PermissionBytes.WRITE);
        }
        if (m_entry.allCanExecute()) {
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
        try {
            switch (m_entry.getTime().length()) {
                case 4:
                {
                    SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
                    Date date = format.parse(m_entry.getDate()+","+m_entry.getTime());
                    return date.getTime();
                }
                case 5:
                {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, -6);
                    String year6MonthAgo = ""+cal.get(Calendar.YEAR);
                    SimpleDateFormat format = new SimpleDateFormat("MMM dd,hh:mm,yyyy", Locale.ENGLISH);
                    Date date = format.parse(m_entry.getDate()+","+m_entry.getTime()+","+year6MonthAgo);
                    return date.getTime();
                }
                default:
                    return DATE_UNKNOWN;
            }
        } catch (ParseException e) {
            return DATE_UNKNOWN;
        }
    }
}
