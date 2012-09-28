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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.apache.ws.patched.security.SOAPConstants;
import org.apache.ws.patched.security.WSConstants;
import org.apache.ws.patched.security.WSEncryptionPart;
import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.message.WSAddTimestamp;
import org.apache.ws.patched.security.message.WSSignEnvelope;
import org.apache.ws.patched.security.util.WSSecurityUtil;
import org.apache.xml.security.signature.XMLSignature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisFault;

import org.ietf.jgss.GSSCredential;
import org.w3c.dom.Document;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.I18n;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.ContextCredential;
import org.globus.wsrf.impl.security.authentication.ContextCrypto;
import org.globus.wsrf.impl.security.authentication.wssec.GSSConfig;
import org.globus.wsrf.impl.security.util.EnvelopeConverter;

public class X509WSSignedSOAPEnvelopeBuilder
    extends WSSignEnvelope {

    // Timestamp: time in seconds the receiver accepts between creation
    //and reception
    protected int defaultTTL = 300;

    protected static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.authentication.errors");
    private static ContextCrypto crypto = ContextCrypto.getInstance();
    private static Log logger =
        LogFactory.getLog(X509WSSignedSOAPEnvelopeBuilder.class.getName());

    protected GSSCredential credential;
    protected MessageContext msgContext;

    static {
        GSSConfig.init();
    }

    public X509WSSignedSOAPEnvelopeBuilder(MessageContext msgContext,
                                           GSSCredential credential) {
        this.credential = credential;
        this.msgContext = msgContext;
        // pass cert chain
        setUseSingleCertificate(false);
        // pass certs as binary tokens
        setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA);
    }

    private GSSCredential getCredential() throws Exception {

        if (this.credential == null) {
            X509Credential defaultCredential =
                X509Credential.getDefaultCredential();
            if (defaultCredential != null) {
                return new GlobusGSSCredentialImpl(defaultCredential,
                                                   GSSCredential
                                                   .INITIATE_AND_ACCEPT);
            }
        } else if (this.credential instanceof GlobusGSSCredentialImpl) {
            return this.credential;
        }
        return null;
    }

    public SOAPEnvelope build(SOAPEnvelope envelope) throws Exception {
        return buildMessage(envelope).getSOAPPart().getEnvelope();
    }

    public SOAPMessage buildMessage(SOAPEnvelope env) throws Exception {
        GSSCredential contextCredential = getCredential();
        if (contextCredential == null) {
            throw new Exception(i18n.getMessage("noCreds"));
        }
        logger.debug("Beginning signing...");

        Document doc = EnvelopeConverter.getInstance().toDocument(env);

        // build timestamp
        WSAddTimestamp timeStampBuilder =
                        new WSAddTimestamp(this.actor, true);
        int TTL = defaultTTL;
        if (msgContext != null) {
            String time = (String) msgContext.getProperty(Constants.TTL);
            if (time != null) {
                TTL = Integer.parseInt(time);
            }
        }
        timeStampBuilder.build(doc, TTL);

        // sign body and timestamp
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(env);
        Vector parts = new Vector();
        parts.add(new WSEncryptionPart(WSConstants.TIMESTAMP_TOKEN_LN,
                                       WSConstants.WSU_NS, "Content"));
        parts.add(new WSEncryptionPart(soapConstants.getBodyQName()
                                       .getLocalPart(),
                                       soapConstants.getEnvelopeURI(),
                                       "Content"));

        // get non-wsa headers
        HashMap nonWsaHeaders =
            (HashMap)this.msgContext.getProperty(Constants.SECURE_HEADERS);
        if (nonWsaHeaders != null) {
            Iterator iterator = nonWsaHeaders.keySet().iterator();
            while (iterator.hasNext()) {
                QName qName = (QName)iterator.next();
                logger.debug("Header added " + qName);
                parts.add(new WSEncryptionPart(qName.getLocalPart(),
                                               qName.getNamespaceURI(),
                                               "Content"));
            }
        }

        setParts(parts);
        try {
            ContextCredential.begin(contextCredential);
            build(doc, crypto);
        } catch (WSSecurityException e) {
            throw new AxisFault(i18n.getMessage("signErr"), e);
        } finally {
            ContextCredential.release();
        }
        logger.debug("Signing complete.");
        return EnvelopeConverter.getInstance().toSOAPMessage(doc);
    }
}
