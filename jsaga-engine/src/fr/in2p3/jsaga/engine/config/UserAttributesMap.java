package fr.in2p3.jsaga.engine.config;

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
    private static final String[] KNOWN_SYSTEM_PROPERTIES = {
            "java", "awt", "user", "os", "line", "path", "file.separator", "file.encoding"};
    private Map m_map;

    public UserAttributesMap() {
        // set known system properties
        Set propNamesFilter = new HashSet();
        for (int i=0; i< KNOWN_SYSTEM_PROPERTIES.length; i++) {
            propNamesFilter.add(KNOWN_SYSTEM_PROPERTIES[i]);
        }

        // set map
        m_map = new HashMap();
        for (Iterator it=System.getProperties().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String propName = (String) entry.getKey();
            String attributeValue = (String) entry.getValue();
            String[] array = propName.split("\\.");
            if (array.length == 2) {
                String id = array[0];
                String attributeName = array[1];
                if (!propNamesFilter.contains(id) && !propNamesFilter.contains(propName)) {
                    Properties userAttributes = (Properties) m_map.get(id);
                    if (userAttributes == null) {
                        userAttributes = new Properties();
                        m_map.put(id, userAttributes);
                    }
                    userAttributes.setProperty(attributeName, attributeValue);
                }
            }
        }
    }

    public Attribute[] update(Attribute[] attributes, String id) {
        Properties userAttributes = (Properties) m_map.get(id);
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
