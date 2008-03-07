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
package org.globus.wsrf.impl.security.authentication.securemsg;

import org.globus.wsrf.impl.security.authentication.Constants;

import org.globus.wsrf.impl.security.authentication.encryption.X509WSEncryptedSOAPEnvelopeBuilder;
import org.globus.wsrf.impl.security.authentication.encryption.EncryptionCredentials;

import org.globus.wsrf.impl.security.authentication.wssec.WSSecurityException;
import org.globus.wsrf.impl.security.authentication.wssec.WSSecurityFault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import java.security.cert.X509Certificate;

import javax.xml.namespace.QName;

import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import java.util.Set;
import java.util.Iterator;

import org.globus.util.I18n;

/**
 * Used for GSI Secure Message. Encrypts and adds relevant information
 * into the security header.
 */
public class X509EncryptHandler extends GenericHandler {

    protected static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.authentication.errors");
    private static Log log =
        LogFactory.getLog(X509EncryptHandler.class.getName());

    // client side
    public boolean handleRequest(MessageContext context) {

        if (!(encryptionRequested((SOAPMessageContext)context))) {
            return false;
        }

        X509Certificate serverCert = null;
        Subject peerSubject =
            (Subject) context.getProperty(Constants.PEER_SUBJECT);
        if (peerSubject == null) {
            throw WSSecurityFault.makeFault(
                                  new WSSecurityException(
                                  WSSecurityException.FAILURE, "noCreds"));
        }

        Set encryptCredSet =
            peerSubject.getPublicCredentials(EncryptionCredentials.class);
        if (encryptCredSet != null) {
            Iterator iterator = encryptCredSet.iterator();
            if (iterator.hasNext()) {
                EncryptionCredentials creds =
                    (EncryptionCredentials)iterator.next();
                serverCert = creds.getFirstCertificate();
            }
        }

        if (serverCert == null) {
            throw WSSecurityFault.makeFault(
                                  new WSSecurityException(
                                  WSSecurityException.FAILURE, "noCreds"));
        }

        return handleMessage((SOAPMessageContext) context,
                             serverCert);
    }

    // server side
    public boolean handleResponse(MessageContext context) {

        if (!(encryptionRequested((SOAPMessageContext)context))) {
            return false;
        }

        // The caller's subject is populated by the request handlers
        // with infomation about the caller's credentials. The public
        // key to encrypt response is picked up from that property.
        Subject subject =
            (Subject) context.getProperty(Constants.PEER_SUBJECT);

        X509Certificate clientCert = null;
        if (subject != null) {
            clientCert = getPublicCredential(subject);
            log.debug("Client " + clientCert.getSubjectDN().getName());
        } else {
            log.error(i18n.getMessage("noCreds"));
            throw WSSecurityFault.makeFault(
                                  new WSSecurityException(
                                  WSSecurityException.FAILURE, "noCreds"));
        }

        return handleMessage((SOAPMessageContext) context,
                             clientCert);
    }

    public boolean handleMessage(SOAPMessageContext ctx,
                                 X509Certificate clientCert) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: encrypt " 
                      + clientCert.getPublicKey().getEncoded().length);
        }

        SOAPMessage msg = ctx.getMessage();
        if (msg == null) {
            log.debug("No message - not encrypting.");
            return true;
        }

        SOAPMessage encryptedMsg = null;

        try {
            SOAPEnvelope unsignedEnvelope = msg.getSOAPPart().getEnvelope();

            X509WSEncryptedSOAPEnvelopeBuilder builder =
                new X509WSEncryptedSOAPEnvelopeBuilder(clientCert);

            builder.setActor((String) ctx.getProperty("x509Actor"));

            encryptedMsg = builder.buildMessage(unsignedEnvelope);
        } catch (Exception e) {
            log.error(i18n.getMessage("encryptErr"), e);
            throw WSSecurityFault.makeFault(e);
        }

        ctx.setMessage(encryptedMsg);

        log.debug("Exit: encrypt");

        return true;
    }

    public QName[] getHeaders() {
        return null;
    }

    private X509Certificate getPublicCredential(Subject subject) {

        if (subject == null) {
            return null;
        }

        Set gssCreds = subject.getPublicCredentials(X509Certificate[].class);
        if (gssCreds != null) {
            Iterator iter = gssCreds.iterator();
            if (iter.hasNext()) {
                X509Certificate cert[] = (X509Certificate[])iter.next();
                if ((cert != null) && (cert.length > 0)) {
                    return cert[0];
                }
            }
        }
        return null;
    }

    private boolean encryptionRequested(SOAPMessageContext ctx) {
        Object tmp = ctx.getProperty(Constants.GSI_SEC_MSG);

        if (!Constants.ENCRYPTION.equals(tmp)) {
            return false;
        } else {
            return true;
        }
    }
}
