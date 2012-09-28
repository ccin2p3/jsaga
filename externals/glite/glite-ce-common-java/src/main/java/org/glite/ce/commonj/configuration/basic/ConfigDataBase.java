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
 * Version info: $Id: ConfigDataBase.java,v 1.5 2009/03/09 14:19:26 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigDataBase extends DefaultHandler implements TriggerManager {
    private static final Logger logger = Logger.getLogger(ConfigDataBase.class.getName());

    public static final String NAME_PATH_SEPARATOR = ";";

    private ConfigItem root;

    private String configFilename;

    private long configTimestamp;

    private Hashtable<Object, Object> parserResource;

    private Stack<ConfigParser> subParsers;

    private Stack<ConfigItem> contextStack;

    private String currentPath;

    private ConfigParser currentParser;

    private ConfigItem currentItem;

    private List<File> triggers;

    public ConfigDataBase(String confFile, Hashtable<Object, Object> resource) throws IOException {
        configFilename = confFile;
        parserResource = resource;

        root = null;

        triggers = new ArrayList<File>(0);
        
        File tmpf = new File(configFilename);
        
        triggers.add(tmpf);
        
        configTimestamp = tmpf.lastModified();
        
        if (configTimestamp == 0) {
            throw new IOException("Cannot state config file timestamp");
        }
        
        try {
            parseConfigFile();
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }
        
        configTimestamp = getMaxTriggerTS();

        root.put("lastModificationTime", new Long(configTimestamp));
    }

    public synchronized void startElement(String uri, String name, String qName, Attributes attributes) throws SAXParseException {
        currentPath = currentPath + "/" + qName;

        logger.debug("Current path is " + currentPath);

        if (parserResource.containsKey(currentPath)) {
            if (currentParser != null) {
                logger.debug("Push parser " + currentParser.getClass().getName());
                subParsers.push(currentParser);
            }

            try {
                currentParser = createSubParser((String)parserResource.get(currentPath));
                logger.debug("Loaded parser " + currentParser.getClass().getName());
            } catch (Exception ex) {
                throw new SAXParseException(ex.getMessage(), null);
            }
        }

        String objName = currentParser.getObjectName(uri, name, qName, attributes);

        if (objName != null) {
            if (objName.indexOf(NAME_PATH_SEPARATOR) >= 0) {
                throw new SAXParseException("Cannot use separator char in name", null);
            }
            
            if (currentItem != null) {
                ConfigItem parentItem = currentItem;
                contextStack.push(parentItem);

                ConfigNodes nodes = (ConfigNodes) parentItem.getContent();
                currentItem = (ConfigItem) nodes.get(objName);
                
                if (currentItem == null) {
                    currentItem = new ConfigItem(objName, currentPath);
                    currentItem.put("creationtime", new Long(System.currentTimeMillis()));
                    nodes.putConfigItem(currentItem);
                    logger.debug("Creating node " + objName);
                }
            } else if (root == null) {
                currentItem = new ConfigItem(objName, currentPath);
                currentItem.put("creationtime", new Long(System.currentTimeMillis()));
                root = currentItem;
                logger.debug("Creating root node " + objName);
            } else {
                currentItem = root;
            }
            currentItem.put("scantime", new Long(System.currentTimeMillis()));
        }

        currentParser.startElement(uri, name, qName, attributes, currentItem);
    }

    public synchronized void endElement(String uri, String name, String qName) throws SAXParseException {
        currentParser.endElement(uri, name, qName, currentItem);

        if (currentItem.getPath().equals(currentPath)) {
            Object tmpo = currentItem.getContent();
            if (tmpo instanceof ConfigNodes) {
                ConfigNodes nodes = (ConfigNodes) tmpo;
                Iterator children = nodes.keySet().iterator();

                String[] childToRemove = new String[nodes.size() + 1];

                for (int j = 0; children.hasNext();) {
                    String child = (String) children.next();
                    ConfigItem tmpItem = (ConfigItem) nodes.get(child);

                    long stime = ((Long) tmpItem.get("scantime")).longValue();
                    
                    if (stime < configTimestamp) {
                        // nodes.remove(child);
                        childToRemove[j] = child;
                        j++;
                        childToRemove[j] = null;
                    }
                }

                for (int j = 0; j < nodes.size(); j++) {
                    if (childToRemove[j] == null) {
                        break;
                    }
                    nodes.remove(childToRemove[j]);
                }
            }

            if (contextStack.empty()) {
                currentItem = null;
            } else {
                currentItem = (ConfigItem) contextStack.pop();
            }
        }

        if (parserResource.containsKey(currentPath)) {
            logger.debug("Trying to pop parser for path " + currentPath);
            
            if (!subParsers.empty()) {
                currentParser = (ConfigParser) subParsers.pop();
            }
        }

        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        currentParser.characters(ch, start, length, currentItem);
    }

    public synchronized ConfigItem query(String namePath) throws ConfigQueryException {
        checkConfiguration();

        if (namePath == null) {
            throw new ConfigQueryException("Bad path name");
        }
        if (root == null) {
            throw new ConfigQueryException("Configuration database is empty");
        }

        return (ConfigItem) this.findItem(namePath.trim()).clone();
    }

    public synchronized ConfigItem[] query(String namePath, String[] attrNames, Object[] attrValues) throws ConfigQueryException {
        checkConfiguration();

        if (namePath == null) {
            throw new ConfigQueryException("Bad path name");
        }
        if (root == null) {
            throw new ConfigQueryException("Configuration database is empty");
        }

        ConfigItem targetCtx = this.findItem(namePath.trim());
        Object tmpo = targetCtx.getContent();

        if (tmpo == null || !(tmpo instanceof ConfigNodes)) {
            throw new ConfigQueryException("Name is not a context: " + namePath);
        }

        ConfigNodes children = (ConfigNodes) tmpo;
        List<ConfigItem> selectedNodes = new ArrayList<ConfigItem>(0);
        
        for(String key : children.keySet()) {
            ConfigItem node = (ConfigItem) children.get(key);
            
            for (int k = 0; k < attrNames.length; k++) {
                if (attrValues[k].equals(node.get(attrNames[k]))) {
                    selectedNodes.add((ConfigItem)node.clone());
                }
            }
        }

        ConfigItem[] result = new ConfigItem[selectedNodes.size()];
        result = (ConfigItem[]) selectedNodes.toArray(result);

        return result;
    }

    public void addTrigger(File file) {
        logger.debug("Added trigger for " + file.getAbsolutePath());
        triggers.add(file);
    }

    public void removeTrigger(File file) {
        logger.debug("Removed trigger for " + file.getAbsolutePath());
        triggers.remove(file);
    }

    public Iterator getAllTriggers() {
        return triggers.iterator();
    }

    private ConfigItem findItem(String namePath) throws ConfigQueryException {
        if (namePath.equals("")) {
            return root;
        }

        String[] tokens = namePath.split(NAME_PATH_SEPARATOR);
        ConfigItem cItem = root;
        
        for (int k = 0; k < tokens.length; k++) {
            Object tmpo = cItem.getContent();
            
            if (tmpo == null || !(tmpo instanceof ConfigNodes)) {
                throw new ConfigQueryException("Bad name path " + tmpo.getClass().getName());
            }
            
            cItem = (ConfigItem) ((ConfigNodes) tmpo).get(tokens[k]);
            
            if (cItem == null) {
                throw new ConfigQueryException("Missing context " + tokens[k]);
            }
        }

        return cItem;
    }

    private long getMaxTriggerTS() {
        long result = 0;
        
        for (File tmpf : triggers) {
            long tmpl = tmpf.lastModified();

            if (tmpl == 0) {
                logger.warn("Cannot retrieve timestamp for: " + tmpf.toString());
                continue;
            }

            if (tmpl > result) {
                result = tmpl;
            } 
        }
        return result;
    }

    private void checkConfiguration() {
        long newTS = getMaxTriggerTS();
        
        if (newTS > configTimestamp) {
            // ConfigItem rollBack = (root!= null) ? root.deepClone() : null;
            try {
                configTimestamp = newTS;

                parseConfigFile();

                root.put("lastModificationTime", new Long(configTimestamp));
            } catch (Throwable th) {
                logger.error(th.getMessage(), th);
                // root = rollBack;
            }
        }
    }

    private synchronized void parseConfigFile() throws SAXException, ParserConfigurationException, IOException {
        subParsers = new Stack<ConfigParser>();
        currentParser = null;
        contextStack = new Stack<ConfigItem>();
        currentPath = "";
        currentItem = null;

        FileReader xmlReader = null;
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            SAXParser sp = spf.newSAXParser();

            xmlReader = new FileReader(configFilename);
            InputSource input = new InputSource(xmlReader);
            input.setSystemId("file://" + configFilename);

            logger.debug("Parsing file " + configFilename);

            sp.parse(input, this);
        } finally {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    private ConfigParser createSubParser(String className) throws NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {

        Class parserClass = Class.forName(className);
        Class[] constrArgs = new Class[] { TriggerManager.class };
        Constructor parserConstr = parserClass.getConstructor(constrArgs);
        return (ConfigParser) parserConstr.newInstance(new Object[] { this });
    }
}
