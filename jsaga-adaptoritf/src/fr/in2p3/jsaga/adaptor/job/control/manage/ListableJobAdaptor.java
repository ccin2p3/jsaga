package fr.in2p3.jsaga.adaptor.job.control.manage;

import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ListableJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface ListableJobAdaptor extends JobMonitorAdaptor {
    /**
     * Obtains the list of jobs that are currently known to the resource manager.
     * @return a list of job identifications.
     */
    public String[] list() throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
