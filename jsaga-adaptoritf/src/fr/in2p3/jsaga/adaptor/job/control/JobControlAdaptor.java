package fr.in2p3.jsaga.adaptor.job.control;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobControlAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobControlAdaptor extends JobAdaptor {
    /**
     * submit a job
     * @param jobDesc the job description in the language supported by the targeted grid
     * @param checkMatch if true then explicitly checks if job description matches job service before submitting job
     * @param uniqId a identifier unique to this job (not the job identifier, which is not generated yet)
     * @return the identifier of the job in the grid
     * @throws BadResource if job service does not match job description
     */
    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource;

    /**
     * cancel a job
     * @param nativeJobId the identifier of the job in the grid
     */
    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
