package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.engine.schema.config.types.AttributeSourceType;

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
public abstract class AbstractAttributesParser implements AttributesParser {
    protected EffectiveConfig m_config;

    /** constructor */
    public AbstractAttributesParser(EffectiveConfig config) {
        m_config = config;
    }

    public abstract void updateAttributes() throws ConfigurationException;

    protected void updateOrInsertAttribute(String propertyName, String propertyValue) {
        String[] array = propertyName.split("\\.");
        if (array.length == 2) {
            String contextRef = array[0];
            String attributeName = array[1];
            Context context = this.getContext(contextRef);
            if (context != null) {
                updateOrInsertAttribute(context, attributeName, propertyValue);
            }
        } else if (array.length == 3) {
            String scheme = array[0];
            String serviceRef = array[1];
            String attributeName = array[2];
            DataService dataService = this.getDataService(scheme, serviceRef);
            if (dataService != null) {
                updateOrInsertAttribute(dataService, attributeName, propertyValue);
            } else {
                JobService jobService = this.getJobService(scheme, serviceRef);
                if (jobService != null) {
                    updateOrInsertAttribute(jobService, attributeName, propertyValue);
                }
            }
        }
    }

    private static void updateOrInsertAttribute(ObjectType object, String attributeName, String attributeValue) {
        Attribute attribute = getOrCreateAttribute(object, attributeName);
        attribute.setValue(attributeValue);
        attribute.setSource(AttributeSourceType.USERPROPERTIES);
    }
    private static Attribute getOrCreateAttribute(ObjectType object, String attributeName) {
        for (int i=0; i<object.getAttributeCount(); i++) {
            Attribute attribute = object.getAttribute(i);
            if (attribute.getName().equals(attributeName)) {
                return attribute;
            }
        }
        Attribute attribute = new Attribute();
        attribute.setName(attributeName);
        attribute.setSource(AttributeSourceType.USERPROPERTIES);
        object.addAttribute(attribute);
        return attribute;
    }

    private Context getContext(String contextRef) {
        for (int i=0; i<m_config.getContextCount(); i++) {
            Context context = m_config.getContext(i);
            if (context.getName().equals(contextRef)) {
                return context;
            }
        }
        return null;
    }

    private DataService getDataService(String scheme, String serviceRef) {
        Protocol protocol = this.getProtocol(scheme);
        if (protocol != null) {
            for (int i=0; i<protocol.getDataServiceCount(); i++) {
                DataService service = protocol.getDataService(i);
                if (service.getName().equals(serviceRef)) {
                    return service;
                }
            }
        }
        return null;
    }
    private Protocol getProtocol(String scheme) {
        for (int i=0; i<m_config.getProtocolCount(); i++) {
            Protocol protocol = m_config.getProtocol(i);
            if (protocol.getScheme().equals(scheme) || arrayContains(protocol.getSchemeAlias(),scheme)) {
                return protocol;
            }
        }
        return null;
    }

    private JobService getJobService(String scheme, String serviceRef) {
        Execution execution = this.getExecution(scheme);
        if (execution != null) {
            for (int i=0; i<execution.getJobServiceCount(); i++) {
                JobService service = execution.getJobService(i);
                if (service.getName().equals(serviceRef)) {
                    return service;
                }
            }
        }
        return null;
    }
    private Execution getExecution(String scheme) {
        for (int i=0; i<m_config.getExecutionCount(); i++) {
            Execution execution = m_config.getExecution(i);
            if (execution.getScheme().equals(scheme) || arrayContains(execution.getSchemeAlias(),scheme)) {
                return execution;
            }
        }
        return null;
    }

    private static boolean arrayContains(String[] array, String value) {
        for (int i=0; array!=null && i<array.length; i++) {
            if (array[i].equals(value)) {
                return true;
            }
        }
        return false;
    }
}
