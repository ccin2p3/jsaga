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
package org.globus.wsrf.impl.security.authentication.secureconv;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

import org.globus.util.Base64;
import org.globus.ws.trust.BinaryExchangeType;
import org.globus.ws.trust.RequestSecurityTokenResponseType;
import org.globus.ws.trust.RequestSecurityTokenType;
import org.globus.ws.trust.holders.RequestSecurityTokenResponseTypeHolder;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.security.impl.secconv.SecureConversation;

/**
 * Establishes a security context.
 */
public class Authenticator
{

    static Log logger = LogFactory.getLog(Authenticator.class.getName());

    protected String contextId;
    protected GSSContext context;
    protected EndpointReferenceType contextEPR;

    public Authenticator(GSSContext context)
    {
        this.context = context;
    }

    public void authenticate(SecureConversation auth)
        throws GSSException, RemoteException
    {

        byte[] inToken = new byte[0];
        byte[] outToken = null;
        RequestSecurityTokenResponseType response = null;
        RequestSecurityTokenResponseTypeHolder holder =
            new RequestSecurityTokenResponseTypeHolder();
        BinaryExchangeType token = null;
        SecureConversationMessage message = null;
        boolean firstTime = true;

        while(!context.isEstablished())
        {
            outToken = context.initSecContext(inToken, 0, inToken.length);

            if(outToken != null)
            {
                if(firstTime)
                {
                    firstTime = false;

                    RequestSecurityTokenType request =
                        new RequestSecurityTokenType();

                    token = new BinaryExchangeType();

                    token.setValueType(
                        SecureConversationMessage.GSSAPI_GSI_TOKEN_VALUE_TYPE);
                    token.setEncodingType(
                        SecureConversationMessage.BASE64_BINARY_ENCODING);
                    token.set_value(new String(Base64.encode(outToken)));

                    MessageElement[] requestContent = null;
                    try
                    {
                        requestContent =
                            SecureConversationMessage.createMessage(token);
                    }
                    catch(SerializationException e)
                    {
                        throw new RemoteException("", e);
                    }

                    request.set_any(requestContent);
                    response = auth.requestSecurityToken(request);
                    contextId = response.getContext().getSchemeSpecificPart();
                    message =
                        new SecureConversationMessage(response.get_any());
                    try
                    {
                        message.parseMessage();
                    }
                    catch(Exception e)
                    {
                        throw new RemoteException("",e);
                    }
                    token = message.getExchangeToken();
                    inToken = Base64.decode(
                        token.get_value().getBytes());
                }
                else
                {
                    token.set_value(new String(Base64.encode(outToken)));
                    try
                    {
                        response.set_any(
                            SecureConversationMessage.createMessage(token));
                    }
                    catch(SerializationException e)
                    {
                        throw new RemoteException("",e);
                    }
                    holder.value = response;
                    auth.requestSecurityTokenResponse(holder);
                    response = holder.value;
                    message =
                        new SecureConversationMessage(response.get_any());

                    try
                    {
                        message.parseMessage();
                    }
                    catch(Exception e)
                    {
                        throw new RemoteException("",e);
                    }
                    token = message.getExchangeToken();
                    if(token != null)
                    {
                        inToken = Base64.decode(
                            token.get_value().getBytes());
                    }
                }
            }
        }
    }

    public String getContextId()
    {
        return contextId;
    }

    public EndpointReferenceType getContextEPR()
    {
        return contextEPR;
    }

    public GSSContext getContext()
    {
        return context;
    }
}
