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

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.SecureResourcePropertiesHelper;
import org.globus.wsrf.impl.security.descriptor.SecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.SecurityPropertiesHelper;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityDescriptor;
import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.impl.security.util.PDPUtils;
import org.globus.wsrf.utils.ContextUtils;

/**
 * Enforces the service authorization policy.
 */
public class AuthorizationHandler extends BasicHandler {

    private static Log logger =
        LogFactory.getLog(AuthorizationHandler.class.getName());

    public void invoke(MessageContext messageContext) throws AxisFault {

        logger.debug("Authorization");

        Subject subject =
            (Subject) messageContext.getProperty(Constants.PEER_SUBJECT);

        // If subject is null, no authorization is done.
        if (subject == null) {
            logger.debug("No authenticaiton done, so no authz");
            return;
        }

        String servicePath = ContextUtils.getTargetServicePath(messageContext);
        // If null will fail further along chain, so return.
        if (servicePath == null) {
            return;
        }
        logger.debug("Service path " + servicePath);

        // If no auth mechanism was enforced for this operation, no
        // need to do authz.
        Boolean authzReq =
            (Boolean)messageContext.getProperty(Constants.AUTHZ_REQUIRED);
        if ((authzReq != null) && (authzReq.equals(Boolean.FALSE))) {
            logger.debug("Authz not required, since auth not enforced");
            return;
        }

        // get resource
        Resource resource = null;
        try {
            ResourceContext context =
                ResourceContext.getResourceContext(messageContext);
            resource = context.getResource();
        } catch (ResourceContextException exp) {
            // FIXME: quiet catch, set resource to null
            resource = null;
            logger.debug("Error getting resource/may not exist", exp);
        } catch (ResourceException exp) {
            // FIXME: quiet catch, set resource to null
            resource = null;
            logger.debug("Error getting resource/may not exist", exp);
        }
        logger.debug("Resource is null: " + (resource == null));

        // Subject is not null, but check if
        // resource/service/container required security. If no
        // security descriptor was present, return.
        ServiceAuthorizationChain authzChain = null;
        SecurityDescriptor secDesc = null;
        if (resource != null) {
            secDesc = (ServiceSecurityDescriptor)SecureResourcePropertiesHelper
                .getResourceSecDescriptor(resource);
            if (secDesc != null) {
                // use helper class, so initialization is done if
                // required.
                try {
                    authzChain =
                        SecureResourcePropertiesHelper.getAuthzChain(resource);
                } catch (ConfigException exp) {
                    throw AxisFault.makeFault(exp);
                }
            }
        }
        logger.debug("Sec desc after resource is " + (secDesc != null));

        if (authzChain == null) {
            try {
                secDesc = (ServiceSecurityDescriptor)ServiceSecurityConfig
                    .getSecurityDescriptor(servicePath);
            } catch (ConfigException exp) {
                throw AxisFault.makeFault(exp);
            }
            if (secDesc != null) {
                authzChain = secDesc.getAuthzChain();
            }
        }

        logger.debug("Sec desc after service is " + (secDesc != null));

        if (authzChain == null) {
            try {
                ContainerSecurityConfig config =
                    ContainerSecurityConfig.getConfig();
                secDesc = config.getSecurityDescriptor();
                if (secDesc != null) {
                    authzChain = secDesc.getAuthzChain();
                }
                // Check if insecure container
                if (authzChain == null) {
                    if (config.getSecurityDescriptorFile() == null) {
                        logger.debug("Insecure container");
                        secDesc = null;
                    }
                }
            } catch (ConfigException exp) {
                throw AxisFault.makeFault(exp);
            }
        }

        logger.debug("Sec desc after container is " + (secDesc != null));

        // No security descriptor, return
        if (secDesc == null) {
            logger.debug("Insecure setting, return");
            return;
        }

        // Security descriptor present and subject not null, resort to
        // default authorization
        if (authzChain == null) {
            logger.debug("Sec desc is present, default authz chain");
            String authzString = getDefaultAuthzChain(servicePath,
                                                      resource);
            try {
                authzChain = PDPUtils.getServiceAuthzChain(authzString,
                                                           servicePath);
            } catch (ConfigException exp) {
                throw AxisFault.makeFault(exp);
            }
        }

        // AuthzChain cannot be null here
        logger.debug("Invoking authorize on authz chain");
        try {
            authzChain.authorize(subject, messageContext, servicePath);
        } catch (AuthorizationException e) {
            throw AxisFault.makeFault(e);
        }
    }

    private String getDefaultAuthzChain(String servicePath,
                                        Resource resource) {

        String interceptor = null;
        boolean gridMapPresent;
        try {
            gridMapPresent
                = SecurityPropertiesHelper.gridMapPresent(servicePath,
                                                          resource);
        } catch (ConfigException exp) {
            // FIXME: throw error ?
            gridMapPresent = false;
        }

        if (!gridMapPresent) {
            interceptor = AuthUtil.getPDPName(Authorization.AUTHZ_SELF);
        } else {
            interceptor = AuthUtil.getPDPName(Authorization.AUTHZ_GRIDMAP);
        }
        return interceptor;
    }
}
