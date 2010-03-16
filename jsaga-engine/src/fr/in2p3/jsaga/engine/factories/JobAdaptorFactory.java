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
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

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

    public JobService getConfig(URL url) throws NotImplementedException, IncorrectURLException, NoSuccessException {
        // check URL
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURLException("Invalid entry name: "+url);
        }

        // get config
        return m_configuration.findJobService(url);
    }

    public JobControlAdaptor getJobControlAdaptor(JobService config) throws NoSuccessException {
        // create instance
        Class clazz = m_descriptor.getClass(config.getType());
        try {
            return (JobControlAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public ContextImpl getContextImpl(URL url, JobControlAdaptor jobAdaptor, JobService config, Session session) throws NotImplementedException, NoSuccessException {
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
                throw new NoSuccessException("Security context not found: "+url.getFragment());
            }
        } else if (session.listContexts().length == 1) {
            context = (ContextImpl) session.listContexts()[0];
        } else if (config.getSupportedContextTypeCount() > 0) {
            context = super.findContext(session, config.getSupportedContextType());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("None of the supported security context is valid");
            }
        } else {
            context = null;
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("None of the supported security context is found");
            }
        }
        return context;
    }

    public Map getAttributes(URL url, JobService config) throws NotImplementedException, NoSuccessException {
        try {
            // get attributes from config and URL
            Map attributes = new HashMap();
            AttributesBuilder.updateAttributes(attributes, config);
            AttributesBuilder.updateAttributes(attributes, url);
            return attributes;
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }

    public void connect(URL url, JobControlAdaptor jobAdaptor, Map attributes, ContextImpl context) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // set security adaptor
        SecurityAdaptor securityAdaptor;
        if (context != null) {
            SecurityAdaptor candidate;
            try {
                candidate = context.getAdaptor();
            } catch (IncorrectStateException e) {
                throw new NoSuccessException("Invalid security context: "+super.getContextType(context), e);
            }
            if (SecurityAdaptorDescriptor.isSupported(candidate.getClass(), jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                securityAdaptor = candidate;
            } else if (SecurityAdaptorDescriptor.isSupportedNoContext(jobAdaptor.getSupportedSecurityAdaptorClasses())) {
                securityAdaptor = null;
            } else {
                throw new AuthenticationFailedException("Security context class '"+candidate.getClass().getName()+"' not supported for protocol: "+url.getScheme());
            }
        } else {
            securityAdaptor = null;
        }

        // connect
        connect(jobAdaptor, securityAdaptor, url, attributes);
    }

    public static void connect(JobControlAdaptor jobAdaptor, SecurityAdaptor securityAdaptor, URL url, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        jobAdaptor.setSecurityAdaptor(securityAdaptor);
        jobAdaptor.connect(
                url.getUserInfo(),
                url.getHost(),
                url.getPort()>0 ? url.getPort() : jobAdaptor.getDefaultPort(),
                url.getPath(),
                attributes);
    }
    public static void disconnect(JobControlAdaptor jobAdaptor) throws NoSuccessException {
        jobAdaptor.disconnect();
    }
}
