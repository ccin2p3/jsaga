package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.adaptor.JobAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.JobserviceEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.Jobservice;
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
* File:   JobAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobAdaptorFactory {
    private JobAdaptorDescriptor m_descriptor;
    private JobserviceEngineConfiguration m_configuration;

    public JobAdaptorFactory(Configuration configuration) {
        m_descriptor = configuration.getDescriptors().getJobDesc();
        m_configuration = configuration.getConfigurations().getJobserviceCfg();
    }

    /**
     * Create a new instance of job control adaptor for URL <code>service</code> and connect to service.
     * @param service the URL of the service
     * @param session the security session
     * @return the job control adaptor instance
     */
    public JobControlAdaptor getJobControlAdaptor(URL service, Session session) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (service==null || service.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name: "+service);
        }
        ContextImpl context = new JobContextSelector(session).selectContextByURI(service);
        if (context != null) {
            return this.getJobControlAdaptor(service, context);
        } else {
            return this.getJobControlAdaptor(service, (ContextImpl)null);
        }
    }

    /**
     * Create a new instance of job control adaptor for URL <code>service</code> and connect to service.
     * @param service the URL of the service
     * @param context the security context
     * @return the job control adaptor instance
     */
    private JobControlAdaptor getJobControlAdaptor(URL service, ContextImpl context) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        Jobservice config = m_configuration.findJobservice(service.getScheme());

        // create instance
        Class clazz = m_descriptor.getClass(config.getType());
        JobControlAdaptor jobAdaptor;
        try {
            jobAdaptor = (JobControlAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // set security
        if (context != null) {
            SecurityAdaptor securityAdaptor = context.getAdaptor();
            if (SecurityAdaptorDescriptor.isSupported(securityAdaptor.getClass(), jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                jobAdaptor.setSecurityAdaptor(securityAdaptor);
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
        int port = (service.getPort()>0 ? service.getPort() : jobAdaptor.getDefaultPort());
        jobAdaptor.connect(service.getUserInfo(), service.getHost(), port, service.getPath(), attributes);
        return jobAdaptor;
    }
}
