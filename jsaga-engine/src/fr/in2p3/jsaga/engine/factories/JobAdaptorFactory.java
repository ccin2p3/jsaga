package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.adaptor.JobAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.JobserviceEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.JobService;
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
public class JobAdaptorFactory extends ServiceAdaptorFactory {
    private JobAdaptorDescriptor m_descriptor;
    private JobserviceEngineConfiguration m_configuration;

    public JobAdaptorFactory(Configuration configuration) {
        super(configuration.getConfigurations().getContextCfg());
        m_descriptor = configuration.getDescriptors().getJobDesc();
        m_configuration = configuration.getConfigurations().getJobserviceCfg();
    }

    /**
     * Create a new instance of job control adaptor for URL <code>url</code> and connect to service.
     * todo: cache job adaptor instances with "scheme://userInfo@host:port"
     * @param url the URL of the service
     * @param session the security session
     * @return the job control adaptor instance
     */
    public JobControlAdaptor getJobControlAdaptor(URL url, Session session) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name: "+url);
        }

        // get config
        JobService config = m_configuration.findJobService(url);

        // create instance
        Class clazz = m_descriptor.getClass(config.getType());
        JobControlAdaptor jobAdaptor;
        try {
            jobAdaptor = (JobControlAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // get security context
        ContextImpl context;
        if (config.getContextRef() != null) {
            context = super.findContext(session, config.getContextRef());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new ConfigurationException("INTERNAL ERROR: effective-config may be inconsistent");
            }
        } else if (url.getFragment() != null) {
            context = super.findContext(session, url.getFragment());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccess("Security context not found: "+url.getFragment());
            }
        } else if (config.getSupportedContextTypeCount() > 0) {
            context = super.findContext(session, config.getSupportedContextType());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccess("None of the supported security context is valid");
            }
        } else {
            context = null;
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccess("None of the supported security context is found");
            }
        }

        // set security adaptor
        if (context != null) {
            SecurityAdaptor securityAdaptor;
            try {
                securityAdaptor = context.getAdaptor();
            } catch (IncorrectState e) {
                throw new NoSuccess("Bad security context: "+super.getContextType(context), e);
            }
            if (SecurityAdaptorDescriptor.isSupported(securityAdaptor.getClass(), jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                jobAdaptor.setSecurityAdaptor(securityAdaptor);
            } else {
                throw new AuthenticationFailed("Security context class '"+ securityAdaptor.getClass().getName() +"' not supported for protocol: "+url.getScheme());
            }
        }

        // get attributes from config
        Map attributes = new HashMap();
        for (int i=0; i<config.getAttributeCount(); i++) {
            attributes.put(config.getAttribute(i).getName(), config.getAttribute(i).getValue());
        }
        if (config.getMonitorService()!=null && config.getMonitorService().getUrl()!=null) {
            URL monitorURL = new URL(config.getMonitorService().getUrl());
            attributes.put(JobControlAdaptor.MONITOR_SERVICE_URL, monitorURL);
        }

        // connect
        int port = (url.getPort()>0 ? url.getPort() : jobAdaptor.getDefaultPort());
        jobAdaptor.connect(url.getUserInfo(), url.getHost(), port, url.getPath(), attributes);
        return jobAdaptor;
    }
}
