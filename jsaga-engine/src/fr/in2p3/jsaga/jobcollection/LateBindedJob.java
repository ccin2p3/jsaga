package fr.in2p3.jsaga.jobcollection;

import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LateBindedJob
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LateBindedJob extends Job {
    /**
     * Allocate the resource <code>rm</code> to this job
     * @param rm the resource to allocate
     */
    public void allocate(Resource rm)
        throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess;
}
