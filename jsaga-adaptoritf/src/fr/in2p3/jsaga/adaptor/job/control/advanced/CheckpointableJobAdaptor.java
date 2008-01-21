package fr.in2p3.jsaga.adaptor.job.control.advanced;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CheckpointableJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface CheckpointableJobAdaptor extends JobControlAdaptor {
    /**
     * initiate a checkpoint operation on an active job
     * @param nativeJobId the identifier of the job in the grid
     * @return true if the job has been successfully checkpointed, false if it was not active
     */
    public boolean checkpoint(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess;
}
