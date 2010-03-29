package fr.in2p3.jsaga.adaptor.job.control.manage;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PurgeableJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface PurgeableJobAdaptor extends JobControlAdaptor {
    /**
     * Purge the list of jobs that are currently known to the resource manager.
     */
    public void purge() throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Purge the list of jobs included in nativeJobIdArray.
     * @param nativeJobIdArray a list of job identifications.
     */
//    public void purge(String[] nativeJobIdArray) throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
