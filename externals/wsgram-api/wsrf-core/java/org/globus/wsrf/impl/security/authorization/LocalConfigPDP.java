/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.impl.security.authorization;

import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Node;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;
import org.globus.wsrf.security.authorization.PDPConstants;

import org.globus.wsrf.impl.security.util.AuthUtil;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * A <code>PDP<code> implementation that is intended to be used
 * by services to easily bootstrap an ACL into the PDP. The deployment
 * descriptor of the service should have an authzConfigFile parameter
 * that points to a file with mappings between users and their allowed
 * operations. One user mapping is specified per line and multiple
 * operations are separated by semicolon (;). The file may be modified
 * without restarting the hosting environemnt <p> Example:<p>
 * <code><pre> /O\=Grid/O\=Globus/OU\=Sample\
 * Org/CN\=AdminUser={http://www.gridforum.org/namespaces/2003/03/OGSI}findServiceData;
 * \ {http://www.gridforum.org/namespaces/2003/03/OGSI}setServiceData;
 * /O\=Grid/O\=Globus/OU\=Sample\
 * Org/CN\=User={http://www.gridforum.org/namespaces/2003/03/OGSI}findServiceData
 * </pre></code> <p> Note that white spaces and equal signs (=) need
 * to be escaped with backslash (\)<p> Further note that the mappings
 * are on a service level, and this pdp is thus appropriate to use in
 * e.g. a ServiceAuthorizationChain
 * @see PDP
 * @see org.globus.wsrf.security.authorization.Interceptor
 */ 
public class LocalConfigPDP implements PDP {

    private static I18n i18n = 
        I18n.getI18n(PDPConstants.RESOURCE,
                     LocalConfigPDP.class.getClassLoader());
    public static final String SECURITY_CONFIG_FILE = 
        "authzConfigFile";
    public static final String DEFAULT_SECURITY_CONFIG_FILE = 
        "service-authz.conf";    
    
    public static final String[] CONFIG_LOCATIONS = 
    {"",".", "/etc", "/etc/grid-security"};
    
    private static Log logger = 
        LogFactory.getLog(LocalConfigPDP.class.getName());

    private String configFileName = null;
    private File configFile = null;
    private Map userRightsMap = null;
    private long configLastModified = 0;
    
    public LocalConfigPDP() {
        this.userRightsMap = new HashMap();
    }
    
    public void initialize(PDPConfig config, String name, String id) 
            throws InitializeException {
        this.configFileName = getConfigFileName(config, name);  
        this.configFile = findConfigFile(this.configFileName);  
        if (this.configFile != null) {
            this.userRightsMap = readConfigFile(this.configFile);           
        }
    }

    public String[] getPolicyNames() {
        return null;
    }

    public boolean isPermitted(Subject peer, MessageContext context, QName op)
        throws AuthorizationException {
        String operation = op.toString();
        String peerIdentity = AuthUtil.getIdentity(peer);

        // Find security config file (unless already found)
        if (this.configFile == null) {
            this.configFile = findConfigFile(this.configFileName);   
        }
        
        if (this.configFile == null) {
            logger.warn(i18n.getMessage("noSecConfig"));            
            return false;
        }
        
        logger.debug("Last modification time: " + configFile.lastModified());
        
        // If config file has been modified, re-read all user rights
        if (this.configLastModified < this.configFile.lastModified()) {
            this.userRightsMap = readConfigFile(this.configFile);
        }

        Object allowedOpsList = this.userRightsMap.get(peerIdentity);
        if (allowedOpsList == null) {
            logger.warn(i18n.getMessage("noSubjFile", peerIdentity));
            return false;
        }               
        if (((List)allowedOpsList).contains(operation)) {
            return true;
        }
        
        return false; 
    }
    
    private File findConfigFile(String fileName) {
        File configFile = null;
        for(int i = 0; i < CONFIG_LOCATIONS.length; i++) {
            String filePath = (CONFIG_LOCATIONS[i].equals("") ? fileName :
                               CONFIG_LOCATIONS[i] + File.separator +
                               fileName);
            configFile = new File(filePath);
            logger.debug("Trying authz file: " + configFile.getAbsolutePath());
            if (configFile.exists()) {
                logger.debug("Security config file found: " +
                             configFile.getAbsolutePath());
                break;
            } else {
                configFile = null;
            }
        }
        return configFile;
    }

    private String getConfigFileName(PDPConfig config, String name) {
        // Get config file name from service properties
        Object configFile = config.getProperty(name, SECURITY_CONFIG_FILE);

        return ((configFile == null) ?
                DEFAULT_SECURITY_CONFIG_FILE : (String) configFile);
    }

    private Map readConfigFile(File configFile) {
        logger.debug("Reading config file: " + configFile.getAbsolutePath());
        HashMap authzMap = new HashMap();
        Properties accessMap = new Properties();
        try {
            accessMap.load(new FileInputStream(configFile));
            this.configLastModified = this.configFile.lastModified();
        } catch (IOException e) {
            logger.warn("Could not read security config file " +
                        configFile.getAbsolutePath());
            return new HashMap();
        }
        // Set up authorization map from loaded config file
        Enumeration subjects = accessMap.propertyNames();
        while (subjects.hasMoreElements()) {
            String subject = (String) subjects.nextElement();
            String rights = accessMap.getProperty(subject);
            logger.debug("  Subject: \"" + subject + "\"\n"+
                         "  Rights: \"" + rights + "\"");
            // Get each allowed operation
            ArrayList allowedOps = new ArrayList();
            StringTokenizer opsTokenizer = new StringTokenizer(rights, ",;");
            while(opsTokenizer.hasMoreTokens()) {
                String op = opsTokenizer.nextToken();
                allowedOps.add(op.trim());
            }
            authzMap.put(subject, allowedOps);
        }
        return authzMap;
    }          
    
    public Node getPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }
    
    public Node setPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }
    
    public void close() throws CloseException {
    }
}
