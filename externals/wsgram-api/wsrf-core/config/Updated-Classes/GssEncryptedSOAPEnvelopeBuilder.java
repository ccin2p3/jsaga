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
package org.globus.wsrf.impl.security.authentication.encryption;

import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.apache.ws.patched.security.WSConstants;
import org.apache.ws.patched.security.WSSConfig;
import org.apache.ws.patched.security.conversation.message.token.SecurityContextToken;
import org.apache.ws.patched.security.message.WSEncryptBody;
import org.apache.ws.patched.security.message.token.Reference;
import org.apache.ws.patched.security.message.token.SecurityTokenReference;
import org.apache.ws.patched.security.util.WSSecurityUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.globus.wsrf.impl.security.authentication.ContextCrypto;
import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;
import org.globus.wsrf.impl.security.authentication.secureconv.SecureConversationMessage;
import org.globus.wsrf.impl.security.authentication.wssec.GSSConfig;
import org.globus.wsrf.impl.security.util.EnvelopeConverter;
import org.globus.wsrf.providers.GSSKey;

/**
 * Used for GSI secure conversation encryption. Encrypts the body of
 * the message and inserts relevant information into SOAP header.
 */
public class GssEncryptedSOAPEnvelopeBuilder
    extends WSEncryptBody {

    public static final String ALGORITHM =
        "http://www.globus.org/2002/04/xmlenc#gssapi-enc";

    static {
        GSSConfig.init();
    }

    private static Log logger =
        LogFactory.getLog(GssEncryptedSOAPEnvelopeBuilder.class.getName());

    protected GSSContext context;
    protected String contextId;
    protected MessageContext msgContext;
    protected static ContextCrypto crypto = ContextCrypto.getInstance();

    public GssEncryptedSOAPEnvelopeBuilder(MessageContext msgContext,
                                           SecurityContext context) {
        this(msgContext, context.getContext(), (String)context.getID());
    }

    protected GssEncryptedSOAPEnvelopeBuilder(MessageContext msgContext,
                                           GSSContext context,
                                           String contextId) {
        super();
        this.context = context;
        this.contextId = contextId;
        this.msgContext = msgContext;
        setSymmetricEncAlgorithm(ALGORITHM);
        setSymmetricKey(new GSSKey(contextId, context));
    }

    private String getContextId() {
        if (this.contextId == null) {
            return String.valueOf(this.context.hashCode());
        } else {
            return this.contextId;
        }
    }

    public SOAPEnvelope build(SOAPEnvelope envelope) throws Exception {
        return buildMessage(envelope).getSOAPPart().getEnvelope();
    }

    public SOAPMessage buildMessage(SOAPEnvelope env) throws Exception {
        logger.debug("Beginning encryption...");
        Document doc = EnvelopeConverter.getInstance().toDocument(env);
        WSSConfig wssConfig = WSSConfig.getDefaultWSConfig();
        SecurityTokenReference securityTokenReference =
            new SecurityTokenReference(wssConfig, doc);
        Reference tokenReference = new Reference(wssConfig, doc);
        tokenReference.setValueType(
            SecureConversationMessage.CONTEXT_TOKEN_VALUE_TYPE.toString());
        tokenReference.setURI("#SecurityContextToken-" + env.hashCode());
        securityTokenReference.setReference(tokenReference);
        setSecurityTokenReference(securityTokenReference);
        setKey(new byte[1]);
        setKeyIdentifierType(WSConstants.EMBED_SECURITY_TOKEN_REF);
        doc = build(doc, crypto);
        SecurityContextToken secContextToken = new SecurityContextToken(
            doc, getContextId());
        secContextToken.setID("SecurityContextToken-" + env.hashCode());
        Element secHeader = insertSecurityHeader(doc);
        WSSecurityUtil.prependChildElement(doc, secHeader,
                                           secContextToken.getElement(), false);
        SOAPMessage message = EnvelopeConverter.getInstance().toSOAPMessage(doc);
        logger.debug("Encryption complete");
        return message;
    }
}
