package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.engine.schema.config.ObjectType;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AttributesBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   09 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AttributesBuilder {
    public static void updateAttributes(Map attributes, ObjectType config) {
        for (int i=0; i<config.getAttributeCount(); i++) {
            attributes.put(config.getAttribute(i).getName(), config.getAttribute(i).getValue());
        }
    }

    public static void updateAttributes(Map attributes, URL url) throws BadParameterException {
        String query = url.getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (int i=0; pairs!=null && i<pairs.length; i++) {
                String[] pair = pairs[i].split("=");
                switch (pair.length) {
                    case 1:
                        attributes.put(pair[0], null);
                        break;
                    case 2:
                        attributes.put(pair[0], pair[1]);
                        break;
                    default:
                        throw new BadParameterException("Bad query in URL: "+url);
                }
            }
        }
    }
}
