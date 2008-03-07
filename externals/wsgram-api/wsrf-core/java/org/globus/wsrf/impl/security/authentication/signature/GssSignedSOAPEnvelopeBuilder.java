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
package org.globus.wsrf.impl.security.authentication.signature;

import java.security.Key;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.ws.patched.security.SOAPConstants;
import org.apache.ws.patched.security.WSSConfig;
import org.apache.ws.patched.security.conversation.message.token.SecurityContextToken;
import org.apache.ws.patched.security.message.EnvelopeIdResolver;
import org.apache.ws.patched.security.message.WSSignEnvelope;
import org.apache.ws.patched.security.message.token.Reference;
import org.apache.ws.patched.security.message.token.SecurityTokenReference;
import org.apache.ws.patched.security.util.WSSecurityUtil;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.InclusiveNamespaces;
import org.apache.xml.security.utils.XMLUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.secureconv.SecureConversationMessage;
import org.globus.wsrf.impl.security.authentication.wssec.GSSConfig;
import org.globus.wsrf.impl.security.util.EnvelopeConverter;
import org.globus.wsrf.providers.GSSPrivateKey;

public class GssSignedSOAPEnvelopeBuilder
    extends WSSignEnvelope {

    private static Log logger =
        LogFactory.getLog(GssSignedSOAPEnvelopeBuilder.class.getName());

    protected GSSContext context;
    protected MessageContext msgContext;
    protected String contextId;
    protected String canonicalizationAlgorithm =
        Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;

    static {
        GSSConfig.init();
    }

    public GssSignedSOAPEnvelopeBuilder(MessageContext msgContext,
                                        GSSContext context) {
        this(msgContext, context, null);
    }

    public GssSignedSOAPEnvelopeBuilder(MessageContext msgContext,
                                        GSSContext context, String contextId) {
        this.msgContext = msgContext;
        this.context = context;
        this.contextId = contextId;
    }

    private String getContextId() {
        if (this.contextId == null) {
            return String.valueOf(this.context.hashCode());
        } else {
            return this.contextId;
        }
    }


    public String getCanonicalizationAlgorithm() {
        return canonicalizationAlgorithm;
    }

    public void setCanonicalizationAlgorithm(String canonicalizationAlgorithm) {
        this.canonicalizationAlgorithm = canonicalizationAlgorithm;
    }

    public SOAPEnvelope build(SOAPEnvelope envelope) throws Exception {
        return buildMessage(envelope).getSOAPPart().getEnvelope();
    }

    public SOAPMessage buildMessage(SOAPEnvelope env) throws Exception {
        logger.debug("Beginning signing...");
        WSSConfig wssConfig = WSSConfig.getDefaultWSConfig();
        Document doc = EnvelopeConverter.getInstance().toDocument(env);
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(
            doc.getDocumentElement());

        Element secHeader = insertSecurityHeader(doc);

        Element canonElem = XMLUtils.createElementInSignatureSpace(
            doc,
            org.apache.xml.security.utils.Constants._TAG_CANONICALIZATIONMETHOD);

        canonElem.setAttributeNS(
            null,
            org.apache.xml.security.utils.Constants._ATT_ALGORITHM,
            canonicalizationAlgorithm);

        Set prefixes = getInclusivePrefixes(secHeader, false);

        InclusiveNamespaces inclusiveNamespaces = new InclusiveNamespaces(
            doc, prefixes);

        canonElem.appendChild(inclusiveNamespaces.getElement());

        SignatureAlgorithm signatureAlgorithm =
            new SignatureAlgorithm(doc, SignatureGSS.URI);

        XMLSignature sig = new XMLSignature(doc, null,
                                            signatureAlgorithm.getElement(),
                                            canonElem);

        // must set the id of the elements that will be used
        // as digest source
        Element body =
            (Element) WSSecurityUtil.findBodyElement(doc, soapConstants);
        String id = setWsuId(body);
        Transforms transforms = new Transforms(doc);
        transforms.addTransform(
            Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        transforms.item(0).getElement().appendChild(
            new InclusiveNamespaces(
                doc,
                getInclusivePrefixes(body)).getElement());
        sig.addResourceResolver(EnvelopeIdResolver.getInstance(wssConfig));
        sig.addDocument("#" + id, transforms);

        // get non-wsa headers
        HashMap nonWsaHeaders =
            (HashMap)this.msgContext.getProperty(Constants.SECURE_HEADERS);
        if (nonWsaHeaders != null) {
            // Get all in SOAP headers
            SOAPHeader header = env.getHeader();
            if (header != null) {
                Iterator iter = header.examineHeaderElements(this.actor);
                while (iter.hasNext()) {
                    SOAPHeaderElement elem = (SOAPHeaderElement)iter.next();
                    String ns = elem.getNamespaceURI();
                    String name = elem.getElementName().getLocalName();
                    QName qName = new QName(ns, name);
                    if (nonWsaHeaders.containsKey(qName)) {
                        logger.debug("Sign header " + qName);
                        NodeList list = doc.getElementsByTagNameNS(ns, name);
                        Element headerElem = (Element)list.item(0);
                        String headerId = setWsuId(headerElem);
                        transforms = new Transforms(doc);
                        transforms.addTransform(
                            Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
                        transforms.item(0).getElement().appendChild(
                            new InclusiveNamespaces(
                                doc,
                                getInclusivePrefixes(headerElem)).getElement());
                        sig.addDocument("#" + headerId, transforms);
                    }
                }
            }
        }

        Key contextKey = new GSSPrivateKey(getContextId(), this.context);
        sig.sign(contextKey);
        KeyInfo keyInfo = sig.getKeyInfo();
        SecurityTokenReference securityTokenReference =
            new SecurityTokenReference(wssConfig, doc);
        Reference tokenReference = new Reference(wssConfig, doc);
        tokenReference.setValueType(
            SecureConversationMessage.CONTEXT_TOKEN_VALUE_TYPE.toString());
        tokenReference.setURI("#SecurityContextToken-" + env.hashCode());
        securityTokenReference.setReference(tokenReference);
        keyInfo.addUnknownElement(securityTokenReference.getElement());
        SecurityContextToken secContextToken = new SecurityContextToken(
            doc, getContextId());
        secContextToken.setID("SecurityContextToken-" + env.hashCode());
        WSSecurityUtil.prependChildElement(doc, secHeader,
                                           secContextToken.getElement(), false);
        WSSecurityUtil.prependChildElement(doc, secHeader,
                                           sig.getElement(), false);
        logger.debug("Signing complete.");

        return EnvelopeConverter.getInstance().toSOAPMessage(doc);
    }
}
