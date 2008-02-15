package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import org.ogf.saga.error.NoSuccess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SecurityAdaptorBuilderFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 * Create and manage security adaptors
 */
public class SecurityAdaptorBuilderFactory {
    private static SecurityAdaptorBuilderFactory _instance = null;

    private SecurityAdaptorDescriptor m_descriptor;

    public static synchronized SecurityAdaptorBuilderFactory getInstance() throws ConfigurationException {
        if (_instance == null) {
            _instance = new SecurityAdaptorBuilderFactory();
        }
        return _instance;
    }
    private SecurityAdaptorBuilderFactory() throws ConfigurationException {
        m_descriptor = Configuration.getInstance().getDescriptors().getSecurityDesc();
    }

    public SecurityAdaptorBuilder getSecurityAdaptorBuilder(String id) throws NoSuccess {
        ContextInstance byName = Configuration.getInstance().getConfigurations().getContextCfg().findContextInstanceByName(id);
        if (byName != null) {
            return this.getSecurityAdaptorBuilderByType(byName.getType());
        } else {
            return this.getSecurityAdaptorBuilderByType(id);
        }
    }

    private SecurityAdaptorBuilder getSecurityAdaptorBuilderByType(String type) throws NoSuccess {
        // create instance
        Class clazz = m_descriptor.getBuilderClass(type);
        SecurityAdaptorBuilder adaptorBuilder;
        try {
            adaptorBuilder = (SecurityAdaptorBuilder) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        return adaptorBuilder;
    }
}
