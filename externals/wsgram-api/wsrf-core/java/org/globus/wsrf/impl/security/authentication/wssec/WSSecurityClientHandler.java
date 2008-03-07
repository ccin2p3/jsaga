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
package org.globus.wsrf.impl.security.authentication.wssec;

import org.globus.wsrf.impl.security.authentication.Constants;

import org.globus.wsrf.security.SecurityException;

import org.globus.wsrf.impl.security.util.AuthUtil;

import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;

import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

// client-side handler
public class WSSecurityClientHandler extends WSSecurityBasicHandler {

    private static Log log =
        LogFactory.getLog(WSSecurityClientHandler.class.getName());

    // server
    public boolean handleRequest(MessageContext context) {
        // does nothing on the server side
        return true;
    }

    // client
    public boolean handleResponse(MessageContext context) {

        Object xmlSig = context.getProperty(Constants.GSI_SEC_MSG);
        if (xmlSig != null) {
            context.setProperty(Constants.GSI_SEC_MSG, Constants.NONE);
        }

        Object gssSec = context.getProperty(Constants.GSI_SEC_CONV);
        if (gssSec != null) {
            context.setProperty(Constants.GSI_SEC_CONV, Constants.NONE);
        }

        SOAPMessageContext ctx = (SOAPMessageContext) context;
        boolean v = handleMessage(ctx, WSSecurityResponseEngine.getEngine());

        SOAPMessage msg = ctx.getMessage();
        SOAPBody body = null;

        try {
            SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
            body = env.getBody();
        } catch (SOAPException e) {
            log.error("Failed to get envelope", e);
            return false;
        }

        if (!body.hasFault()) {
            try {
                Object gsiSecMsg = context.getProperty(Constants.GSI_SEC_MSG);
                checkGSISecureMessage(xmlSig, gsiSecMsg, context);
                Object gsiSecConv = 
                    context.getProperty(Constants.GSI_SEC_CONV);
                checkGSISecConv(gssSec, gsiSecConv);
            } catch (Exception e) {
                log.error(e);
                throw WSSecurityFault.makeFault(e);
            }
        }

        return v;
    }

    private void checkGSISecureMessage(Object before, Object after,
                                       MessageContext context) 
        throws WSSecurityException, AuthorizationException, SecurityException {

        
        // check of some secure message protection was requested
        if ((before != null) && (before.equals(Constants.SIGNATURE) ||
                                 before.equals(Constants.ENCRYPTION))) {
            // verify that the requested protection method was used in response
            if (!before.equals(after)) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "gsiXmlError"
                    );
            }

            Subject peer =
                (Subject) context.getProperty(Constants.PEER_SUBJECT);

            Authorization author = AuthUtil.getClientAuthorization(context);
            
            if (author == null) {
                author = HostAuthorization.getInstance();
            }
            
            author.authorize(peer, context);
        }
    }

    // no need to perform authorization checks
    // as they were done in SecContextHandler for GSI Sec Conv.
    private void checkGSISecConv(Object before, Object after) 
        throws WSSecurityException {

        // check if gss sec was requested
        if (
            (before != null) &&
                (
                    before.equals(Constants.SIGNATURE) ||
                    before.equals(Constants.ENCRYPTION)
                )
        ) {
            if (!before.equals(after)) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "gsiSecConvError"
                );
            }
        }
    }
}
