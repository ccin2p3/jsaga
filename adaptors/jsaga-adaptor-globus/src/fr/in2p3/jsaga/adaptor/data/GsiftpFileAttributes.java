package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.MlsxEntry;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.permissions.Permission;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpFileAttributes extends FileAttributes {
    private static final int UNIX_READ = 4;
    private static final int UNIX_WRITE = 2;
    private static final int UNIX_EXEC = 1;

    public GsiftpFileAttributes(MlsxEntry entry) throws DoesNotExist {
        // set name
        m_name = entry.getFileName();
        if (m_name ==null || m_name.equals(".") || m_name.equals("..")) {
            throw new DoesNotExist("Ignore this entry");
        }

        // set type
        String _type = entry.get("type");
        if (_type != null) {
            if (_type.equals("file")) {
                m_type = FileAttributes.FILE_TYPE;
            } else if (_type.endsWith("dir")) {
                m_type = FileAttributes.DIRECTORY_TYPE;
            } else {
                m_type = FileAttributes.UNKNOWN_TYPE;
            }
        }

        // set size
        try {
            m_size = Long.parseLong(entry.get("size"));
        } catch(NumberFormatException e) {
            m_size = -1;
        }

        // set permission
        String _perm = entry.get("unix.mode");
        if (_perm != null) {
            m_permission = Permission.NONE;
            int userPerm = _perm.charAt(1) - '0';
            if ((userPerm & UNIX_READ) != 0) {
                m_permission = m_permission.or(Permission.READ);
            }
            if ((userPerm & UNIX_WRITE) != 0) {
                m_permission = m_permission.or(Permission.WRITE);
            }
            if ((userPerm & UNIX_EXEC) != 0) {
                m_permission = m_permission.or(Permission.EXEC);
            }
        }

        // set last modified
        try {
            Date date = new SimpleDateFormat("yyyyMMddhhmmss", Locale.ENGLISH).parse(entry.get("modify"));
            m_lastModified = date.getTime()+7200000;
        } catch (ParseException e) {
            m_lastModified = 0;
        }
    }
}
