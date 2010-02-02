package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.impl.job.service.JobServiceImpl;
import org.apache.log4j.Logger;
import org.ogf.saga.error.SagaException;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobServiceReset
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   22 janv. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class JobServiceReset implements Runnable {
    private static Logger s_logger = Logger.getLogger(JobServiceReset.class);

    private Map<JobServiceImpl,Map> m_jobServices;
    private SecurityAdaptor m_adaptor;

    public JobServiceReset(Map<JobServiceImpl,Map> registry, SecurityAdaptor adaptor) {
        m_jobServices = registry;
        m_adaptor = adaptor;
    }

    public void run() {
        for (Map.Entry<JobServiceImpl,Map> entry : m_jobServices.entrySet()) {
            JobServiceImpl jobService = entry.getKey();
            Map attributes = entry.getValue();
            try {
                jobService.resetAdaptors(m_adaptor, attributes);
            } catch (SagaException e) {
                s_logger.warn("Failed to reconnect to job service", e);
            }
        }
    }
}
