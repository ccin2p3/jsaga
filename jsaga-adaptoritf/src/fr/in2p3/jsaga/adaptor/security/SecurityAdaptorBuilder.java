package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.security.defaults.Default;
import fr.in2p3.jsaga.adaptor.security.usage.Usage;
import org.ogf.saga.error.IncorrectState;

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
public interface SecurityAdaptorBuilder {
    /**
     * @return the context type.
     */
    public String getType();

    /**
     * Get the expected usage for this context.
     * @return the description of the expected usage.
     */
    public Usage getUsage();

    /**
     * Get the defaults values for this context.
     * These values may be statically defined, or they may be generated according to the available information.
     * @param attributes the available information.
     * @return array of default values for some attributes.
     * @throws IncorrectState if cannot create valid default values based on the available information.
     */
    public Default[] getDefaults(Map attributes) throws IncorrectState;

    /**
     * Create a security context instance and initialize it with the provided attributes.
     * @param attributes the provided attributes.
     * @return the security context instance.
     */
    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception;
}
