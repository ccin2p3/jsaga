package fr.in2p3.jsaga.adaptor.base;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import org.ogf.saga.error.IncorrectStateException;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaBaseAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SagaBaseAdaptor {
    /**
     * @return the adaptor type (context type, data protocol or job type).
     */
    public String getType();

    /**
     * Get the expected usage for this adaptor.
     * @return the description of the expected usage.
     */
    public Usage getUsage();

    /**
     * Get the defaults values for this adaptor.
     * These values may be statically defined, or they may be generated according to the available information.
     * @param attributes the available information.
     * @return array of default values for some attributes.
     * @throws IncorrectStateException if cannot create valid default values based on the available information.
     */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException;
}
