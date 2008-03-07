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
package org.globus.wsrf.impl.security.descriptor;

import org.globus.wsrf.config.ConfigException;

import org.globus.security.gridmap.GridMap;

import org.globus.wsrf.Resource;

import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class has API that determines the appropriate security property
 * for an invocation. It first looks for resource property, if not set,
 * service property, if not set, looks for container property. 
 */
public class SecurityPropertiesHelper {

    private static Log logger =
        LogFactory.getLog(SecurityPropertiesHelper.class.getName());

    // Initialization is also done. 
    public static Integer getContextLifetime(String servicePath, 
                                             Resource resource) 
        throws ConfigException {
        
        logger.debug("getContext lifetime " + servicePath);

        Integer contextLifetime = 
            SecureResourcePropertiesHelper.getContextLifetime(resource);

        if (contextLifetime != null) 
            return contextLifetime;

        ServiceSecurityDescriptor desc = getSecurityDesc(servicePath);

        if (desc != null)
            contextLifetime = desc.getContextLifetime();
        if (contextLifetime == null) {
            ContainerSecurityDescriptor containerDesc =
                ContainerSecurityConfig.getConfig().getSecurityDescriptor();
            if (containerDesc != null)
                contextLifetime = containerDesc.getContextLifetime();
        }
        return contextLifetime;
    }

    // Initialization is also done.
    public static Boolean getRejectLimitedProxyState(String servicePath,
                                                     Resource res) 
        throws ConfigException {
        logger.debug("getRejectLimitedProxyState " + servicePath);

        String state = 
            SecureResourcePropertiesHelper.getRejectLimitedProxyState(res);
        logger.debug("State is " + state);

        if (state == null) {
            ServiceSecurityDescriptor desc = getSecurityDesc(servicePath);
            if (desc != null) {
                state = desc.getRejectLimitedProxyState();
            }
            logger.debug("Service state is " + state);
            
            if (state == null) {
                ContainerSecurityDescriptor containerDesc =
                    ContainerSecurityConfig.getConfig()
                    .getSecurityDescriptor();
                if (containerDesc != null) {
                    logger.debug("Container desc is not null");
                    state = containerDesc.getRejectLimitedProxyState();
                }
            }
        }
        logger.debug("State is " + state);
        if (state == null)
            return Boolean.FALSE;
        else 
            return Boolean.valueOf(state);
    }

    // No initialization is done.
    public static String getReplayAttackWindow(String servicePath, 
                                               Resource resource) 
        throws ConfigException {

        logger.debug("getReplayAttackWindow for " + servicePath);
        
        String replayWin = 
            SecureResourcePropertiesHelper.getReplayAttackWindow(resource);
        
        if (replayWin != null) {
            logger.debug("Replay Window is " + replayWin);
            return replayWin;
        }
        
        ServiceSecurityDescriptor desc = 
            (ServiceSecurityDescriptor)ServiceSecurityConfig
            .getSecurityDescriptor(servicePath);
        if (desc != null) {
            replayWin = desc.getReplayAttackWindow();
        }
        logger.debug("Replay Window is " + replayWin);
        
        if (replayWin == null) {
            ContainerSecurityDescriptor containerDesc =
                ContainerSecurityConfig.getConfig().getSecurityDescriptor();
            if (containerDesc != null) {
                logger.debug("Container descriptor is null");
                replayWin = containerDesc.getReplayAttackWindow();
            }
        }
        return replayWin;
    }

    // No initialization is done.
    public static String getReplayAttackFilter(String servicePath, 
                                               Resource resource) 
        throws ConfigException {

        logger.debug("getReplayAttackFilyer for " + servicePath);
        
        String replayWin = 
            SecureResourcePropertiesHelper.getReplayAttackFilter(resource);
        
        if (replayWin != null) {
            logger.debug("Replay Window is " + replayWin);
            return replayWin;
        }
        
        ServiceSecurityDescriptor desc = 
            (ServiceSecurityDescriptor)ServiceSecurityConfig
            .getSecurityDescriptor(servicePath);
        if (desc != null) {
            replayWin = desc.getReplayAttackFilter();
        }
        logger.debug("Replay Window is " + replayWin);
        
        if (replayWin == null) {
            ContainerSecurityDescriptor containerDesc =
                ContainerSecurityConfig.getConfig().getSecurityDescriptor();
            if (containerDesc != null)
                replayWin = containerDesc.getReplayAttackFilter();
        }
        return replayWin;
    }
    
    // No Initialization is done.
    public static String getAuthorizationType(String servicePath, 
                                              Resource resource) 
        throws ConfigException {

        logger.debug("GetAuthzType " + servicePath);
        String authz = 
            SecureResourcePropertiesHelper.getAuthorizationType(resource);

        if (authz != null) {
            logger.debug("Auth is " + authz);
            return authz;
        }
        
        ServiceSecurityDescriptor desc = 
            (ServiceSecurityDescriptor)ServiceSecurityConfig
            .getSecurityDescriptor(servicePath);
        if (desc != null) {
            authz = desc.getAuthz();
        }
        logger.debug("Auth is " + authz);
        if (authz == null) {
            ContainerSecurityDescriptor containerDesc =
                ContainerSecurityConfig.getConfig().getSecurityDescriptor();
            if (containerDesc != null)
                authz = containerDesc.getAuthz();
        }
        return authz;
    }

    // No Initialization is done.
    public static GridMap getGridMap(String servicePath, Resource resource) 
        throws ConfigException {

        logger.debug("getGridMap " + servicePath);
        GridMap obj = 
            SecureResourcePropertiesHelper.getGridMap(resource);
        
        if (obj != null)
            return obj;
        
        obj = ServiceSecurityConfig.getGridMap(servicePath);
        if (obj == null) {
            logger.debug("Service gridmap null");
            obj = ContainerSecurityConfig.getConfig().getGridMap();
        }
        return obj;
    }

    // No Initialization is done.
    public static ServiceAuthorizationChain getAuthzChain(String servicePath, 
                                                          Resource resource) 
        throws ConfigException {

        logger.debug("get authz chain " + servicePath);
        ServiceAuthorizationChain obj = 
            SecureResourcePropertiesHelper.getAuthzChain(resource);
        
        if (obj != null) {
            logger.debug("Resource authz is not null");
            return obj;
        }

        logger.debug("resource authz is null " + servicePath);
        obj = ServiceSecurityConfig.getAuthzChain(servicePath);
        if (obj == null) {
            logger.debug("service authz is null");        
            obj = ContainerSecurityConfig.getConfig().getAuthzChain();
        } else {
            logger.debug("service authz is not null");
        }   
        return obj;
    }

    public static boolean gridMapPresent(String servicePath, Resource resource)
        throws ConfigException { 

        return (getGridMap(servicePath, resource) != null);
    }

    private static ServiceSecurityDescriptor 
        getSecurityDesc(String servicePath) throws ConfigException {
        // initialize stuff for the target service
        try {
            ServiceSecurityConfig.initialize(servicePath);
        } catch (Exception exp) {
            throw new ConfigException(exp);
        }
        return (ServiceSecurityDescriptor)ServiceSecurityConfig
            .getSecurityDescriptor(servicePath);
    }
}
