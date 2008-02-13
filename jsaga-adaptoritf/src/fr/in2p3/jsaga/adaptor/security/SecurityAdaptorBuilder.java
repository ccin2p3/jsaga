package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.SagaBaseAdaptor;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SecurityAdaptorBuilder extends SagaBaseAdaptor {
    /**
     * @return the context type.
     */
    public String getType();

    /**
     * @return the security adaptor class instanciated by this builder.
     */
    public Class getSecurityAdaptorClass();

    /**
     * Create a security context instance and initialize it with the provided attributes.
     * @param usage the identifier of the usage.
     * @param attributes the provided attributes.
     * @param contextId the identifier of the context instance.
     * @return the security context instance.
     * @throws IncorrectState if the attributes refer to a context that is not of expected type
     * @throws NoSuccess if creating the adaptor failed
     */
    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectState, NoSuccess;
}
