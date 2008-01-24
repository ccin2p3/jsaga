package fr.in2p3.jsaga.adaptor.data.read;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import org.ogf.saga.permissions.Permission;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    protected String m_name = null;
    protected int m_type = UNKNOWN_TYPE;
    protected long m_size = -1;
    protected PermissionBytes m_permission = PermissionBytes.UNKNOWN;
    protected String m_owner = null;
    protected long m_lastModified = 0;

    public String getName() {
        switch(m_type) {
            case DIRECTORY_TYPE:
                return m_name+"/";
            default:
                return m_name;
        }
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

    public long getLastModified() {
        return m_lastModified;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(m_type ==DIRECTORY_TYPE ? 'd' : '-');
        buf.append(m_permission.contains(Permission.READ) ? 'r' : '-');
        buf.append(m_permission.contains(Permission.WRITE) ? 'w' : '-');
        buf.append(m_permission.contains(Permission.EXEC) ? 'x' : '-');
        buf.append(' ');
        if (m_owner != null) {
            buf.append(format(m_owner, 8));
            buf.append(' ');
        }
        buf.append(format(""+m_size, 10));
        buf.append(' ');
        buf.append(new SimpleDateFormat("MMM-dd-yyyy HH:mm", Locale.ENGLISH).format(new Date(m_lastModified)));
        buf.append(' ');
        buf.append(this.getName());
        return buf.toString();
    }

    private StringBuffer format(String value, int length) {
        StringBuffer buf = new StringBuffer();
        for (int i=value.length(); i<length; i++) {
            buf.append(' ');
        }
        buf.append(value);
        return buf;
    }
}
