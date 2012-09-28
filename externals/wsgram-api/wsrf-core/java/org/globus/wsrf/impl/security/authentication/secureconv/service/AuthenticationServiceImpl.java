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

import java.rmi.RemoteException;

import javax.security.auth.Subject;
import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.MessageContext;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI;

import org.gridforum.jgss.ExtendedGSSContext;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;

import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.JaasGssUtil;

import org.globus.util.I18n;
import org.globus.util.Base64;

import org.globus.ws.sc.SecurityContextTokenType;
import org.globus.ws.trust.BinaryExchangeType;
import org.globus.ws.trust.RequestSecurityTokenResponseType;
import org.globus.ws.trust.RequestSecurityTokenType;
import org.globus.ws.trust.holders.RequestSecurityTokenResponseTypeHolder;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.impl.ResourceContextImpl;
import org.globus.wsrf.impl.SimpleResourceKey;

import org.globus.wsrf.security.SecurityException;
import org.globus.wsrf.security.SecurityManager;

import org.globus.wsrf.impl.security.descriptor.SecurityPropertiesHelper;
import org.globus.wsrf.impl.security.authentication.secureconv.SecureConversationMessage;

import org.globus.wsrf.security.impl.secconv.SecureConversation;
import org.globus.wsrf.security.impl.secconv.InvalidContextIdFaultType;
import org.globus.wsrf.security.impl.secconv.MalformedMessageFaultType;
import org.globus.wsrf.security.impl.secconv.BinaryExchangeFaultType;
import org.globus.wsrf.security.impl.secconv.EncodingTypeNotSupportedFaultType;
import org.globus.wsrf.security.impl.secconv.RequestTypeNotSupportedFaultType;
import org.globus.wsrf.security.impl.secconv.TokenTypeNotSupportedFaultType;
import org.globus.wsrf.security.impl.secconv.ValueTypeNotSupportedFaultType;

import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.utils.AddressingUtils;

/**
 * This is used by the services to establish a security context. This class must
 * be thread-safe. And must assume stateless for contexts because can have
 * multiple active contexts.
 */
