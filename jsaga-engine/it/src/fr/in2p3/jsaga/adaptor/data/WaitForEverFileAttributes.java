package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverFileAttributes
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   3 avr. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WaitForEverFileAttributes extends FileAttributes {
    public String getName() {
        return "a_file_name";
    }

    public int getType() {
        return TYPE_UNKNOWN;
    }

    public long getSize() {
        return SIZE_UNKNOWN;
    }

    public PermissionBytes getUserPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getGroupPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getAnyPermission() {
        return PERMISSION_UNKNOWN;
    }

    public String getOwner() {
        return ID_UNKNOWN;
    }

    public String getGroup() {
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        return DATE_UNKNOWN;
    }
}
