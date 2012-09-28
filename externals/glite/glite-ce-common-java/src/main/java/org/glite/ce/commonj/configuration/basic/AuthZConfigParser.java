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
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 * Version info: $Id: AuthZConfigParser.java,v 1.4 2009/03/09 13:42:06 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public class AuthZConfigParser implements ConfigParser {
    private static final Logger logger = Logger.getLogger(AuthZConfigParser.class.getName());
    public static final String PLUGIN_CLASS_LABEL = "_classname_";

    private LinkedHashMap<String, HashMap<String, Object>> configItems = null;
    private HashMap<String, Object> currentParams;
    private ArrayList<String> argItems;
    private String currentParamName;
    private String currentParamClassName;

    public AuthZConfigParser(TriggerManager tMan) {
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
            configItems = new LinkedHashMap<String, HashMap<String, Object>>(0);

            boolean configChanged = false;
            String tmps = attributes.getValue("operations");
            
            String[] operations = tmps == null? new String[] { "default" } : tmps.trim().split("\\s*,\\s*");
            
            HashSet<String> opTable = (HashSet<String>) prevObj.get("operations");
            if (opTable == null || opTable.size() != operations.length) {
                configChanged = true;
            } else {
                for (int k = 0; k < operations.length; k++) {
                    configChanged = !opTable.contains(operations[k]);
                }
            }

            if (configChanged) {
                opTable = new HashSet<String>(operations.length);
                
                for (int k = 0; k < operations.length; k++) {
                    opTable.add(operations[k]);
                }
                
                prevObj.put("operations", opTable);
                prevObj.put("modificationtime", new Long(System.currentTimeMillis()));
            }
        } else if (qName.equals("plugin")) {
            currentParams = new HashMap<String, Object>();
            currentParams.put(PLUGIN_CLASS_LABEL, attributes.getValue("classname"));
            configItems.put(attributes.getValue("name"), currentParams);

        } else if (qName.equals("parameter")) {
            currentParamName = attributes.getValue("name");
            currentParamClassName = attributes.getValue("type");
            
            if (currentParamClassName == null || currentParamClassName.equals("java.lang.String")) {
                String tmps = attributes.getValue("value");
                
                if (tmps != null) {
                    currentParams.put(currentParamName, tmps);
                } else {
                    throw new SAXParseException("String value required for " + currentParamName, null);
                }
                currentParamClassName = null;
            } else {
                argItems = new ArrayList<String>(0);
            }
        } else if (qName.equals("argument")) {
            argItems.add(attributes.getValue("value"));
        }
    }

    public void endElement(String uri, String name, String qName, ConfigItem currentObj) throws SAXParseException {
        if (qName.equals("authzchain")) {
            boolean configChanged = false;
            Long tmpl = new Long(System.currentTimeMillis());

            if (currentObj.getContent() == null) {
                currentObj.put("creationtime", tmpl);
                currentObj.put("category", "authzchain");
                configChanged = true;
            } else {
                try {
                    AuthZConfigChain chain = (AuthZConfigChain) currentObj.getContent();
                    configChanged = !chain.equals(configItems);
                } catch (Exception ex) {
                    throw new SAXParseException(ex.getMessage(), null);
                }
            }

            if (configChanged) {
                currentObj.put("modificationtime", tmpl);
                try {
                    AuthZConfigChain chain = new AuthZConfigChain(currentObj.getName(), this.getClass().getName(), configItems);

                    currentObj.setContent(chain);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new SAXParseException(ex.getMessage(), null);
                }
            }
        } else if (qName.equals("parameter")) {
            if (currentParamClassName != null) {
                Object confObj = getConfObjInstance(currentParamClassName, argItems);
                currentParams.put(currentParamName, confObj);
            }
        }
    }

    public void characters(char[] chars, int start, int length, ConfigItem currentContext) throws SAXParseException {
    }

    private Object getConfObjInstance(String className, ArrayList args) throws SAXParseException {
        try {
            Class objClass = Class.forName(className);
            Class[] constrArgClass = new Class[args.size()];
            Object[] constrArgValue = new Object[args.size()];
            
            for (int k = 0; k < args.size(); k++) {
                constrArgValue[k] = args.get(k);
                constrArgClass[k] = constrArgValue[k].getClass();
            }

            Constructor constr = objClass.getConstructor(constrArgClass);

            return constr.newInstance(constrArgValue);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new SAXParseException(ex.getMessage(), null);
        }
    }
}
