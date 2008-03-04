package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.adaptor.DataAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.ProtocolEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.DataService;
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
* File:   DataAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Create and manage data adaptors
 */
public class DataAdaptorFactory extends ServiceAdaptorFactory {
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
    public DataAdaptor getDataAdaptor(URL url, Session session) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (url==null || url.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name");
        }

        // get config
        DataService config = m_configuration.findDataService(url);

        // create instance
        Class clazz = m_descriptor.getClass(config.getType());
        DataAdaptor dataAdaptor;
        try {
            dataAdaptor = (DataAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // get security context
        ContextImpl context;
        if (config.getContextRef() != null) {
            context = super.findContext(session, config.getContextRef());
            if (context == null) {
                throw new ConfigurationException("INTERNAL ERROR: effective-config may be inconsistent");                
            }
        } else if (url.getFragment() != null) {
            context = super.findContext(session, url.getFragment());
            if (context == null) {
                throw new NoSuccess("Security context not found: "+url.getFragment());
            }
        } else if (config.getSupportedContextTypeCount() > 0) {
            context = super.findContext(session, config.getSupportedContextType());
            if (context == null) {
                throw new NoSuccess("None of the supported security context is valid");
            }
        } else {
            context = null;
        }

        // set security adaptor
        if (context != null) {
            SecurityAdaptor securityAdaptor;
            try {
                securityAdaptor = context.getAdaptor();
            } catch (IncorrectState e) {
                throw new NoSuccess(e);
            }
            if (SecurityAdaptorDescriptor.isSupported(securityAdaptor.getClass(), dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                dataAdaptor.setSecurityAdaptor(securityAdaptor);
            } else {
                throw new AuthenticationFailed("Security context class '"+ securityAdaptor.getClass().getName() +"' not supported for protocol: "+url.getScheme());
            }
        }

        // get attributes from config
        Map attributes = new HashMap();
        for (int i=0; i<config.getAttributeCount(); i++) {
            attributes.put(config.getAttribute(i).getName(), config.getAttribute(i).getValue());
        }

        // connect
        int port = (url.getPort()>0 ? url.getPort() : dataAdaptor.getDefaultPort());
        dataAdaptor.connect(url.getUserInfo(), url.getHost(), port, null, attributes);
        return dataAdaptor;
    }
}
