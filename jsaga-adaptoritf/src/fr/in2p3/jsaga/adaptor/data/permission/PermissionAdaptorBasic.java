package fr.in2p3.jsaga.adaptor.data.permission;

import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   PermissionAdaptorBasic
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   15 avr. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface PermissionAdaptorBasic extends PermissionAdaptor {
    /**
     * Get the list of primary and secondary groups, for which the user identified by id is member.
     * @param id the identifier of the user.
     * @return array of primary and secondary groups.
     * @throws BadParameterException if the given id is unknown, or if this method is not implemented.
     */
    public String[] getGroupsOf(String id)
            throws BadParameterException, NoSuccessException;

    /**
     * Enables the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param permissions the permissions to enable.
     */
    public void permissionsAllow(String absolutePath, int scope, PermissionBytes permissions)
            throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Disables the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param permissions the permissions to disable.
     */
    public void permissionsDeny(String absolutePath, int scope, PermissionBytes permissions)
            throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
