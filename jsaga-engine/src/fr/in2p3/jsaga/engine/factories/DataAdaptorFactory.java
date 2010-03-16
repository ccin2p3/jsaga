package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.adaptor.DataAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.ProtocolEngineConfiguration;
import fr.in2p3.jsaga.engine.data.FilledURL;
import fr.in2p3.jsaga.engine.schema.config.DataService;
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
* File:   DataAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Create and manage data adaptors
 */
public class DataAdaptorFactory extends ServiceAdaptorFactory {
    public static final boolean PHYSICAL = false;
    public static final boolean LOGICAL = true;
    
    private DataAdaptorDescriptor m_descriptor;
    private ProtocolEngineConfiguration m_configuration;

    public DataAdaptorFactory(Configuration configuration) {
        super(configuration.getConfigurations().getContextCfg());
        m_descriptor = configuration.getDescriptors().getDataDesc();
        m_configuration = configuration.getConfigurations().getProtocolCfg();
    }

    /**
     * Create a new instance of data adaptor for URL <code>url</code> and connect to service.
     * todo: cache data adaptor instances with "scheme://userInfo@host:port"
     * @param url the URL of the service
     * @param session the security session
     * @return the data adaptor instance
     */
    public DataAdaptor getDataAdaptor(URL url, Session session, boolean isLogical) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURLException("No protocol found in URL: "+url);
        }

        // get config
        DataService config = m_configuration.findDataService(url, isLogical);

        // create instance
        Class clazz = m_descriptor.getClass(config.getType());
        DataAdaptor dataAdaptor;
        try {
            dataAdaptor = (DataAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }

        // get security context
        ContextImpl context;
        if (config.getContextRef() != null) {
            context = super.findContext(session, config.getContextRef());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new ConfigurationException("INTERNAL ERROR: effective-config may be inconsistent");                
            }
        } else if (url.getFragment() != null) {
            context = super.findContext(session, url.getFragment());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("Security context not found: "+url.getFragment());
            }
        } else if (session.listContexts().length == 1) {
            context = (ContextImpl) session.listContexts()[0];
        } else if (config.getSupportedContextTypeCount() > 0) {
            context = super.findContext(session, config.getSupportedContextType());
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("None of the supported security context is valid");
            }
        } else {
            context = null;
            if (context == null && !SecurityAdaptorDescriptor.isSupportedNoContext(dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                throw new NoSuccessException("None of the supported security context is found");
            }
        }

        // set security adaptor
        if (context != null) {
            SecurityAdaptor securityAdaptor;
            try {
                securityAdaptor = context.getAdaptor();
            } catch (IncorrectStateException e) {
                throw new NoSuccessException("Invalid security context: "+super.getContextType(context), e);
            }
            if (SecurityAdaptorDescriptor.isSupported(securityAdaptor.getClass(), dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                dataAdaptor.setSecurityAdaptor(securityAdaptor);
            } else if (SecurityAdaptorDescriptor.isSupportedNoContext(dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                dataAdaptor.setSecurityAdaptor(null);
            } else {
                throw new AuthenticationFailedException("Security context class '"+ securityAdaptor.getClass().getName() +"' not supported for protocol: "+url.getScheme());
            }
        }

        // get attributes from config
        Map attributes = new HashMap();
        AttributesBuilder.updateAttributes(attributes, config);

        // get attributes from filled URL
        FilledURL filledUrl = new FilledURL(url, config);
        filledUrl.setAttributes(attributes);
        
        // connect
        dataAdaptor.connect(filledUrl.getUserInfo(), filledUrl.getHost(), filledUrl.getPort(), filledUrl.getPath(), attributes);
        return dataAdaptor;
    }
}
