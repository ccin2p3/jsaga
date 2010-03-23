package fr.in2p3.jsaga.adaptor.job.control.staging;

import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   StagingJobAdaptorOnePhase
 * Author: sreynaud (sreynaud@in2p3.fr)
 * Date:   18 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface StagingJobAdaptorOnePhase extends StagingJobAdaptor {
    /**
     * Get the URL of the directory where to copy job input/output files.
     * Protocol must be one of the supported protocols.
     * @param nativeJobDescription the job description in native language
     * @param uniqId a identifier unique to this job (not the job identifier, which is not generated yet)
     * @return the staging directory URL
     */
    public String getStagingDirectory(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Get pre-staging operations to perform before submitting the job.
     * @param nativeJobDescription the job description in native language
     * @param uniqId a identifier unique to this job (not the job identifier, which is not generated yet)
     * @return list of transfers that are not managed by the adaptor
     */
    public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
