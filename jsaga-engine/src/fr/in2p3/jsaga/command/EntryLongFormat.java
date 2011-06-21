package fr.in2p3.jsaga.command;

import org.ogf.saga.error.*;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.*;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.url.URL;

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

    public String toString(URL url) throws SagaException {
        NSEntry entry = m_parentDir.open(url, Flags.NONE.getValue());
        String entry_str = this.toString(entry);
        entry.close();
        return entry_str;
    }

    public String toString(NSEntry entry) throws SagaException {
        String owner = this.getOwner(entry);
        String group = this.getGroup(entry);

        StringBuffer buf = new StringBuffer();
        buf.append(this.isDir(entry));
        if (owner != null) {
            buf.append(this.getPermissions(entry, "user-"+owner));
        } else {
            buf.append("???");
        }
        if (group != null) {
            buf.append(this.getPermissions(entry, "group-"+group));
        } else {
            buf.append("???");
        }
        buf.append(this.getPermissions(entry, "*"));
        buf.append(' ');
        if (owner != null) {
            if (owner.startsWith("/")) {
                buf.append("'");
                buf.append(owner.substring(owner.lastIndexOf("/CN=")+4));
                buf.append("'");
            } else {
                buf.append(format(owner, 8));
            }
            buf.append(' ');
        }
        if (group != null) {
            buf.append(format(group, 8));
            buf.append(' ');
        }
        buf.append(format(this.getSize(entry), 10));
        buf.append(' ');
        final String FORMAT = "MMM-dd-yyyy HH:mm";
        Date lastModified = this.getMTime(entry);
        if (lastModified != null) {
            buf.append(new SimpleDateFormat(FORMAT, Locale.ENGLISH).format(lastModified));
        } else {
            buf.append(format("?", FORMAT.length()));
        }
        buf.append(' ');
        buf.append(this.getName(entry));
        return buf.toString();
    }

    public char isDir(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            if (entry.isDir()) {
                return 'd';
            } else {
                return '-';
            }
        } catch (NotImplementedException e) {
            return '?';
        }
    }

    public String getOwner(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            return entry.getOwner();
        } catch (NotImplementedException e) {
            return null;
        }
    }

    public String getGroup(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            return entry.getGroup();
        } catch (NotImplementedException e) {
            return null;
        }
    }

    public String getPermissions(NSEntry entry, String id) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        StringBuffer buf = new StringBuffer();
        buf.append(this.permissionsCheck(entry, id, Permission.READ, 'r'));
        buf.append(this.permissionsCheck(entry, id, Permission.WRITE, 'w'));
        buf.append(this.permissionsCheck(entry, id, Permission.EXEC, 'x'));
        return buf.toString();
    }
    public char permissionsCheck(NSEntry entry, String id, Permission perm, char c) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            if (entry.permissionsCheck(id, perm.getValue())) {
                return c;
            } else {
                return '-';
            }
        } catch (BadParameterException e) {
            return '?';
        } catch (NotImplementedException e) {
            return '?';
        }
    }

    public String getSize(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (entry instanceof File) {
            try {
                return ""+((File)entry).getSize();
            } catch (NotImplementedException e) {
                return "?";
            }
        } else {
            return "0";
        }
    }

    public Date getMTime(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            return new Date(entry.getMTime());
        } catch (NotImplementedException e) {
            return new Date(0);
        }
    }

    public String getName(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            URL nameUrl = entry.getName();
            // getString() decodes the URL, while toString() does not
            String name = (nameUrl!=null ? nameUrl.getString() : "");
            if (entry.isDir()) {
                name += "/";
            }
            return name;
        } catch (NotImplementedException e) {
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
