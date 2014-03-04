package fr.in2p3.jsaga.adaptor.job.control.advanced;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HoldableJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface HoldableJobAdaptor extends JobControlAdaptor {
    /**
     * hold a job in queue
     * @param nativeJobId the identifier of the job in the grid
     * @return true if the job has been successfully held, false if it was not queued
     */
    public boolean hold(String nativeJobId) throws IncorrectStateException, PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * release a held job
     * @param nativeJobId the identifier of the job in the grid
     * @return true if the job has been successfully released, false if it was not held
     */
    public boolean release(String nativeJobId) throws IncorrectStateException, PermissionDeniedException, TimeoutException, NoSuccessException;
}
