package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobAdaptor extends SagaSecureAdaptor {
    public static final String CHECK_AVAILABILITY = "CheckAvailability";

    /**
     * @return the default server port.
     */
    public int getDefaultPort();

    /**
     * @return the class of the job monitor implementation
     */
    public JobMonitorAdaptor getDefaultJobMonitor();
}
