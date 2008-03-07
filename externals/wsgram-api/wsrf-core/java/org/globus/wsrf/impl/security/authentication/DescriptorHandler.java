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
package org.globus.wsrf.impl.security.authentication;

import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.SecureResourcePropertiesHelper;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityConfig;

import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceContextException;

import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.utils.ContextUtils;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;

/**
 * Handler used for enforcing security policy on server side. Parses
 * and initalized all relevant
 * <code>SecurityDescriptor</code>. Resource security policy is used,
 * if not set, service security policy is used. 
 */
// GT3-specific handler
public abstract class DescriptorHandler extends BasicHandler {

    private static Log logger =
        LogFactory.getLog(DescriptorHandler.class.getName());

    public void invoke(MessageContext msgCtx) throws AxisFault {

        String servicePath = ContextUtils.getTargetServicePath(msgCtx);

        if (servicePath == null) {
            return;
        }

        logger.debug("Service path " + servicePath);

        // get resource
        Resource resource = null;
        try {
            ResourceContext context = 
                ResourceContext.getResourceContext(msgCtx);
            resource = context.getResource();
        } catch (ResourceContextException exp) {
            // FIXME quiet catch
            logger.debug("Resource does not exist ", exp);
            resource = null;
        } catch (ResourceException exp) {
            // FIXME quiet catch
            logger.debug("Resource does not exist ", exp);
            resource = null;
        }

        ResourceSecurityDescriptor resDesc = null;
        if (resource != null) {
            resDesc = SecureResourcePropertiesHelper
                .getResourceSecDescriptor(resource);
        }
        
        ServiceSecurityDescriptor desc = null;
        try {
            desc = (ServiceSecurityDescriptor)ServiceSecurityConfig
                .getSecurityDescriptor(servicePath);
        } catch (ConfigException e) {
            throw AxisFault.makeFault(e);
        }
        
        // No auth done, so no authz.
        if ((resDesc == null) && (desc == null)) {
            msgCtx.setProperty(Constants.AUTHZ_REQUIRED, Boolean.FALSE);
        }
        
        handle(msgCtx, resDesc, desc, servicePath);
    }
    
    public abstract void handle(MessageContext msgCtx, 
                                ResourceSecurityDescriptor resDesc,
                                ServiceSecurityDescriptor desc,
                                String servicePath) throws AxisFault;
}
