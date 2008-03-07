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

import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import org.globus.wsrf.impl.security.descriptor.AuthMethod;
import org.globus.wsrf.impl.security.descriptor.NoneAuthMethod;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityDescriptor;
import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.security.SecurityException;

/**
 * Handler that enforces authentication requirements of the service.
 */
public class AuthHandler extends DescriptorHandler {

    private static Log logger = LogFactory.getLog(AuthHandler.class.getName());

    public static final List DEFAULT_AUTH_METHODS = new Vector();

    // No authentication by default
    static {
        DEFAULT_AUTH_METHODS.add(NoneAuthMethod.getInstance());
    }

    public void handle(MessageContext msgCtx,
                       ResourceSecurityDescriptor resDesc,
                       ServiceSecurityDescriptor desc,
                       String servicePath)
        throws AxisFault {

        if ((resDesc == null) && (desc == null)) {
            logger.debug("Resource desc and service desc are null, return");
            return;
        }

        QName opName = null;
        try {
            opName = AuthUtil.getOperationName(msgCtx);
        } catch (SecurityException exp) {
            throw AxisFault.makeFault(exp);
        }

        logger.debug("Method invoked " + opName);

        msgCtx.setProperty(Constants.OPERATION_NAME, opName);

        List authMethods = null;

        ServiceSecurityDescriptor descUsed = null;

        if (resDesc != null) {
            authMethods = resDesc.getAuthMethods(opName);
            if (authMethods == null) {
                authMethods = resDesc.getDefaultAuthMethods();
            }
            descUsed = resDesc;
        }

        if ((authMethods == null) && (desc != null)) {
            authMethods = desc.getAuthMethods(opName);
            if (authMethods == null) {
                authMethods = desc.getDefaultAuthMethods();
            }
            descUsed = desc;
        }

        if (authMethods == null) {
            authMethods = DEFAULT_AUTH_METHODS;
            descUsed = (desc == null) ? desc : resDesc;
        }

        logger.debug("Checking authentication methods");
        int size = authMethods.size();

        Boolean authzReq = Boolean.FALSE;
        for (int i=0; i<size; i++) {
            if (!authMethods.get(i).equals(NoneAuthMethod.getInstance())) {
                authzReq = Boolean.TRUE;
                break;
            }
        }

        msgCtx.setProperty(Constants.AUTHZ_REQUIRED, authzReq);

        // It has only no authentication, so no point verifying
        if (Boolean.FALSE.equals(authzReq)) {
            logger.debug("No auth, so no point verifiying");
            return;
        }

        AuthMethod method;
        for (int i = 0; i < size; i++) {
            method = (AuthMethod) authMethods.get(i);
            logger.debug("Configured Method " + method.getName());
            if (method.isAuthenticated(msgCtx)) {
                logger.debug("Returned true, valid mechanism");
                return;
            }
        }

        throw new AxisFault(
            descUsed.getRequiredAuthMethodsErrorMessage(authMethods, opName)
        );
    }
}
