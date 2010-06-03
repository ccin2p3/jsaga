package fr.in2p3.jsaga.engine.job.monitor;

import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import fr.in2p3.jsaga.engine.schema.config.JobService;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobMonitorServiceFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobMonitorServiceFactory {
    private JobMonitorAdaptorFactory m_adaptorFactory;
    private Map<JobService,JobMonitorService> m_services;

    public JobMonitorServiceFactory(JobMonitorAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
        m_services = new HashMap<JobService,JobMonitorService>();
    }

    public JobMonitorService getJobMonitorService(URL controlURL, JobService config, ContextImpl context) throws TimeoutException, PermissionDeniedException, NoSuccessException, BadParameterException, IncorrectURLException, AuthorizationFailedException, NotImplementedException, AuthenticationFailedException {
        URL monitorURL = m_adaptorFactory.getJobMonitorURL(controlURL);
        JobMonitorService service = m_services.get(monitorURL);
        if (service == null) {
            // get adaptor
            JobMonitorAdaptor monitorAdaptor = m_adaptorFactory.getJobMonitorAdaptor(config);
            // get attributes
            Map monitorAttributes = m_adaptorFactory.getAttributes(monitorURL, config);

            // connect
            m_adaptorFactory.connect(monitorURL, monitorAdaptor, monitorAttributes, context);

            // register monitor service
            service = new JobMonitorService(monitorURL, monitorAdaptor, monitorAttributes);
            m_services.put(config, service);
        }
        return service;
    }
}
