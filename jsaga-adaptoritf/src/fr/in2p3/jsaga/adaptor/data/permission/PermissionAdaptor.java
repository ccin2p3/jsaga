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
public abstract interface PermissionAdaptor extends DataAdaptor {
    public static final int SCOPE_USER = 0;
    public static final int SCOPE_GROUP = 1;
    public static final int SCOPE_ANY = 2;

    /**
     * Get the list of supported scopes.
     * @return array of scopes.
     */
    public int[] getSupportedScopes();

    /**
     * Change owner of the entry.
     * @param id the identifier of the new owner.
     * @throws BadParameterException if the given id is unknown or if changing owner is not supported
     */
    public void setOwner(String id)
            throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException;

    /**
     * Change group of the entry.
     * @param id the identifier of the new group.
     * @throws BadParameterException if the given id is unknown or if changing group is not supported
     */
    public void setGroup(String id)
            throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException;
}
