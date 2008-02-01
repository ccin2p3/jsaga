package fr.in2p3.jsaga.adaptor.job.control.advanced;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CleanableJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 févr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface CleanableJobAdaptor extends JobControlAdaptor {
    /**
     * clean an ended job
     * @param nativeJobId the identifier of the job in the grid
     */
    public void clean(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess;
}
