package fr.in2p3.jsaga.engine.config.attributes;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CommandLineAttributesParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CommandLineAttributesParser extends AbstractAttributesParser {
    private static final String[] KNOWN_SYSTEM_PROPERTIES = {
            "java", "awt", "user", "os", "line", "path", "file.separator", "file.encoding"};

    public CommandLineAttributesParser(Map map) {
        super(map);
    }

    public void parse() {
        // set known system properties
        Set propNamesFilter = new HashSet();
        for (int i=0; i< KNOWN_SYSTEM_PROPERTIES.length; i++) {
            propNamesFilter.add(KNOWN_SYSTEM_PROPERTIES[i]);
        }

        // set attributes
        for (Iterator it=System.getProperties().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String propName = (String) entry.getKey();
            String attributeValue = (String) entry.getValue();
            String[] array = propName.split("\\.");
            if (array.length == 2) {
                String id = array[0];
                String attributeName = array[1];
                if (!propNamesFilter.contains(id) && !propNamesFilter.contains(propName)) {
                    super.addAttribute(id, attributeName, attributeValue);
                }
            }
        }
    }
}
