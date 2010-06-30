package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UnmonitoredJobControlAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   25 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class UnmonitoredJobControlAdaptor extends LCGCEJobControlAdaptor {
    public String getType() {
        return "unmonitored";
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new UnmonitoredJobMonitorAdaptor();
    }
}
