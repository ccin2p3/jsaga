package fr.in2p3.jsaga.adaptor.data.permission;

import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   PermissionAdaptorFull
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   15 avr. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface PermissionAdaptorFull extends PermissionAdaptor {
    /**
     * Enables the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param permissions the permissions to enable.
     * @param id the identifier.
     * @throws BadParameterException if the given id is unknown or not supported
     */
    public void permissionsAllow(String absolutePath, int scope, PermissionBytes permissions, String id)
            throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException;

    /**
     * Disables the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param permissions the permissions to disable.
     * @param id the identifier.
     * @throws BadParameterException if the given id is unknown or not supported
     */
    public void permissionsDeny(String absolutePath, int scope, PermissionBytes permissions, String id)
            throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException;

    /**
     * Checks the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param permissions the permissions to check.
     * @param id the identifier.
     * @return true if all permissions are set for id.
     * @throws BadParameterException if the given id is unknown or not supported
     */
    public boolean permissionsCheck(String absolutePath, int scope, PermissionBytes permissions, String id)
            throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException;
}
