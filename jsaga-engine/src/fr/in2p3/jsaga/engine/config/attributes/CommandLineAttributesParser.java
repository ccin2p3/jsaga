package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.schema.config.EffectiveConfig;

import java.util.Iterator;
import java.util.Map;

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
 * Update attributes with command line arguments
 */
public class CommandLineAttributesParser extends AbstractAttributesParser implements AttributesParser {
    public CommandLineAttributesParser(EffectiveConfig config) {
        super(config);
    }

    public void updateAttributes() {
        for (Iterator it=System.getProperties().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String propertyName = (String) entry.getKey();
            String propertyValue = (String) entry.getValue();
            if (isValid(propertyName)) {
                super.updateOrInsertAttribute(propertyName, propertyValue);
            }
        }
    }

    private static boolean isValid(String propertyName) {
        final String[] KNOWN_SYSTEM_PROPERTY_PREFIX = {"java.", "awt.", "user.", "os.", "line.", "path."};
        for (int i=0; i<KNOWN_SYSTEM_PROPERTY_PREFIX.length; i++) {
            if (propertyName.startsWith(KNOWN_SYSTEM_PROPERTY_PREFIX[i])) {
                return false;
            }
        }
        final String[] KNOWN_SYSTEM_PROPERTY_NAME = {"file.separator", "file.encoding"};
        for (int i=0; i<KNOWN_SYSTEM_PROPERTY_NAME.length; i++) {
            if (propertyName.equals(KNOWN_SYSTEM_PROPERTY_NAME[i])) {
                return false;
            }
        }
        return true;
    }
}
