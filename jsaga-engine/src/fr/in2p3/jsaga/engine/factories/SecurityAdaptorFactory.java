package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.descriptors.SecurityAdaptorDescriptor;
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
    private SecurityAdaptorDescriptor m_descriptor;

    public SecurityAdaptorFactory(AdaptorDescriptors descriptors) {
        m_descriptor = descriptors.getSecurityDesc();
    }

    public SecurityAdaptor getSecurityAdaptor(String type) throws NoSuccessException {
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
