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

import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;

import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.axis.gsi.GSIConstants;

/**
 * Handler used to set appropriate authorization properties when transport
 * security is used. It picks up the authorization properties from
 * those that are used for Secure Message/Conversation mechanism
 */
public class ClientPropertiesHandler extends BasicHandler
{
    public void invoke(MessageContext messageContext) throws AxisFault
    {
        if(messageContext.getTransportName().equals("https"))
        {
            Authorization authzMethod = null;
            if((authzMethod = (Authorization)
                messageContext.getProperty(Constants.AUTHORIZATION)) != null &&
               messageContext.getProperty(
                   GSIConstants.GSI_AUTHORIZATION) == null)
            {
                if(authzMethod instanceof SelfAuthorization)
                {
                    messageContext.setProperty(
                        GSIConstants.GSI_AUTHORIZATION,
                        org.globus.gsi.gssapi.auth.SelfAuthorization
                        .getInstance());
                }
                else if(authzMethod instanceof HostAuthorization)
                {
                    messageContext.setProperty(
                        GSIConstants.GSI_AUTHORIZATION,
                        org.globus.gsi.gssapi.auth.HostAuthorization
                        .getInstance());
                }
                else if(authzMethod instanceof NoAuthorization)
                {
                    messageContext.setProperty(
                        GSIConstants.GSI_AUTHORIZATION,
                        org.globus.gsi.gssapi.auth.NoAuthorization
                        .getInstance());
                }
                else if(authzMethod instanceof IdentityAuthorization)
                {
                    messageContext.setProperty(
                        GSIConstants.GSI_AUTHORIZATION,
                        new org.globus.gsi.gssapi.auth.IdentityAuthorization(
                            ((IdentityAuthorization) authzMethod)
                            .getIdentity()));
                }
            }
        }
    }
}
