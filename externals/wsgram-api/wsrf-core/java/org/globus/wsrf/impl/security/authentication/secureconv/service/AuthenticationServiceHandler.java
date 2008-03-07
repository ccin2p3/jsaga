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
package org.globus.wsrf.impl.security.authentication.secureconv.service;

import org.globus.wsrf.config.ContainerConfig;
import org.globus.axis.description.ServiceDescUtil;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This handler is responsible for mapping incoming GSI Secure Conversation
 * requests to the authentication service.
 */
public class AuthenticationServiceHandler extends BasicHandler
    implements AuthenticationServiceConstants {

    static Log logger =
        LogFactory.getLog(AuthenticationServiceHandler.class.getName());

    public void invoke(MessageContext messageContext) throws AxisFault {

        String target = (String) messageContext.getTargetService();

        if ((target != null) && target.endsWith(AUTH_SERVICE_PATH)) {
            try {
                ContainerConfig config = 
                    ContainerConfig.getConfig(messageContext.getAxisEngine());
                String authService = config.getOption(AUTH_SERVICE);
                messageContext.setTargetService(authService);
                target = target.substring(0,
                                          target.length() - AUTH_SERVICE_PATH.length());
                logger.debug("TARGET_SERVICE is set to " + target);
                messageContext.setProperty(TARGET_SERVICE, target);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

            if (messageContext.getService() != null) {
                ServiceDescUtil.resetOperations(messageContext);
            }
        }
    }
}
