package fr.in2p3.jsaga.adaptor.job.control.advanced;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SignalableJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SignalableJobAdaptor extends JobControlAdaptor {
    /**
     * deliver an arbitrary signal to an active job
     * @param nativeJobId the identifier of the job in the grid
     * @param signum the signal number
     * @return true if the job has been successfully signaled, false if it was not active
     */
    public boolean signal(String nativeJobId, int signum) throws PermissionDenied, Timeout, NoSuccess;
}