public class AuthenticationServiceImpl
    implements AuthenticationServiceConstants, SecureConversation
{

    static Log logger =
        LogFactory.getLog(AuthenticationServiceImpl.class.getName());

    private static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.error",
                     AuthenticationServiceImpl.class.getClassLoader());

    protected GSSCredential getCredential(
        String servicePath,
        Resource resource)
        throws SecurityException
    {
        Subject s = SecurityManager.getManager().getSubject(servicePath,
                                                            resource);
        logger.debug("Credential on server side " + s);
        return JaasGssUtil.getCredential(s);
    }

    private String getTargetService()
    {
        MessageContext ctx =
            MessageContext.getCurrentContext(); // axis-specific call
        String url = (String) ctx.getProperty(TARGET_SERVICE);
        // if not set get the url for the authentication service
        if(url == null)
        {
            url = ctx.getTargetService();
        }
        logger.debug("Target service " + url);
        return url;
    }


    // Returns the resource object associated with the actual method
    // invocation that is being secured with GSI Secure Conv. No
    // exception is thrown on a getResource call.
    // If a resource cannot be retrieved here.
    // (could be because there is none associated with the call or
    // some other errors that are specific reosurce impl. dependent),
    // then there is no valid resource associated with this invocation
    // and hence no resource security property either.
    private Resource getTargetResource(String servicePath)
        throws SecurityException
    {

        logger.debug("Service path is " + servicePath);
        ResourceContext ctx = null;
        try
        {
            ctx = ResourceContext.getResourceContext();
        }
        catch(ResourceContextException exp)
        {
            throw new SecurityException(exp);
        }

        ((ResourceContextImpl) ctx).setService(servicePath);
        Resource resource = null;
        try
        {
            resource = ctx.getResource();
        }
        catch(ResourceContextException exp)
        {
            // FIXME: quiet catch, set resource to null
            resource = null;
            logger.debug("Error getting resource/may not exist", exp);
        }
        catch(ResourceException exp)
        {
            // FIXME: quiet catch, set resource to null
            resource = null;
            logger.debug("Error getting resource/may not exist", exp);
        }

        logger.debug("Resource is null: " + (resource == null));
        return resource;
    }

    public void requestSecurityTokenResponse(
        RequestSecurityTokenResponseTypeHolder response)
        throws RemoteException,
               MalformedMessageFaultType,
               InvalidContextIdFaultType,
               TokenTypeNotSupportedFaultType,
               ValueTypeNotSupportedFaultType,
               EncodingTypeNotSupportedFaultType,
               BinaryExchangeFaultType,
               RequestTypeNotSupportedFaultType
    {
        RequestSecurityTokenResponseType request = response.value;
        SecurityContext securityContext = null;
        ResourceContext ctx = null;
        SimpleResourceKey key = null;
        MessageElement[] requestElements = request.get_any();
        SecureConversationMessage message = new SecureConversationMessage(
            requestElements);
        BinaryExchangeType gssToken = message.parseMessage();

        String contextId = request.getContext().getSchemeSpecificPart();

        try
        {
            ctx = ResourceContext.getResourceContext();
            SecurityContextHome home =
                (SecurityContextHome) ctx.getResourceHome();
            logger.debug("Context id is " + contextId);
            key = new SimpleResourceKey(home.getKeyTypeName(), contextId);
            securityContext = (SecurityContext) home.find(key);
        }
        catch(Exception e)
        {
            InvalidContextIdFaultType fault =
                new InvalidContextIdFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        }

        GSSContext context = securityContext.getContext();
        byte[] inToken = Base64.decode(gssToken.get_value().getBytes());

        try
        {
            byte[] outToken = context.acceptSecContext(inToken, 0,
                                                       inToken.length);
            if(outToken != null && context.isEstablished())
            {
                SecurityContextTokenType contextToken =
                    createSecurityContextToken(request.getContext(), ctx, key);
                gssToken.set_value(new String(Base64.encode(outToken)));
                request.set_any(SecureConversationMessage.createMessage(
                    gssToken, contextToken));
            }
            else if(outToken != null)
            {
                gssToken.set_value(new String(Base64.encode(outToken)));
                request.set_any(SecureConversationMessage.createMessage(
                    gssToken));
            }
            else if(context.isEstablished())
            {
                SecurityContextTokenType contextToken =
                    createSecurityContextToken(request.getContext(), ctx, key);
                request.set_any(SecureConversationMessage.createMessage(
                    contextToken));
            }
        }
        catch(Exception e)
        {
            BinaryExchangeFaultType fault =
                new BinaryExchangeFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        }
    }

    private SecurityContextTokenType createSecurityContextToken(
        URI contextId, ResourceContext ctx,
        SimpleResourceKey key)
        throws Exception
    {
        SecurityContextTokenType contextToken =
            new SecurityContextTokenType();
        contextToken.setIdentifier(contextId);
        EndpointReferenceType contextEPR =
            AddressingUtils.createEndpointReference(
                ctx.getServiceURL().toString(),
                key);
        SOAPElement contextEPRElement =
            ObjectSerializer.toSOAPElement(
                contextEPR,
                SecureConversationMessage.CONTEXT_EPR_QNAME);
        contextToken.set_any(
            new MessageElement[] {(MessageElement) contextEPRElement});
        return contextToken;
    }

    public RequestSecurityTokenResponseType requestSecurityToken(
        RequestSecurityTokenType request)
        throws RemoteException,
               MalformedMessageFaultType,
               TokenTypeNotSupportedFaultType,
               ValueTypeNotSupportedFaultType,
               EncodingTypeNotSupportedFaultType,
               BinaryExchangeFaultType,
               RequestTypeNotSupportedFaultType
    {
        logger.debug("Enter requestSecurityToken");

        RequestSecurityTokenResponseType response =
            new RequestSecurityTokenResponseType();

        SecureConversationMessage message = new SecureConversationMessage(
            request.get_any());
        BinaryExchangeType gssToken = message.parseMessage();

        GSSManager manager = ExtendedGSSManager.getInstance();
        ExtendedGSSContext context = null;

        // Get target service path
        String servicePath = getTargetService();
        if(servicePath == null)
        {
            BinaryExchangeFaultType fault =
                new BinaryExchangeFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                i18n.getMessage(
                    "noTargetService"));
            throw fault;
        }

        try
        {
            // Get target resource
            Resource resource = getTargetResource(servicePath);

            // get SecurityDescriptor filename
            context = (ExtendedGSSContext) manager.createContext(
                getCredential(servicePath,
                              resource));

            logger.debug("Invoking secure service on " + servicePath);

            Integer reqContextLifetime = null;
            reqContextLifetime =
                SecurityPropertiesHelper.getContextLifetime(servicePath,
                                                            resource);
            if(reqContextLifetime != null)
            {
                logger.debug("Setting context lifetime to "
                             + reqContextLifetime.intValue());
                context.requestLifetime(reqContextLifetime.intValue());
            }

            Boolean rejectLimProxy =
                SecurityPropertiesHelper.getRejectLimitedProxyState(servicePath,
                                                                    resource);
            context.setOption(GSSConstants.REJECT_LIMITED_PROXY,
                              rejectLimProxy);


            // accept both anonymous and other clients
            context.setOption(GSSConstants.ACCEPT_NO_CLIENT_CERTS,
                              Boolean.TRUE);

            ResourceContext ctx = ResourceContext.getResourceContext();
            SecurityContextHome home =
                (SecurityContextHome) ctx.getResourceHome();
            ResourceKey key  = home.create(context);
            response.setContext(new URI("uuid", (String) key.getValue()));
            byte[] inToken = Base64.decode(gssToken.get_value().getBytes());
            byte[] outToken = context.acceptSecContext(inToken, 0,
                                                       inToken.length);
            gssToken.set_value(new String(Base64.encode(outToken)));
            response.set_any(
                SecureConversationMessage.createMessage(gssToken));
        }
        catch(Exception e)
        {
            BinaryExchangeFaultType fault =
                new BinaryExchangeFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        }
        return response;
    }
}

