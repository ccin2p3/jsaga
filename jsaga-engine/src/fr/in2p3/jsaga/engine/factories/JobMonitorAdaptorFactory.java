package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.JobserviceEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.JobService;
import fr.in2p3.jsaga.engine.schema.config.MonitorService;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
public class JobMonitorAdaptorFactory extends ServiceAdaptorFactory {
    private JobserviceEngineConfiguration m_configuration;

    public JobMonitorAdaptorFactory(Configuration configuration) {
        super(configuration.getConfigurations().getContextCfg());
        m_configuration = configuration.getConfigurations().getJobserviceCfg();
    }

    public URL getJobMonitorURL(URL controlURL) throws NotImplementedException, IncorrectURLException, BadParameterException, NoSuccessException {
        if (controlURL==null || controlURL.getScheme()==null) {
            throw new IncorrectURLException("Invalid entry name: "+controlURL);
        }

        // get config
        JobService jobServiceConfig = m_configuration.findJobService(controlURL);
        MonitorService config = jobServiceConfig.getMonitorService();

        // get monitorURL
        if (config.getUrl() != null) {
            return URLFactory.createURL(config.getUrl());
        } else {
            return controlURL;
        }
    }

    /**
     * Create a new instance of job monitor adaptor for URL <code>url</code> and connect to service.
     * @param url the URL of the service
     * @param session the security session
     * @return the job monitor adaptor instance
     */
    public JobMonitorAdaptor getJobMonitorAdaptor(URL url, Session session) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURLException("Invalid entry name: "+url);
        }

        // get config
        JobService jobServiceConfig = m_configuration.findJobService(url);
        MonitorService config = jobServiceConfig.getMonitorService();

        // create instance
        JobMonitorAdaptor monitorAdaptor;
        try {
            Class clazz = Class.forName(config.getImpl());
            monitorAdaptor = (JobMonitorAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }

        // get security context
        ContextImpl context;
        if (jobServiceConfig.getContextRef() != null) {
            context = super.findContext(session, jobServiceConfig.getContextRef());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new ConfigurationException("INTERNAL ERROR: effective-config may be inconsistent");
            }
        } else if (url.getFragment() != null) {
            context = super.findContext(session, url.getFragment());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("Security context not found: "+url.getFragment());
            }
        } else if (jobServiceConfig.getSupportedContextTypeCount() > 0) {
            context = super.findContext(session, jobServiceConfig.getSupportedContextType());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("None of the supported security context is valid");
            }
        } else {
            context = null;
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("None of the supported security context is found");
            }
        }

        // set security adaptor
        if (context != null) {
            SecurityAdaptor securityAdaptor;
            try {
                securityAdaptor = context.getAdaptor();
            } catch (IncorrectStateException e) {
                throw new NoSuccessException("Bad security context: "+super.getContextType(context), e);
            }
            if (SecurityAdaptorDescriptor.isSupported(securityAdaptor.getClass(), monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                monitorAdaptor.setSecurityAdaptor(securityAdaptor);
            } else if (SecurityAdaptorDescriptor.isSupportedNoContext(monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                monitorAdaptor.setSecurityAdaptor(null);
            } else {
                throw new AuthenticationFailedException("Security context class '"+ securityAdaptor.getClass().getName() +"' not supported for protocol: "+url.getScheme());
            }
        }

        // get attributes from config and URL
        Map attributes = new HashMap();
        AttributesBuilder.updateAttributes(attributes, config);
        AttributesBuilder.updateAttributes(attributes, url);

        // connect
        int port = (url.getPort()>0 ? url.getPort() : monitorAdaptor.getDefaultPort());
        monitorAdaptor.connect(url.getUserInfo(), url.getHost(), port, url.getPath(), attributes);
        return monitorAdaptor;
    }
}
