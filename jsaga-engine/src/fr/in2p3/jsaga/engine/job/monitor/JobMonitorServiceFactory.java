package fr.in2p3.jsaga.engine.job.monitor;

import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.engine.factories.JobMonitorAdaptorFactory;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

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
    private Map<URL,JobMonitorService> m_services;

    public JobMonitorServiceFactory(JobMonitorAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
        m_services = new HashMap<URL,JobMonitorService>();
    }

    public JobMonitorService getJobMonitorService(URL controlURL, Session session) throws Timeout, PermissionDenied, IncorrectState, NoSuccess, BadParameter, IncorrectURL, AuthorizationFailed, NotImplemented, AuthenticationFailed {
        URL monitorURL = null;  //todo: get monitorURL from controlURL
        if (monitorURL == null) {
            monitorURL = controlURL;
        }
        JobMonitorService service = m_services.get(monitorURL);
        if (service == null) {
            JobMonitorAdaptor adaptor = m_adaptorFactory.getJobMonitorAdaptor(monitorURL, session);
            service = new JobMonitorService(monitorURL, adaptor);
            m_services.put(monitorURL, service);
        }
        return service;
    }
}
