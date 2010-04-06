package fr.in2p3.jsaga.adaptor.data.cache;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.url.URL;

import java.lang.reflect.Method;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CacheFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CacheFileAttributes extends FileAttributes {
    private NSEntry m_entry;

    public CacheFileAttributes(NSEntry entry) {
        m_entry = entry;
    }

    public String getName() {
        try {
            URL nameUrl = m_entry.getName();
            String name = (nameUrl!=null ? nameUrl.toString() : "");
            if (this.getType() == TYPE_DIRECTORY) {
                name += "/";
            }
            return name;
        } catch (SagaException e) {
            return "?";
        }
    }

    public int getType() {
        try {
            if (m_entry.isDir()) {
                return TYPE_DIRECTORY;
            } else if (m_entry.isEntry()) {
                return TYPE_FILE;
            } else if (m_entry.isLink()) {
                return TYPE_LINK;
            } else {
                return TYPE_UNKNOWN;
            }
        } catch (SagaException e) {
            return TYPE_UNKNOWN;
        }
    }

    public long getSize() {
        try {
            if (m_entry instanceof File) {
                return ((File)m_entry).getSize();
            } else {
                return SIZE_UNKNOWN;
            }
        } catch (SagaException e) {
            return SIZE_UNKNOWN;
        }
    }

    public PermissionBytes getUserPermission() {
        return this.getPermission("user-"+this.getOwner());
    }

    public PermissionBytes getGroupPermission() {
        return this.getPermission("group-"+this.getGroup());
    }

    public PermissionBytes getAnyPermission() {
        return this.getPermission("*");
    }

    private PermissionBytes getPermission(String id) {
        PermissionBytes perms = PermissionBytes.NONE;
        try {
            if (m_entry.permissionsCheck(id, Permission.READ.getValue())) {
                perms = perms.or(PermissionBytes.READ);
            }
        } catch (SagaException e) {
            // do nothing
        }
        try {
            if (m_entry.permissionsCheck(id, Permission.WRITE.getValue())) {
                perms = perms.or(PermissionBytes.WRITE);
            }
        } catch (SagaException e) {
            // do nothing
        }
        return perms;
    }

    public String getOwner() {
        try {
            return m_entry.getOwner();
        } catch (SagaException e) {
            return ID_UNKNOWN;
        }
    }

    public String getGroup() {
        try {
            return m_entry.getGroup();
        } catch (SagaException e) {
            return ID_UNKNOWN;
        }
    }

    public long getLastModified() {
        try {
            Method m = m_entry.getClass().getMethod("getLastModified", (Class[]) null);
            return (m != null
                    ? ((Long)m.invoke(m_entry, (Object[]) null)).longValue()
                    : DATE_UNKNOWN);
        } catch (Exception e) {
            return DATE_UNKNOWN;
        }
    }
}
