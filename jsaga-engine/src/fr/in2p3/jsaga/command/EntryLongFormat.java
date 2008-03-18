package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.*;
import org.ogf.saga.permissions.Permission;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EntryLongFormat
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   6 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EntryLongFormat {
    private NSDirectory m_parentDir;

    public EntryLongFormat(NSDirectory parentDir) {
        m_parentDir = parentDir;
    }

    public String toString(URL url) throws org.ogf.saga.error.Exception {
        NSEntry entry = m_parentDir.open(url, Flags.NONE.getValue());
        return this.toString(entry);
    }

    public String toString(NSEntry entry) throws org.ogf.saga.error.Exception {
        String owner = this.getOwner(entry);
        StringBuffer buf = new StringBuffer();
        buf.append(this.isDir(entry) ? 'd' : '-');
        buf.append(this.permissionsCheck(entry, owner, Permission.READ) ? 'r' : '-');
        buf.append(this.permissionsCheck(entry, owner, Permission.WRITE) ? 'w' : '-');
        buf.append(this.permissionsCheck(entry, owner, Permission.EXEC) ? 'x' : '-');
        buf.append(' ');
        if (owner != null) {
            buf.append(format(owner, 8));
            buf.append(' ');
        }
        buf.append(format(this.getSize(entry), 10));
        buf.append(' ');
        final String FORMAT = "MMM-dd-yyyy HH:mm";
        Date lastModified = this.getLastModified(entry);
        if (lastModified != null) {
            buf.append(new SimpleDateFormat(FORMAT, Locale.ENGLISH).format(lastModified));
        } else {
            buf.append(format("?", FORMAT.length()));
        }
        buf.append(' ');
        buf.append(this.getName(entry));
        return buf.toString();
    }

    public boolean isDir(NSEntry entry) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return entry.isDir();
        } catch (NotImplemented e) {
            return false;
        }
    }

    public String getOwner(NSEntry entry) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        try {
            return entry.getOwner();
        } catch (NotImplemented e) {
            return null;
        }
    }

    public boolean permissionsCheck(NSEntry entry, String owner, Permission perm) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        try {
            return entry.permissionsCheck(owner, perm.getValue());
        } catch (NotImplemented e) {
            return false;
        }
    }

    public String getSize(NSEntry entry) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (entry instanceof File) {
            try {
                return ""+((File)entry).getSize();
            } catch (NotImplemented e) {
                return "?";
            }
        } else {
            return "0";
        }
    }

    public Date getLastModified(NSEntry entry) {
        if (entry instanceof AbstractNSEntryImpl) {
            try {
                return ((AbstractNSEntryImpl)entry).getLastModified();
            } catch (NotImplemented e) {
                return new Date(0);
            }
        } else {
            return null;
        }
    }

    public String getName(NSEntry entry) throws AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            URL nameUrl = entry.getName();
            String name = (nameUrl!=null ? JSagaURL.decode(nameUrl) : "");
            if (this.isDir(entry)) {
                name += "/";
            }
            return name;
        } catch (NotImplemented e) {
            return "?";
        }
    }

    private static StringBuffer format(String value, int length) {
        StringBuffer buf = new StringBuffer();
        for (int i=value.length(); i<length; i++) {
            buf.append(' ');
        }
        buf.append(value);
        return buf;
    }
}
