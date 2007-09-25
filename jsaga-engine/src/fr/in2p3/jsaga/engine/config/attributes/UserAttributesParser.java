package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.config.ConfigurationException;

import java.util.HashMap;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserAttributesParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UserAttributesParser extends AbstractAttributesParser {
    public UserAttributesParser() {
        super(new HashMap());
    }

    public void parse() throws ConfigurationException {
        new FilePropertiesAttributesParser(m_map).parse();
        new CommandLineAttributesParser(m_map).parse();
    }
}
