package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
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
        return this.toString(entry);
    }

    public String toString(NSEntry entry) throws SagaException {
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

    public boolean isDir(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            return entry.isDir();
        } catch (NotImplementedException e) {
            return false;
        }
    }

    public String getOwner(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            return entry.getOwner();
        } catch (NotImplementedException e) {
            return null;
        }
    }

    public boolean permissionsCheck(NSEntry entry, String owner, Permission perm) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            return entry.permissionsCheck(owner, perm.getValue());
        } catch (NotImplementedException e) {
            return false;
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

    public Date getLastModified(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (entry instanceof AbstractNSEntryImpl) {
            try {
                return ((AbstractNSEntryImpl)entry).getLastModified();
            } catch (NotImplementedException e) {
                return new Date(0);
            }
        } else {
            return null;
        }
    }

    public String getName(NSEntry entry) throws AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            URL nameUrl = entry.getName();
            // getString() decodes the URL, while toString() does not
            String name = (nameUrl!=null ? nameUrl.getString() : "");
            if (this.isDir(entry)) {
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
