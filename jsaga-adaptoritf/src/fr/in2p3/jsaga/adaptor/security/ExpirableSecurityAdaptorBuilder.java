package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;

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
     * Get the expected usage for the initialization of this context.
     * @return the description of the expected usage.
     */
    public Usage getInitUsage();

    /**
     * Initialize a security context with the provided attributes, and make it persistent for current user.
     * @param attributes the provided attributes.
     * @param contextId can be either a context name or a pair (context type + indice)
     */
    public void initBuilder(Map attributes, String contextId) throws Exception;

    /**
     * Permanently destroy a security context for current user.
     * @param contextId can be either a context name or a pair (context type + indice)
     */
    public void destroyBuilder(Map attributes, String contextId) throws Exception;
}
