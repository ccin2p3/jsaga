package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.config.ConfigurationException;

import java.io.*;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FilePropertiesAttributesParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FilePropertiesAttributesParser extends AbstractAttributesParser {
    public static final File FILE = new File(new File(System.getProperty("user.home")), ".globus/jsaga-user.properties");

    public FilePropertiesAttributesParser(Map map) {
        super(map);
    }
    
    public void parse() throws ConfigurationException {
        if (FILE.exists()) {
            // load properties file
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(FILE));
            } catch (IOException e) {
                throw new ConfigurationException(e);
            }

            // convert to map of properties
            for (Iterator it=prop.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                String propName = (String) entry.getKey();
                String attributeValue = (String) entry.getValue();
                String[] array = propName.split("\\.");
                if (array.length == 2) {
                    String id = array[0];
                    String attributeName = array[1];
                    super.addAttribute(id, attributeName, attributeValue);
                }
            }
        }
    }
}
