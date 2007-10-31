package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.FileInfo;
import org.ogf.saga.error.DoesNotExist;

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
    public Gsiftp1FileAttributes(FileInfo entry) throws DoesNotExist {
        // set name
        m_name = entry.getName();
        if (m_name ==null || m_name.equals(".") || m_name.equals("..")) {
            throw new DoesNotExist("Ignore this entry");
        }

        // set type
        if (entry.isFile()) {
            m_type = FileAttributes.FILE_TYPE;
        } else if (entry.isDirectory()) {
            m_type = FileAttributes.DIRECTORY_TYPE;
        } else if (entry.isSoftLink()) {
            m_type = FileAttributes.LINK_TYPE;
        } else {
            m_type = FileAttributes.UNKNOWN_TYPE;
        }

        // set size
        try {
            m_size = entry.getSize();
        } catch(NumberFormatException e) {
            m_size = -1;
        }

        // set permission
        m_permission = PermissionBytes.NONE;
        if (entry.userCanRead() || entry.allCanRead()) {
            m_permission = m_permission.or(PermissionBytes.READ);
        }
        if (entry.userCanWrite() || entry.allCanWrite()) {
            m_permission = m_permission.or(PermissionBytes.WRITE);
        }
        if (entry.userCanExecute() || entry.allCanExecute()) {
            m_permission = m_permission.or(PermissionBytes.EXEC);
        }

        // set last modified
        try {
            switch (entry.getTime().length()) {
                case 4:
                {
                    SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);
                    Date date = format.parse(entry.getDate()+","+entry.getTime());
                    m_lastModified = date.getTime();
                    break;
                }
                case 5:
                {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, -6);
                    String year6MonthAgo = ""+cal.get(Calendar.YEAR);
                    SimpleDateFormat format = new SimpleDateFormat("MMM dd,hh:mm,yyyy", Locale.ENGLISH);
                    Date date = format.parse(entry.getDate()+","+entry.getTime()+","+year6MonthAgo);
                    m_lastModified = date.getTime();
                    break;
                }
                default:
                    m_lastModified = 0;
                    break;
            }
        } catch (ParseException e) {
            m_lastModified = 0;
        }
    }
}
