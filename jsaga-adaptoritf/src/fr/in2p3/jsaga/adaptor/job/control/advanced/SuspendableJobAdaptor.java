package fr.in2p3.jsaga.adaptor.job.control.advanced;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SuspendableJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SuspendableJobAdaptor extends JobControlAdaptor {
    /**
     * suspend an active job
     * @param nativeJobId the identifier of the job in the grid
     * @return true if the job has been successfully suspended, false if it was not active
     */
    public boolean suspend(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * resume a suspended job
     * @param nativeJobId the identifier of the job in the grid
     * @return true if the job has been successfully resumed, false if it was not suspended
     */
    public boolean resume(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
