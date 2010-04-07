package fr.in2p3.jsaga.adaptor.job.control.staging;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   StagingJobAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   18 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract interface StagingJobAdaptor extends JobControlAdaptor {
    /**
     * Get the URL of the directory where to copy job input/output files.
     * Protocol must be one of the supported protocols.
     * @param nativeJobId the identifier of the job in the grid
     * @return the staging directory URL, or null if the staging directory is managed by the job service.
     */
    public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Get pre-staging operations to perform before starting the job.
     * @param nativeJobId the identifier of the job in the grid
     * @return list of transfers that are not managed by the adaptor
     */
    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Get post-staging operations to perform after the job is done.
     * @param nativeJobId the identifier of the job in the grid
     * @return list of transfers that are not managed by the adaptor
     */
    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
