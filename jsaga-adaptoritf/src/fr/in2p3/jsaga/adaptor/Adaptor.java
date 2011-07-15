package fr.in2p3.jsaga.adaptor;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import org.ogf.saga.error.IncorrectStateException;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Adaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface Adaptor {
    public static final String JSAGA_FACTORY = Base.getSagaFactory();

    /**
     * @return the adaptor type (context type, data protocol or job protocol).
     */
    public String getType();

    /**
     * Get a data structure that describes how to use this adaptor.
     * This data structure contains attribute names with usage constraints (and/or, required/optional, hidden...).
     * @return the usage data structure.
     */
    public Usage getUsage();

    /**
     * Get the defaults values for (some of the) attributes supported by this adaptor.
     * These values can be static or dynamically created from the information available on local host
     * (environment variables, files, ...) and from the attributes map.
     * @param attributes the attributes set by the user.
     * @return an array of default values.
     * @throws IncorrectStateException if cannot create valid default values based on the information available.
     */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException;
}
