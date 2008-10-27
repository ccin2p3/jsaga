package fr.in2p3.jsaga.adaptor.data.cache;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.url.URL;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.permissions.Permission;

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
    public CacheFileAttributes(NSEntry entry) {
        m_name = getName(entry);

        m_type = isDir(entry)
                ? FileAttributes.DIRECTORY_TYPE
                : FileAttributes.FILE_TYPE;

        m_size = getSize(entry);

        String owner = getOwner(entry);
        m_permission = PermissionBytes.NONE;
        if(permissionsCheck(entry, owner, Permission.READ)) {
            m_permission = m_permission.or(PermissionBytes.READ);
        }
        if(permissionsCheck(entry, owner, Permission.WRITE)) {
            m_permission = m_permission.or(PermissionBytes.WRITE);
        }

        m_lastModified = getLastModified(entry);
    }

    private static String getName(NSEntry entry) {
        try {
            URL nameUrl = entry.getName();
            String name = (nameUrl!=null ? nameUrl.toString() : "");
            if (isDir(entry)) {
                name += "/";
            }
            return name;
        } catch (Exception e) {
            return "?";
        }
    }

    private static boolean isDir(NSEntry entry) {
        try {
            return entry.isDir();
        } catch (Exception e) {
            return false;
        }
    }

    private static String getOwner(NSEntry entry) {
        try {
            return entry.getOwner();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean permissionsCheck(NSEntry entry, String owner, Permission perm) {
        try {
            return entry.permissionsCheck(owner, perm.getValue());
        } catch (Exception e) {
            return false;
        }
    }

    private static long getSize(NSEntry entry) {
        try {
            return (entry instanceof File
                    ? ((File)entry).getSize()
                    : 0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static long getLastModified(NSEntry entry) {
        try {
            Method m = entry.getClass().getMethod("getLastModified", (Class[]) null);
            return (m != null
                    ? ((Long)m.invoke(entry, (Object[]) null)).longValue()
                    : 0);
        } catch (Exception e) {
            return 0;
        }
    }
}
