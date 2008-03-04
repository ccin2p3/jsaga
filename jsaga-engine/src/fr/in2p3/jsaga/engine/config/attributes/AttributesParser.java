package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.config.ConfigurationException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AttributesParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface AttributesParser {
    public void updateAttributes() throws ConfigurationException;
}
