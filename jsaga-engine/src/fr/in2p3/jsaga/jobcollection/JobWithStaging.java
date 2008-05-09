package fr.in2p3.jsaga.jobcollection;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobWithStaging
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobWithStaging extends LateBindedJob {
    /**
     * Get the job wrapper script embedding the user job.
     * @return the job wrapper script
     */
    public String getWrapper()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, NoSuccess;
}
