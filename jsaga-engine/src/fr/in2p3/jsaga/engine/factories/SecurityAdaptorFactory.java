package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.schema.config.Context;
import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SecurityAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Create and manage security adaptors
 */
public class SecurityAdaptorFactory {
    private static SecurityAdaptorFactory _instance = null;

    private SecurityAdaptorDescriptor m_descriptor;

    public static synchronized SecurityAdaptorFactory getInstance() throws ConfigurationException {
        if (_instance == null) {
            _instance = new SecurityAdaptorFactory();
        }
        return _instance;
    }
    private SecurityAdaptorFactory() throws ConfigurationException {
        m_descriptor = Configuration.getInstance().getDescriptors().getSecurityDesc();
    }

    public SecurityAdaptor getSecurityAdaptor(String id) throws NoSuccessException {
        Context byName = Configuration.getInstance().getConfigurations().getContextCfg().findContextByName(id);
        if (byName != null) {
            return this.getSecurityAdaptorByType(byName.getType());
        } else {
            return this.getSecurityAdaptorByType(id);
        }
    }

    private SecurityAdaptor getSecurityAdaptorByType(String type) throws NoSuccessException {
        // create instance
        Class clazz = m_descriptor.getAdaptorClass(type);
        SecurityAdaptor adaptor;
        try {
            adaptor = (SecurityAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        return adaptor;
    }
}
