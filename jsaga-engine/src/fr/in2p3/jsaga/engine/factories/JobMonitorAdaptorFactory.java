package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.JobserviceEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.JobService;
import fr.in2p3.jsaga.engine.schema.config.MonitorService;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.error.*;
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

    public JobMonitorAdaptor getJobMonitorAdaptor(JobService config) throws NoSuccessException {
        MonitorService monitorConfig = config.getMonitorService();

        // create instance
        try {
            Class clazz = Class.forName(monitorConfig.getImpl());
            return (JobMonitorAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public Map getAttributes(URL monitorURL, JobService config) throws NoSuccessException {
        try {
            // get attributes from config and URL
            Map attributes = new HashMap();
            AttributesBuilder.updateAttributes(attributes, config.getMonitorService());
            AttributesBuilder.updateAttributes(attributes, monitorURL);
            return attributes;
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }
    
    public void connect(URL monitorURL, JobMonitorAdaptor monitorAdaptor, Map monitorAttributes, ContextImpl context) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // check URL
        if (monitorURL==null || monitorURL.getScheme()==null) {
            throw new IncorrectURLException("Invalid entry name: "+monitorURL);
        }

        // set security adaptor
        SecurityAdaptor securityAdaptor;
        if (context != null) {
            SecurityAdaptor candidate;
            try {
                candidate = context.getAdaptor();
            } catch (IncorrectStateException e) {
                throw new NoSuccessException("Invalid security context: "+super.getContextType(context), e);
            }
            if (SecurityAdaptorDescriptor.isSupported(candidate.getClass(), monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                securityAdaptor = candidate;
            } else if (SecurityAdaptorDescriptor.isSupportedNoContext(monitorAdaptor.getSupportedSecurityAdaptorClasses())) {
                securityAdaptor = null;
            } else {
                throw new AuthenticationFailedException("Security context class '"+ candidate.getClass().getName() +"' not supported for protocol: "+monitorURL.getScheme());
            }
        } else {
            securityAdaptor = null;
        }

        // connect
        connect(monitorAdaptor, securityAdaptor, monitorURL, monitorAttributes);
    }

    public static void connect(JobMonitorAdaptor monitorAdaptor, SecurityAdaptor securityAdaptor, URL url, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        monitorAdaptor.setSecurityAdaptor(securityAdaptor);
        monitorAdaptor.connect(
                url.getUserInfo(),
                url.getHost(),
                url.getPort()>0 ? url.getPort() : monitorAdaptor.getDefaultPort(),
                url.getPath(),
                attributes);
    }
    public static void disconnect(JobMonitorAdaptor monitorAdaptor) throws NoSuccessException {
        monitorAdaptor.disconnect();
    }
}
