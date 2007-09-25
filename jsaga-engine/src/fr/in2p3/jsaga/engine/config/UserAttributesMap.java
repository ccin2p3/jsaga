package fr.in2p3.jsaga.engine.config;

import fr.in2p3.jsaga.engine.config.attributes.UserAttributesParser;
import fr.in2p3.jsaga.engine.schema.config.Attribute;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserAttributesMap
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UserAttributesMap {
    private UserAttributesParser m_parser;

    public UserAttributesMap() throws ConfigurationException {
        m_parser = new UserAttributesParser();
        m_parser.parse();
    }

    public Attribute[] update(Attribute[] attributes, String id) {
        Properties userAttributes = m_parser.getUserAttributes(id);
        if (userAttributes != null) {
            List newAttributes = new ArrayList();

            // modify/remove existing attributes (and remove them from user attributes)
            for (int i=0; attributes!=null && i<attributes.length; i++) {
                Attribute attr = attributes[i];
                String newValue = (String) userAttributes.remove(attr.getName());
                if (newValue != null) {
                    // attribute exist
                    if (!newValue.equals("")) {
                        // attribute is modified
                        attr.setValue(newValue);
                        newAttributes.add(attr);
                    } else {
                        // attribute is removed
                    }
                } else {
                    // attribute is unchanged
                    newAttributes.add(attr);
                }
            }

            // add remaining user attributes
            if (userAttributes.size() > 0) {
                for (Iterator it=userAttributes.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String newName = (String) entry.getKey();
                    String newValue = (String) entry.getValue();
                    if (!newValue.equals("")) {
                        // attribute is added
                        Attribute attr = new Attribute();
                        attr.setName(newName);
                        attr.setValue(newValue);
                        newAttributes.add(attr);
                    } else {
                        // attribute is ignored
                    }
                }
            }

            return (Attribute[]) newAttributes.toArray(new Attribute[newAttributes.size()]);
        } else {
            return null;
        }
    }
}
