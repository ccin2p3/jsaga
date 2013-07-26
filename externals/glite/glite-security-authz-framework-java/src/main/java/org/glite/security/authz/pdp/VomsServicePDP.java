package org.glite.security.authz.pdp;


/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://public.eu-egee.org/partners/ for details on the copyright
 * holders.For license conditions see the license file or http://www.eu-egee.org/license.html
 *
 */
/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 * Modified and redistributed under the terms of the Apache Public
 * License, found at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * @author Mehran Ahsant
 * @author Yuri Demchenko
 * @author Trygve Aspelien
 * @author Håkon Sagehaug
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.log4j.Logger;
import org.glite.security.authz.AuthorizationException;
import org.glite.security.authz.AuthzUtil;
import org.glite.security.authz.ChainConfig;
import org.glite.security.authz.CloseException;
import org.glite.security.authz.InitializeException;
import org.glite.security.authz.InvalidPolicyException;
import org.glite.security.authz.PIPAttribute;
import org.glite.security.authz.ServicePDP;
import org.glite.security.authz.VomsPDPPolicy;
import org.glite.voms.VOMSAttribute;
import org.w3c.dom.Node;


public class VomsServicePDP implements ServicePDP {
    public static final String[] CONFIG_LOCATIONS = {
            "", ".", "/etc", "/etc/grid-security/voms"
        };
    private static Logger logger = Logger.getLogger(VomsServicePDP.class.getName());

    /**
     * Property used to set in-memory grid map.
     */
    public static final String VOMS_PDP_POLICY = "vomsPDPPolicy";

    /**
     * Property used to set grid map file name.
     */
    public static final String ATTR_SECURITY_CONFIG_FILE = "attrAuthzConfigFile";
    private VomsPDPPolicy vomsPolicy = null;

    /* attrs authz list */
    private String attrConfigFileName = null;
    private File attrConfigFile = null;
    private String[] attrs = null;
    private long attrConfigLastModified = 0;

    public String[] getPolicyNames() {
        return null;
    }

    public Node getPolicy(Node node) throws InvalidPolicyException {
        return null;
    }

    public Node setPolicy(Node node) throws InvalidPolicyException {
        return null;
    }

    /**
     * this operation is called by the PDP Framework whenever the application
     * needs to call secured operations. The PDP should return true if the
     * local policy allows the subject to invoke the operation. If the PDP
     * has no local knowledge about whether the operation is allowed or not
     * it should return false to allow other PDPs and PIPs in the chain to
     * continue the evaluation. Obligations to be read by other PIPs or PDPs
     * may be set as attributes in the Subject credentials.
     * @param peerSubject authenticated client subject with credentials
     *                    and attributes
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @return true if user was found, otherwise false
     * @throws AuthorizationException if an exception occured during evaluation
     */
    public boolean isPermitted(Subject peer, MessageContext msgCtx, QName op)
        throws AuthorizationException {
        boolean attrPassed = false;

        String operation = op.toString();
        String peerIdentity = AuthzUtil.getIdentity(peer);

        logger.debug("Operation " + operation + " called by subject: " +
            peerIdentity);

        Set credsPIP = peer.getPublicCredentials(PIPAttribute.class);
        
        for (Iterator iterator = credsPIP.iterator();iterator.hasNext();) {
        	PIPAttribute pipAttribute = (PIPAttribute) iterator.next();
        	logger.debug("Found pip attribute named" + pipAttribute.getName());
	           
        	VOMSAttribute vomsAttribute = (VOMSAttribute) pipAttribute.getValue();           	
        	Vector fqanList = (Vector) vomsAttribute.getFullyQualifiedAttributes();
        	
        	for (int j = 0; j < fqanList.size(); j++) {
        		String attr = (String) fqanList.get(j);
        		logger.debug("checking attribute " + attr);
        		
        		if(this.vomsPolicy != null){
        			Iterator attrs = this.vomsPolicy.getAttrs();
                	String policyAttr = null;
                	
                	//current algorithm only cares if there is one passing attribute
                	while ((attrPassed == false) && (attrs.hasNext())) {
                		policyAttr = (String) attrs.next();
                		
                		// Fix globbing attributes
                		policyAttr = "^" +policyAttr.replaceAll("\\*", ".*").replaceAll("\\?", ".")+"$";
                		logger.debug("found an attribute among in-memory policy: " +
                				policyAttr);
                		
               			for (int i = 0; j < fqanList.size(); j++) {
               				String fqan = (String) fqanList.get(i);
                			if (fqan.matches(policyAttr)) {
               					attrPassed = true;        						
               					break;
               				}
                		}
                	}			
        		}else{
        			try {
	        			if (checkAttrFile(attr) == false) {
	        				logger.debug("Attribute denied: " + attr);
	        			} else {
	        				//current algorithm only cares if there is one passing attribute
	        				attrPassed = true;	
	        				break;
	        			}
	        		} catch (IOException e) {
	        			throw new AuthorizationException("", e);
	        		}
        		}        		
        	}           
        }

        if (attrPassed) {
            logger.info("ACCEPTED: Operation " + operation + " called " +
               "by subject: " + peerIdentity);

            return true;
        } else {
            logger.info("DENIED: Operation " + operation + " called " +
                "by subject: " + peerIdentity);

            return false;
        }
    }

