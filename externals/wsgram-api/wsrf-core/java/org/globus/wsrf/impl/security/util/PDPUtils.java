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
package org.globus.wsrf.impl.security.util;

import org.globus.wsrf.config.ConfigException;

import org.apache.axis.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.Stub;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;

import org.globus.wsrf.security.authorization.PDPConfig;
import org.globus.wsrf.security.authorization.PDPConstants;

import org.globus.wsrf.impl.security.authorization.ContainerPDPConfig;
import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;
import org.globus.wsrf.impl.security.authorization.ServicePropertiesPDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;

/**
 * Collection of utility methods used by PDP based services
 */
public class PDPUtils {
    static Log logger =
        LogFactory.getLog(PDPUtils.class.getName());
    /**
     * sets a map of trusted targets (subject DN string should be both key 
     * and value of the map).
     * The map can be constructed from a configuration file using 
     * the {@link #loadTrustedTargets} method.
     * @param stub proxy object used to invoke the service
     * @param trustedTargets (String, String) map of Subject DNs
     */ 
    public static void setTrustedTargets(Stub stub, HashMap trustedTargets) {
	stub._setProperty(PDPConstants.TRUSTED_TARGETS,
			  trustedTargets);
    }
    
    /**
     * if some action needs to be performed by the application it can
     * be checked against a ServiceAuthorizationChain
     * explicitly. Subject is picked up from  the current thread.
     *
     * @param action qname of the operation to be invoked


    public static void authorizeAction(QName action) throws Exception {
        //	try {
	    MessageContext currentMessage = MessageContext.getCurrentContext();
            String service = ContextUtils.getTargetServicePath(currentMessage);
             // FIXME: Store PDPContants.ACTION in JNDI ?
            
             //	    properties.setProperty(PDPConstants.ACTION,action);
             // FIXME: get Auzth propety from JNDI
             ServiceAuthorization auth = (ServiceAuthorization) 
                 currentMessage.getProperty(Authorization.AUTHZ_CLASS);
             Subject subject = 
                 (Subject) currentMessage.getProperty(org.globus.wsrf.impl.security.authentication.Constants.PEER_SUBJECT);
	    auth.authorize(subject, currentMessage, service);
            //       } catch (Exception e) {
            //	   throw FaultHelper.makeFault(FaultType.class, e);
            //       }
    }
    */

    /**
     * loads a map of trusted targets from a file in order to later be used 
     * by {@link #setTrustedTargets}. The map is a (String, String) tuple
     * of keyed Subject DNs 
     * @param fileName file name containing a list of dn (one per row)
     * @return map of Subject DNs 
     */ 
    public static HashMap loadTrustedTargets(String fileName) 
	throws Exception {
        HashMap allowedSubjects = new HashMap();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
            String subject = in.readLine();
            while (subject != null) {
	        if (logger.isDebugEnabled()) {
                    logger.debug("Adding trusted target: " + subject);
	        }
	        allowedSubjects.put(subject, subject);
                subject = in.readLine();
            }
        } finally {
            if (in != null) { try { in.close(); } catch (Exception e) {} }
        }
        return allowedSubjects;
     }

    public static ServiceAuthorizationChain 
        getServiceAuthzChain(PDPConfig config, String id)
        throws ConfigException {    

        ServiceAuthorizationChain authzChain = new ServiceAuthorizationChain();
        try {
            authzChain.initialize(config, null, id);
        } catch (InitializeException exp) {
            throw new ConfigException(exp);
        }
        return authzChain;
    }

    public static ServiceAuthorizationChain 
        getServiceAuthzChain(String authzChain, String servicePath)
        throws ConfigException {
        
        if (authzChain == null) 
            return null;
        
        String pdpChain = AuthUtil.substitutePDPNames(authzChain);
        MessageContext messageContext = MessageContext.getCurrentContext();
        ServicePropertiesPDPConfig config = 
            new ServicePropertiesPDPConfig(messageContext, 
                                           servicePath, 
                                           pdpChain);
        return getServiceAuthzChain(config, servicePath);
    }
    
    public static ServiceAuthorizationChain 
        getContainerAuthzChain(String authzChain, String id) 
        throws ConfigException {
        
        if (authzChain == null) 
            return null;
        
        String pdpChain = AuthUtil.substitutePDPNames(authzChain);
        MessageContext messageContext = MessageContext.getCurrentContext();
        ContainerPDPConfig config = new ContainerPDPConfig(pdpChain,
                                                           messageContext);
        return getServiceAuthzChain(config, id);
    }
}
