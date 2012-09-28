/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 */

package org.glite.ce.commonj.authz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import org.glite.ce.commonj.configuration.basic.ConfigItem;
import org.glite.ce.commonj.configuration.basic.ConfigParser;
import org.glite.ce.commonj.configuration.basic.TriggerManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.apache.log4j.Logger;

public class AuthZConfigParser implements ConfigParser {
    private static final Logger logger = Logger.getLogger(AuthZConfigParser.class.getName());

    protected String chainName;
    protected ArrayList<ServiceInterceptor> plugins;
    protected ServiceInterceptor currentPlugin;
    protected TriggerManager triggerManager;
    protected ClassLoader loader;

    public AuthZConfigParser(TriggerManager tMan) {
        triggerManager = tMan;
        loader = this.getClass().getClassLoader();
    }

    public String getObjectName(String uri, String name, String qName, Attributes attributes) throws SAXParseException {
        if (qName.equals("authzchain")) {
            String result = attributes.getValue("name");

            if (result == null) {
                throw new SAXParseException("Missing authZ chain name", null);
            }

            return result;
        }
        return null;
    }

    public void startElement(String uri, String name, String qName, Attributes attributes, ConfigItem prevObj) throws SAXParseException {
        if (qName.equals("authzchain")) {
            chainName = attributes.getValue("name");
            plugins = new ArrayList<ServiceInterceptor>();            
        } else if (qName.equals("plugin")) {
            try {
                Class<?> interceptorClass = Class.forName(attributes.getValue("classname"));
                Class<?>[] constrArgClass = new Class<?>[] { String.class };
                Constructor<?> constr = interceptorClass.getConstructor(constrArgClass);
                Object[] constrArgValue = new Object[] { attributes.getValue("name") };
                currentPlugin = (ServiceInterceptor) constr.newInstance(constrArgValue);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                throw new SAXParseException("Cannot load interceptor chain: " + ex.getMessage(), null);
            }            
        } else if (qName.equals("parameter")) {
            String pName = attributes.getValue("name");
            if (pName == null) {
                throw new SAXParseException("Missing name attribute for parameter", null);
            }

            String pValue = attributes.getValue("value");
            if (pValue == null) {
                throw new SAXParseException("Missing value for parameter " + pName, null);
            }

            try {
                currentPlugin.setProperty(pName, pValue);
            } catch (InitializeException initEx) {
                throw new SAXParseException(initEx.getMessage(), null);
            }
        }
    }

    public void endElement(String uri, String name, String qName, ConfigItem currentObj) throws SAXParseException {
        if (qName.equals("authzchain")) {
            Long tmpl = new Long(System.currentTimeMillis());
            boolean changed = false;

            ServiceAuthorizationChain oldChain = (ServiceAuthorizationChain) currentObj.getContent();
            ServiceAuthorizationChain newChain = new ServiceAuthorizationChain();
            newChain.initialize(chainName, plugins);

            if (oldChain == null) {
                currentObj.put("creationtime", tmpl);
                currentObj.put("category", "authzchain");
                logger.debug("Register new service chain " + chainName);
                changed = true;
            } else if (!newChain.equals(oldChain)) {
                logger.debug("Detected changes in service chain: " + oldChain.getId());
                changed = true;

                List<File> oldTriggers = oldChain.getAllTriggers();

                for(File oldTrigger : oldTriggers) {
                    triggerManager.removeTrigger(oldTrigger);
                    logger.debug("Removed trigger " + oldTrigger.toString());
                }
            }

            if (changed) {
                currentObj.setContent(newChain);
                currentObj.put("modificationtime", tmpl);

                List<File> newTriggers = newChain.getAllTriggers();
                
                for(File newTrigger : newTriggers) {
                    triggerManager.addTrigger(newTrigger);
                    logger.debug("Added trigger " + newTrigger.toString());
                }
            }
        } else if (qName.equals("plugin")) {
            logger.debug("Inserted new plugin: " + currentPlugin.getId());
            plugins.add(currentPlugin);
        }
    }

    public void characters(char[] chars, int start, int length, ConfigItem currentContext) throws SAXParseException {
    }
}
