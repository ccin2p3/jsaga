package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.schema.config.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SystemPropertiesAttributesParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 * Update attributes with system properties
 */
public class SystemPropertiesAttributesParser implements AttributesParser {
    private EffectiveConfig m_config;
    private Pattern m_pattern;

    /** constructor */
    public SystemPropertiesAttributesParser(EffectiveConfig config) {
        m_config = config;
        m_pattern = Pattern.compile("\\$\\{");
    }

    public void updateAttributes() throws ConfigurationException {
        // root
        if (m_config.getLocalIntermediary() == null) {
            m_config.setLocalIntermediary(System.getProperty("java.io.tmpdir"));
        }

        // contexts
        for (int c=0; c<m_config.getContextCount(); c++) {
            this.updateAttributes(m_config.getContext(c));
        }

        // protocols
        for (int p=0; p<m_config.getProtocolCount(); p++) {
            Protocol protocol = m_config.getProtocol(p);
            for (int i=0; i<protocol.getDataServiceCount(); i++) {
                this.updateAttributes(protocol.getDataService(i));
            }
        }

        // execution
        for (int e=0; e<m_config.getExecutionCount(); e++) {
            Execution execution = m_config.getExecution(e);
            for (int i=0; i<execution.getJobServiceCount(); i++) {
                this.updateAttributes(execution.getJobService(i));
            }
        }
    }

    private void updateAttributes(ObjectType object) throws ConfigurationException {
        for (int a=0; a<object.getAttributeCount(); a++) {
            Attribute attribute = object.getAttribute(a);
            String value = attribute.getValue();
            String[] propertyNames = extractSystemPropertyNames(value);
            if (propertyNames.length > 0) {
                for (int i=0; i<propertyNames.length; i++) {
                    String propertyValue = System.getProperty(propertyNames[i]);
                    if (propertyValue != null) {
                        String regexp = "\\$\\{"+propertyNames[i]+"\\}";
                        String replacement = Matcher.quoteReplacement(propertyValue);
                        value = value.replaceFirst(regexp, replacement);
                    }
                }
                attribute.setValue(value);
            }
        }
    }

    /** can not use regexp because it does not enable capturing several B in (A(B))+ */
    private String[] extractSystemPropertyNames(String value) throws ConfigurationException {
        if (value != null) {
            String[] array = m_pattern.split(value);
            String[] propertyNames = new String[array.length - 1];
            for (int i=1; i<array.length; i++) {
                int position = array[i].indexOf('}');
                switch(position) {
                    case -1:
                        throw new ConfigurationException("Unclosed system property in attribute value: "+value);
                    case 0:
                        throw new ConfigurationException("Empy ${} in attribute value: "+value);
                    default:
                        propertyNames[i-1] = array[i].substring(0, position);
                }
            }
            return propertyNames;
        } else {
            return new String[]{};
        }
    }
}
