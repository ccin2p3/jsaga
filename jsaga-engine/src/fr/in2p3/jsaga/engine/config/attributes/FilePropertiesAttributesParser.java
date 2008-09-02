package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.schema.config.EffectiveConfig;

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
 * Update attributes with user properties file
 */
public class FilePropertiesAttributesParser extends AbstractAttributesParser implements AttributesParser {
    public FilePropertiesAttributesParser(EffectiveConfig config) {
        super(config);
    }
    
    public void updateAttributes() throws ConfigurationException {
        File propFile = getFile();
        if (propFile.exists()) {
            // load properties file
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(propFile));
            } catch (IOException e) {
                throw new ConfigurationException(e);
            }

            // convert to map of properties
            for (Iterator it=prop.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                String propertyName = (String) entry.getKey();
                String propertyValue = (String) entry.getValue();
                super.updateOrInsertAttribute(propertyName, propertyValue);
            }
        }
    }

    private static File s_file;
    public static File getFile() throws ConfigurationException {
        if (s_file == null) {
            // set properties file
            s_file = new File(new File(new File(System.getProperty("user.home")), ".jsaga"), "jsaga-user.properties");

            // create parent directory if needed
            File dir = s_file.getParentFile();
            if (!dir.exists() && !dir.mkdir()) {
                throw new ConfigurationException("Failed to create directory: "+dir);
            }
        }
        return s_file;
    }
}
