package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.SagaBaseAdaptor;

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
     * @param attributes the provided attributes.
     * @return the security context instance.
     */
    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception;
}