    /**
     * initializes the interceptor with configuration information that are
     * valid up until the point when close is called.
     * @param config holding interceptor specific configuration values, that
     *               may be obtained using the name paramter
     * @param name the name that should be used to access all the interceptor
     *             local configuration
     * @param id the id in common for all interceptors in a chain (it is valid
     *          up until close is called)
     *          if close is not called the interceptor may assume that the id
     *          still exists after a process restart
     * @throws InitializeException if vomspdp was not found
     */
    public void initialize(ChainConfig config, String name, String id)
        throws InitializeException {
        if (config == null) {
            throw new InitializeException(
                "no configuration object (ChainConfig)found");
        }

        // We first check for VomsPDPPolicy 
        Object vomsPolicy = config.getProperty(name, VOMS_PDP_POLICY);

        if (vomsPolicy != null) {
            logger.debug("voms policy in-memory exists");

            try {
                this.vomsPolicy = (VomsPDPPolicy) vomsPolicy;

                return;
            } catch (ClassCastException e) {
                logger.debug("vomsPDPPolicy is present but it is not the " +
                    "proper class, programmer error");
                throw new InitializeException("no vomsPDPPolicy configuration");
            }
        }

        // Otherwise, use static files configured in security desc.
        Object configFile = config.getProperty(name, ATTR_SECURITY_CONFIG_FILE);

        if (configFile == null) {
            logger.warn("no attribute authz file configuration");

            return;
        }

        this.attrConfigFileName = (String) configFile;
        this.attrConfigFile = findConfigFile(this.attrConfigFileName);

        if (this.attrConfigFile != null) {
            try {
                this.attrs = readConfigFile(this.attrConfigFile);
                this.attrConfigLastModified = this.attrConfigFile.lastModified();
            } catch (IOException e) {
                throw new InitializeException(e.toString());
            }
        } else {
            throw new InitializeException("No attribute authorization file");
        }
    }

    private boolean checkAttrFile(String attr) throws IOException {
        /* if InitializeException is ignored from initialize */
        if (this.attrConfigFile == null) {
            this.attrConfigFile = findConfigFile(this.attrConfigFileName);
        }

        if (this.attrConfigFile == null) {
            logger.warn("noSecConfig " + this.attrConfigFileName);

            return false;
        }

        // If config file has been modified, re-read all user rights
        if (this.attrConfigLastModified < this.attrConfigFile.lastModified()) {
            logger.debug("file has been modified: " +
                this.attrConfigLastModified + " < " +
                this.attrConfigFile.lastModified());
            this.attrs = readConfigFile(this.attrConfigFile);
            this.attrConfigLastModified = this.attrConfigFile.lastModified();
        }

        String rePolicyAttr = null;

        for (int i = 0; i < this.attrs.length; i++) {
            // Fix globbing attributes
            rePolicyAttr = "^" +
                this.attrs[i].trim().replaceAll("\\*", ".*").replaceAll("\\?",
                    ".") + "$";

            if (attr.matches(rePolicyAttr)) {
                logger.debug("Attribute accepted: " + attr);

                return true;
            }
        }

        return false;
    }

    protected String[] readConfigFile(File authzFile) throws IOException {
        TextFile file = new TextFile(authzFile.getAbsolutePath());
        String[] result = new String[file.size()];

        for (int i = 0; i < file.size(); i++) {
            result[i] = (String) file.get(i);
        }

        if (logger.isDebugEnabled()) {
            for (int i = 0; i < result.length; i++) {
                logger.debug("result[" + i + "] = " + result[i]);
            }
        }

        return result;
    }

    public void close() throws CloseException {
    }

    protected File findConfigFile(String fileName) {
        if (fileName == null) {
            return null;
        }

        File configFile = null;

        for (int i = 0; i < CONFIG_LOCATIONS.length; i++) {
            String filePath = (CONFIG_LOCATIONS[i].equals("") ? fileName
                                                              : (CONFIG_LOCATIONS[i] +
                File.separator + fileName));
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

    private static String gRead(String fileName) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        String s;

        while ((s = in.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }

        in.close();

        return sb.toString();
    }

    private static boolean checkAccess(String content, String[] baddns,
        String[] badattrs) {
        return simpleCheck(content, baddns, badattrs);
    }

    private static boolean checkAuthz(String content, String[] baddns,
        String[] badattrs) {
        return simpleCheck(content, baddns, badattrs);
    }

    private static boolean simpleCheck(String content, String[] baddns,
        String[] badattrs) {
        logger.debug("content = " + content);

        for (int i = 0; i < baddns.length; i++) {
            if (content.equals(baddns[i])) {
                logger.debug(baddns[i] + " is banned");

                return false;
            }
        }

        for (int i = 0; i < badattrs.length; i++) {
            if (content.equals(badattrs[i])) {
                logger.debug(badattrs[i] + " is banned");

                return false;
            }
        }

        // not banned
        return true;
    }

    private class TextFile extends ArrayList {
        //via Eckel
        private TextFile(String fileName) throws IOException {
            super(Arrays.asList(gRead(fileName).split("\n")));
        }
    }
}
