package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LCGCEJobControlAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LCGCEJobControlAdaptor extends UnmonitoredJobControlAdaptor implements JobControlAdaptor {
    public String getType() {
        return "lcgce";
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new LCGCEJobMonitorAdaptor();
    }
}
