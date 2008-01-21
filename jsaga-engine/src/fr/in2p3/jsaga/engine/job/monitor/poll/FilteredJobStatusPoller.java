package fr.in2p3.jsaga.engine.job.monitor.poll;

import fr.in2p3.jsaga.adaptor.job.monitor.QueryFilteredJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FilteredJobStatusPoller
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FilteredJobStatusPoller extends AbstractJobStatusPoller {
    private static Log s_logger = LogFactory.getLog(FilteredJobStatusPoller.class);
    private QueryFilteredJob m_adaptor;

    public FilteredJobStatusPoller(QueryFilteredJob adaptor) {
        super();
        m_adaptor = adaptor;
    }

    public void run() {
        //todo:
    }
}
