package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.config.ConfigurationException;

import java.util.Map;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAttributesParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAttributesParser {
    protected Map m_map;

    /** constructor */
    public AbstractAttributesParser(Map map) {
        m_map = map;
    }

    /**
     * parse user attributes and put them in the map
     */
    public abstract void parse() throws ConfigurationException;

    /**
     * @param id the name of the instance
     * @param attributeName the name of the attribute
     * @param attributeValue the value of the attribute
     */
    protected void addAttribute(String id, String attributeName, String attributeValue) {
        Properties userAttributes = (Properties) m_map.get(id);
        if (userAttributes == null) {
            userAttributes = new Properties();
            m_map.put(id, userAttributes);
        }
        userAttributes.setProperty(attributeName, attributeValue);
    }

    /**
     * @param id the name of the instance
     * @return the user attributes
     */
    public Properties getUserAttributes(String id) {
        return (Properties) m_map.get(id);
    }
}
