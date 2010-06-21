package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

/*
 * Aurélien Croc (CEA Saclay, FRANCE)
 * May, 1st 2010
 *
 * LCGCE+ Job Monitor
 */
public class GatekeeperCondorJobMonitorAdaptor extends GkCommonJobMonitorAdaptor implements QueryIndividualJob {
    public String getType() {
        return "gatekeeper-condor";
    }
}
