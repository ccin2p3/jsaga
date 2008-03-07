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

import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.apache.ws.patched.security.WSConstants;
import org.apache.ws.patched.security.message.WSSAddUsernameToken;

import org.w3c.dom.Document;

import org.globus.wsrf.impl.security.util.EnvelopeConverter;
import org.globus.wsrf.security.Constants;

/**
 * Used to add in username/password and construct the relevant
 * security headers
 */
public class UsernameSOAPEnvelopeBuilder
    extends WSSAddUsernameToken {

    protected String userName;
    protected MessageContext msgContext;

    public UsernameSOAPEnvelopeBuilder(MessageContext msgContext,
                                       String _userName) {
        this.userName = _userName;
        this.msgContext = msgContext;
    }

    public SOAPEnvelope build(SOAPEnvelope envelope) throws Exception {
        return buildMessage(envelope).getSOAPPart().getEnvelope();
    }
    
    public SOAPMessage buildMessage(SOAPEnvelope env) throws Exception {

        Document doc = EnvelopeConverter.getInstance().toDocument(env);

        // setting must understand to false for now
        setMustUnderstand(false);

        // For now assuming password is set in the message
        // context. WSS4J aspears to have other ways to get this information.
        String password =
            (String)this.msgContext.getProperty(Constants.PASSWORD);
        // check if password type was set, if not set to text
        if (password != null) {
            String pwType =
                (String)this.msgContext.getProperty(Constants.PASSWORD_TYPE);
            if (pwType == null) {
                pwType = WSConstants.PASSWORD_TEXT;
            }
            setPasswordType(pwType);
        }

        // add the UsernameToken to the SOAP Enevelope
        build(doc, this.userName, password);

        // FIXME: add nonce ?
        //        builder.addNonce(doc);
        // FIXME: add timestamp ?
        addCreated(doc);

        return EnvelopeConverter.getInstance().toSOAPMessage(doc);
    }
}
