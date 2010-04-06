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
    public static final int SCOPE_USER = 0;
    public static final int SCOPE_GROUP = 1;
    public static final int SCOPE_ANY = 2;

    /**
     * Get the list of supported scopes.
     * @return array of scopes.
     */
    public int[] getSupportedScopes();

    /**
     * Enables the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param id the identifier.
     * @param permissions the permissions to enable.
     */
    public void permissionsAllow(String absolutePath, int scope, String id, PermissionBytes permissions)
            throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Disables the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param id the identifier.
     * @param permissions the permissions to disable.
     */
    public void permissionsDeny(String absolutePath, int scope, String id, PermissionBytes permissions)
            throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Checks the specified permissions for the specified identifier and scope.
     * @param absolutePath the absolute path of the entry.
     * @param scope the scope of permissions (USER, GROUP or ANY).
     * @param id the identifier.
     * @param permissions the permissions to check.
     * @return true if all permissions are set for id.
     */
    public boolean permissionsCheck(String absolutePath, int scope, String id, PermissionBytes permissions)
            throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
