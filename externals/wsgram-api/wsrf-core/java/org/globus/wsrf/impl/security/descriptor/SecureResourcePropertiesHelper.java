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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import org.globus.gsi.gridmap.GridMap;

import org.globus.wsrf.config.ConfigException;


import org.globus.wsrf.Resource;

import org.globus.wsrf.security.SecureResource;

import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

/**
 * Helper API for retrieving security properties of secure resources.
 */
public class SecureResourcePropertiesHelper {

    private static Log logger =
        LogFactory.getLog(SecureResourcePropertiesHelper.class.getName());

    public static Subject getResourceSubject(Resource resource) 
        throws ConfigException {
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            ResourceSecurityConfig config = 
                new ResourceSecurityConfig(resDesc);
            return config.getSubject();
        }
        return null;
    }

    public static String getReplayAttackWindow(Resource resource) 
        throws ConfigException {
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            ResourceSecurityConfig config = 
                new ResourceSecurityConfig(resDesc);
            return config.getSecurityDescriptor().getReplayAttackWindow();
        }
        return null;
    }

    public static String getReplayAttackFilter(Resource resource) 
        throws ConfigException {
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            ResourceSecurityConfig config = 
                new ResourceSecurityConfig(resDesc);
            return config.getSecurityDescriptor().getReplayAttackFilter();
        }
        return null;
    }

    public static Integer getContextLifetime(Resource resource) 
        throws ConfigException {
        Integer contextLifetime = null;
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            contextLifetime = resDesc.getContextLifetime();
        }
        return contextLifetime;
    }

    public static String getRejectLimitedProxyState(Resource resource) 
        throws ConfigException {

        String state = null;
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            state = resDesc.getRejectLimitedProxyState();
        }
        return state;
    }

    public static String getAuthorizationType(Resource resource) 
        throws ConfigException {

        String authz = null;
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            authz = resDesc.getAuthz();
        }
        return authz;
    }
    
    public static ServiceAuthorizationChain getAuthzChain(Resource resource)
        throws ConfigException {

        ServiceAuthorizationChain authz = null;
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            ResourceSecurityConfig config = 
                new ResourceSecurityConfig(resDesc);
            authz = config.getAuthzChain();
        }
        return authz;
    }

    public static GridMap getGridMap(Resource resource)
        throws ConfigException {

        GridMap obj = null;
        ResourceSecurityDescriptor resDesc = 
            getResourceSecDescriptor(resource);
        if (resDesc != null) {
            ResourceSecurityConfig config = 
                new ResourceSecurityConfig(resDesc);
            obj = config.getGridMap();
        }
        return obj;
    }
    
    public static ResourceSecurityDescriptor 
        getResourceSecDescriptor(Resource resource) {
        
        if (resource == null) {
            logger.debug("resource is null");
            return null;
        }
        
        if (SecureResource.class.isAssignableFrom(resource.getClass())) {
            logger.debug("secure resource, retruning descriptor");
            return ((SecureResource)resource).getSecurityDescriptor();
        } else {
            return null;
        }
    }
}
