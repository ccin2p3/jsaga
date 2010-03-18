package fr.in2p3.jsaga.adaptor.job.control.staging;

import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   StagingJobAdaptorTwoPhase
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   18 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface StagingJobAdaptorTwoPhase extends StagingJobAdaptor {
    /**
     * Start the job registered by the submit method.
     * @param nativeJobId the identifier of the job in the grid
     */
    public void start(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
