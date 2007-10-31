package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.adaptor.DataAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.config.bean.ProtocolEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.engine.security.DataContextSelector;
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
public class DataAdaptorFactory {
    private DataAdaptorDescriptor m_descriptor;
    private ProtocolEngineConfiguration m_configuration;

    public DataAdaptorFactory(Configuration configuration) {
        m_descriptor = configuration.getDescriptors().getDataDesc();
        m_configuration = configuration.getConfigurations().getProtocolCfg();
    }

    /**
     * Create a new instance of data adaptor for URL <code>service</code> and connect to service.
     * @param service the URL of the service
     * @param session the security session
     * @return the data adaptor instance
     */
    public DataAdaptor getDataAdaptor(URL service, Session session) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (service==null || service.getScheme()==null) {
            throw new IncorrectURL("Invalid entry name");
        }
        ContextImpl context = new DataContextSelector(session).selectContextByURI(service);
        if (context != null) {
            return this.getDataAdaptor(service, context);
        } else {
            return this.getDataAdaptor(service, (ContextImpl)null);
        }
    }

    /**
     * Create a new instance of data adaptor for URL <code>service</code> and connect to service.
     * <br>Note: only 'scheme' is needed for creation, but 'userid' and 'hostname' might be useful
     * for caching data adaptor instances.
     * @param service the URL of the service
     * @param context the security context
     * @return the data adaptor instance
     */
    private DataAdaptor getDataAdaptor(URL service, ContextImpl context) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // create instance
        Class clazz = m_descriptor.getClass(service.getScheme());
        DataAdaptor dataAdaptor;
        try {
            dataAdaptor = (DataAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // set security
        if (context != null) {
            SecurityAdaptor securityAdaptor = context.createSecurityAdaptor();
            if (SecurityAdaptorDescriptor.isSupported(securityAdaptor.getClass(), dataAdaptor.getSupportedSecurityAdaptorClasses())) {
                dataAdaptor.setSecurityAdaptor(securityAdaptor);
            } else {
                throw new AuthenticationFailed("Security context class '"+ securityAdaptor.getClass().getName() +"' not supported for protocol: "+service.getScheme());
            }
        }

        // get attributes from config
        Map attributes = new HashMap();
        Protocol config = m_configuration.findProtocol(service.getScheme());
        for (int i=0; i<config.getAttributeCount(); i++) {
            attributes.put(config.getAttribute(i).getName(), config.getAttribute(i).getValue());
        }

        // connect
        int port = (service.getPort()>0 ? service.getPort() : dataAdaptor.getDefaultPort());
        dataAdaptor.connect(service.getUserInfo(), service.getHost(), port, attributes);
        return dataAdaptor;
    }
}
