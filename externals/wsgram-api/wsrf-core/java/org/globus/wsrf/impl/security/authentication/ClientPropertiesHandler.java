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

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import org.globus.wsrf.config.ConfigException;

import org.globus.axis.gsi.GSIConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.impl.security.descriptor.ClientSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.authorization.IdentityAuthorization;

/**
 * Handler that translates security settings in client security descriptor
 * into properties on the MessageContext.
 */
public class ClientPropertiesHandler extends BasicHandler {

    private static Log logger =
        LogFactory.getLog(ClientPropertiesHandler.class.getName());

    public void invoke(MessageContext msgCtx) throws AxisFault {

        String descFileName =
            (String)msgCtx.getProperty(Constants.CLIENT_DESCRIPTOR_FILE);
        logger.debug("Descriptor file name " + descFileName);

        ClientSecurityDescriptor desc = null;
        if (descFileName == null) {
            desc = (ClientSecurityDescriptor)msgCtx
                .getProperty(Constants.CLIENT_DESCRIPTOR);
        } else {
            try {
                desc = ClientSecurityConfig.initialize(descFileName);
            } catch (ConfigException exp) {
                throw AxisFault.makeFault(exp);
            }
        }

        if (desc == null) {
            logger.debug("Descriptor is null");
            return;
        }

        // Authorization
        Authorization authz = desc.getAuthz();
        org.globus.gsi.gssapi.auth.Authorization gsiAuthz = null;
        msgCtx.setProperty(Constants.AUTHORIZATION, authz);

        if (authz instanceof HostAuthorization) {
            gsiAuthz =
                org.globus.gsi.gssapi.auth.HostAuthorization.getInstance();
        } else if (authz instanceof NoAuthorization) {
            gsiAuthz =
                org.globus.gsi.gssapi.auth.NoAuthorization.getInstance();
        } else if (authz instanceof SelfAuthorization) {
            gsiAuthz =
                org.globus.gsi.gssapi.auth.SelfAuthorization.getInstance();
        } else if (authz instanceof IdentityAuthorization) {
            gsiAuthz =
                new org.globus.gsi.gssapi.auth.IdentityAuthorization(
                    ((IdentityAuthorization) authz).getIdentity());
        }

        msgCtx.setProperty(GSIConstants.GSI_AUTHORIZATION, gsiAuthz);

        // GSI Secure Conv
        msgCtx.setProperty(Constants.GSI_SEC_CONV, desc.getGSISecureConv());
        logger.debug("Secure conv " + desc.getGSISecureConv());
        // Anonymous
        msgCtx.setProperty(Constants.GSI_ANONYMOUS, desc.getAnonymous());

        // Delegation
        msgCtx.setProperty(GSIConstants.GSI_MODE, desc.getDelegation());
        // Credentials
        msgCtx.setProperty(GSIConstants.GSI_CREDENTIALS,
                           desc.getGSSCredential());

        // GSI Secure Msg
        msgCtx.setProperty(Constants.GSI_SEC_MSG, desc.getGSISecureMsg());
        logger.debug("Secure msg " + desc.getGSISecureMsg());
        // Peer credentials
        msgCtx.setProperty(Constants.PEER_SUBJECT, desc.getPeerSubject());

        // GSI Transport
        msgCtx.setProperty(Constants.GSI_TRANSPORT, desc.getGSITransport());

    }
}
