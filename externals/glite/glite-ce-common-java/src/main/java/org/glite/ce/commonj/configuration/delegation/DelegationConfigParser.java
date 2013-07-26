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
 * Version info: $Id: DelegationConfigParser.java,v 1.2 2008/06/09 09:47:10 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.delegation;

import org.apache.log4j.Logger;
import org.glite.ce.commonj.configuration.basic.ConfigItem;
import org.glite.ce.commonj.configuration.basic.ConfigParser;
import org.glite.ce.commonj.configuration.basic.TriggerManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public class DelegationConfigParser implements ConfigParser {
    private static final Logger logger = Logger.getLogger(DelegationConfigParser.class.getName());
    private static final String DELEGATION_DATABASE_LABEL = "database";
    private static final String DELEGATION_FACTORY_LABEL = "factory";
    private static final String DELEGATION_STORAGE_LABEL = "storage";
    private static final String DELEGATION_KEYSIZE_LABEL = "key_size";

    public DelegationConfigParser(TriggerManager tMan) {}

    public String getObjectName(String uri, String name, String qName, Attributes attributes) throws SAXParseException {
        if (qName.equals("delegation")) {
            return "delegation";
        }
        return null;
    }

    public void startElement(String uri, String name, String qName, Attributes attributes, ConfigItem prevObj) throws SAXParseException {
        if (qName.equals("delegation")) {
            Long now = new Long(System.currentTimeMillis());
            boolean configChanged = false;
            
            DelegationConfig delegationConfig = null;
            
            if (prevObj.getContent() == null) {
                delegationConfig = new DelegationConfig();
                prevObj.setContent(delegationConfig);
                prevObj.put("creationtime", now);
                prevObj.put("category", "delegation");
            } else { 
                delegationConfig = (DelegationConfig)prevObj.getContent();
            }
            
            String tmp = attributes.getValue(DELEGATION_FACTORY_LABEL);
            if (tmp == null) {
                logger.error("Missing " + DELEGATION_FACTORY_LABEL + " attribute on cream configuration file");
                throw new SAXParseException("Missing " + DELEGATION_FACTORY_LABEL + " attribute on cream configuration file", null);
            }
            delegationConfig.setDelegationFactory(tmp);

            tmp = attributes.getValue(DELEGATION_DATABASE_LABEL);
            if (tmp == null) {
                logger.error("Missing " + DELEGATION_DATABASE_LABEL + " attribute on cream configuration file");
                throw new SAXParseException("Missing " + DELEGATION_DATABASE_LABEL + " attribute on cream configuration file", null);
            }
            delegationConfig.setDelegationDatabase(tmp);

            tmp = attributes.getValue(DELEGATION_STORAGE_LABEL);
            if (tmp == null) {
                logger.error("Missing " + DELEGATION_STORAGE_LABEL + " attribute on cream configuration file");
                throw new SAXParseException("Missing " + DELEGATION_STORAGE_LABEL + " attribute on cream configuration file", null);
            }
            delegationConfig.setDelegationStorage(tmp);
            
            tmp = attributes.getValue(DELEGATION_KEYSIZE_LABEL);
            if (tmp != null) {
                try {
                    delegationConfig.setDelegationKeySize(Integer.parseInt(tmp));
                } catch(Throwable  t) {
                    delegationConfig.setDelegationKeySize(2048);
                }
            }
        }
    }

    public void endElement(String uri, String name, String qName, ConfigItem currentObj) throws SAXParseException {
    }

    public void characters(char[] chars, int start, int length, ConfigItem currentContext) throws SAXParseException {}

}
