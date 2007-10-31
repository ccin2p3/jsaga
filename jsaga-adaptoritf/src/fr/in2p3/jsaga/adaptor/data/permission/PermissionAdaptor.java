package fr.in2p3.jsaga.adaptor.data.permission;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PermissionAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface PermissionAdaptor extends DataAdaptor {
    /**
     * Enables the specified permissions for the specified id.
     * An id of "*" enables the permissions for all.
     * Unsupported permission types are silently ignored.
     * @param absolutePath the absolute path of the entry.
     * @param id the id.
     * @param permissions the permissions to enable.
     */
    public void permissionsAllow(String absolutePath, String id, PermissionBytes permissions)
            throws PermissionDenied, Timeout, NoSuccess;

    /**
     * Disables the specified permissions for the specified id.
     * An id of "*" disables the permissions for all.
     * Unsupported permission types throw NotImplemented exception.
     * @param absolutePath the absolute path of the entry.
     * @param id the id.
     * @param permissions the permissions to disable.
     */
    public void permissionsDeny(String absolutePath, String id, PermissionBytes permissions)
            throws PermissionDenied, Timeout, NoSuccess;

    /**
     * Determines if the specified permissions are enabled for the
     * specified id.
     * An id of "*" queries the permissions for all.
     * Unsupported permission types are considered as always enabled.
     * @param absolutePath the absolute path of the entry.
     * @param id the id.
     * @param permissions the permissions to query.
     * @return <code>true</code> if the specified permissions are enabled
     *     for the specified id.
     */
    public boolean permissionsCheck(String absolutePath, String id, PermissionBytes permissions)
            throws PermissionDenied, Timeout, NoSuccess;

    /**
     * Gets the owner id of the entity.
     * @param absolutePath the absolute path of the entry.
     * @return the id of the owner.
     */
    public String getOwner(String absolutePath)
            throws PermissionDenied, Timeout, NoSuccess;

    /**
     * Gets the group id of the entity.
     * @param absolutePath the absolute path of the entry.
     * @return the id of the group.
     */
    public String getGroup(String absolutePath)
            throws PermissionDenied, Timeout, NoSuccess;
}
