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

import java.security.cert.X509Certificate;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.apache.ws.patched.security.message.WSEncryptBody;
import org.apache.xml.security.utils.EncryptionConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;

import org.globus.wsrf.impl.security.authentication.ContextCrypto;
import org.globus.wsrf.impl.security.authentication.wssec.GSSConfig;
import org.globus.wsrf.impl.security.util.EnvelopeConverter;

/**
 * Used for GSI secure message encryption. Encrypts the body of
 * the message and inserts relevant information into SOAP header.
 */
public class X509WSEncryptedSOAPEnvelopeBuilder
    extends WSEncryptBody {

    private static Log logger =
        LogFactory.getLog(X509WSEncryptedSOAPEnvelopeBuilder.class.getName());
    protected static ContextCrypto crypto = ContextCrypto.getInstance();

    static {
        GSSConfig.init();
    }
    
    public X509WSEncryptedSOAPEnvelopeBuilder(X509Certificate cert) {
        setUseThisCert(cert);
        setSymmetricEncAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
    }

    public SOAPEnvelope build(SOAPEnvelope envelope) throws Exception {
        return buildMessage(envelope).getSOAPPart().getEnvelope();
    }

    public SOAPMessage buildMessage(SOAPEnvelope env) throws Exception {
        logger.debug("Begin encryption...");
        Document doc = EnvelopeConverter.getInstance().toDocument(env);
        SOAPMessage message = EnvelopeConverter.getInstance().toSOAPMessage(
            build(doc, crypto));
        logger.debug("Encryption complete");
        return message;
    }
}
