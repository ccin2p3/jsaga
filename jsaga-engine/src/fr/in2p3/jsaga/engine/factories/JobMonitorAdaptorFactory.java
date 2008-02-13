package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.JobserviceEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.Monitor;
import fr.in2p3.jsaga.engine.security.JobContextSelector;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobMonitorAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobMonitorAdaptorFactory {
    private JobserviceEngineConfiguration m_configuration;

    public JobMonitorAdaptorFactory(Configuration configuration) {
        m_configuration = configuration.getConfigurations().getJobserviceCfg();
    }

    /**
     * Create a new instance of job monitor adaptor for URL <code>service</code> and connect to service.
     * @param service the URL of the service
     * @param session the security session
     * @return the job monitor adaptor instance
     */
    public JobMonitorAdaptor getJobMonitorAdaptor(URL service, Session session) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (service==null || service.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name: "+service);
        }
        ContextImpl context = new JobContextSelector(session).selectContextByURI(service);
        if (context != null) {
            return this.getJobMonitorAdaptor(service, context);
        } else {
            return this.getJobMonitorAdaptor(service, (ContextImpl)null);
        }
    }

    /**
     * Create a new instance of job monitor adaptor for URL <code>service</code> and connect to service.
     * @param service the URL of the service
     * @param context the security context
     * @return the job monitor adaptor instance
     */
    private JobMonitorAdaptor getJobMonitorAdaptor(URL service, ContextImpl context) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        Monitor config = m_configuration.findJobservice(service.getScheme()).getMonitor();
        if (config == null) {
            return null;
        }

        // create instance
        JobMonitorAdaptor monitorAdaptor;
        try {
            Class clazz = Class.forName(config.getImpl());
            monitorAdaptor = (JobMonitorAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // set security
        if (context != null) {
            SecurityAdaptor securityAdaptor = context.getAdaptor();
            if (SecurityAdaptorDescriptor.isSupported(securityAdaptor.getClass(), monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                monitorAdaptor.setSecurityAdaptor(securityAdaptor);
            } else {
                throw new AuthenticationFailed("Security context class '"+ securityAdaptor.getClass().getName() +"' not supported for protocol: "+service.getScheme());
            }
        }

        // get attributes from config
        Map attributes = new HashMap();
        for (int i=0; i<config.getAttributeCount(); i++) {
            attributes.put(config.getAttribute(i).getName(), config.getAttribute(i).getValue());
        }

        // connect
        int port = (service.getPort()>0 ? service.getPort() : monitorAdaptor.getDefaultPort());
        monitorAdaptor.connect(service.getUserInfo(), service.getHost(), port, service.getPath(), attributes);
        return monitorAdaptor;
    }
}
