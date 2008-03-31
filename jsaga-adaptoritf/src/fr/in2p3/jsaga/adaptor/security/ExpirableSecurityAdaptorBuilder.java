package fr.in2p3.jsaga.adaptor.security;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ExpirableSecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface ExpirableSecurityAdaptorBuilder extends SecurityAdaptorBuilder {
    /**
     * Destroy persisted state of security context
     * @param attributes the provided attributes.
     * @param contextId the identifier of the security context
     */
    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception;
}
