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

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;

import org.globus.util.I18n;
import org.globus.ws.sc.SecurityContextTokenType;
import org.globus.ws.trust.BinaryExchangeType;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.encoding.DeserializationException;
import org.globus.wsrf.security.impl.secconv.MalformedMessageFaultType;
import org.globus.wsrf.security.impl.secconv.TokenTypeNotSupportedFaultType;
import org.globus.wsrf.security.impl.secconv.RequestTypeNotSupportedFaultType;
import org.globus.wsrf.security.impl.secconv.EncodingTypeNotSupportedFaultType;
import org.globus.wsrf.security.impl.secconv.ValueTypeNotSupportedFaultType;
import org.globus.wsrf.utils.FaultHelper;

public class SecureConversationMessage
{
    public static final String SECCONV_NS =
        "http://schemas.xmlsoap.org/ws/2004/04/sc";
    public static final String TRUST_NS =
        "http://schemas.xmlsoap.org/ws/2004/04/trust";
    public static final String SECCONV_SERVICE_NS =
        "http://wsrf.globus.org/core/2004/07/security/secconv";
    public static final URI SECURITY_CONTEXT_TOKEN_TYPE;
    public static final QName TOKEN_TYPE_QNAME =
        new QName(TRUST_NS, "TokenType");
    public static final QName REQUEST_TYPE_QNAME =
        new QName(TRUST_NS, "RequestType");
    public static final QName BINARY_EXCHANGE_QNAME =
        new QName(TRUST_NS, "BinaryExchange");
    public static final QName CONTEXT_EPR_QNAME =
        new QName(SECCONV_SERVICE_NS, "ContextAddress");
    public static final QName SECURITY_CONTEXT_TOKEN_QNAME =
        new QName(SECCONV_NS, "SecurityContextToken");
    public static final URI CONTEXT_TOKEN_VALUE_TYPE;
    public static final URI REQUEST_TYPE_ISSUE;
    public static final URI GSSAPI_GSI_TOKEN_VALUE_TYPE;
    public static final URI BASE64_BINARY_ENCODING;

    private static MessageElement requestType;
    private static MessageElement tokenType;
    private static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.error",
                     SecureConversationMessage.class.getClassLoader());
    static
    {
        try
        {
            SECURITY_CONTEXT_TOKEN_TYPE =
                new URI("http://schemas.xmlsoap.org/ws/2004/04/security/sc/sct");
            REQUEST_TYPE_ISSUE =
                new URI("http://schemas.xmlsoap.org/ws/2004/04/security/trust/Issue");
            GSSAPI_GSI_TOKEN_VALUE_TYPE =
                new URI("http://www.globus.org/ws/2004/09/security/sc#GSSAPI_GSI_TOKEN");
            CONTEXT_TOKEN_VALUE_TYPE =
                new URI("http://www.globus.org/ws/2004/09/security/sc#GSSAPI_CONTEXT_TOKEN");
            BASE64_BINARY_ENCODING =
                new URI("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
            requestType = (MessageElement) ObjectSerializer.toSOAPElement(
                SecureConversationMessage.REQUEST_TYPE_ISSUE,
                SecureConversationMessage.REQUEST_TYPE_QNAME);
            tokenType = (MessageElement) ObjectSerializer.toSOAPElement(
                SecureConversationMessage.SECURITY_CONTEXT_TOKEN_TYPE,
                SecureConversationMessage.TOKEN_TYPE_QNAME);

        }
        catch(URI.MalformedURIException e)
        {
            throw new RuntimeException();
        }
        catch(SerializationException e)
        {
            throw new RuntimeException();
        }
    }

    private MessageElement[] anyContent;
    private BinaryExchangeType gssToken = null;
    private SecurityContextTokenType secContextToken = null;

    public SecureConversationMessage(MessageElement[] anyContent)
    {
        this.anyContent = anyContent;
    }

    public BinaryExchangeType getExchangeToken()
    {
        return gssToken;
    }

    public SecurityContextTokenType getSecurityContextToken()
    {
        return secContextToken;
    }

    public BinaryExchangeType parseMessage()
        throws MalformedMessageFaultType,
               ValueTypeNotSupportedFaultType,
               EncodingTypeNotSupportedFaultType,
               RequestTypeNotSupportedFaultType,
               TokenTypeNotSupportedFaultType
    {
        URI tokenType = null;
        URI requestType = null;

        for(int i = 0; i < anyContent.length; i++)
        {
            MessageElement requestElement = anyContent[i];
            if(requestElement.getQName().equals(TOKEN_TYPE_QNAME))
            {
                if(tokenType != null)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("invalidTokenTypeCardinality"));
                    throw fault;
                }
                try
                {
                    tokenType = (URI) ObjectDeserializer.toObject(
                        requestElement, URI.class);
                }
                catch(DeserializationException e)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("deserializationFailed",
                                        requestElement.getQName().toString()));
                    throw fault;
                }
                if(!tokenType.equals(SECURITY_CONTEXT_TOKEN_TYPE))
                {
                    TokenTypeNotSupportedFaultType fault =
                        new TokenTypeNotSupportedFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("invalidTokenType",
                                        tokenType.toString()));
                    throw fault;
                }
            }
            else if(requestElement.getQName().equals(REQUEST_TYPE_QNAME))
            {
                if(requestType != null)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("invalidRequestTypeCardinality"));
                    throw fault;
                }
                try
                {
                    requestType = (URI) ObjectDeserializer.toObject(
                        requestElement, URI.class);
                }
                catch(DeserializationException e)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("deserializationFailed",
                                        requestElement.getQName().toString()));
                    throw fault;
                }
                if(!requestType.equals(REQUEST_TYPE_ISSUE))
                {
                    RequestTypeNotSupportedFaultType fault =
                        new RequestTypeNotSupportedFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("invalidRequestType",
                                        requestType.toString()));
                    throw fault;
                }
            }
            else if(requestElement.getQName().equals(BINARY_EXCHANGE_QNAME))
            {
                if(gssToken != null)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("invalidBinaryExchangeCardinality"));
                    throw fault;
                }
                try
                {
                    this.gssToken = (BinaryExchangeType)
                        ObjectDeserializer.toObject(requestElement,
                                                    BinaryExchangeType.class);
                }
                catch(DeserializationException e)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("deserializationFailed",
                                        requestElement.getQName().toString()));
                    throw fault;
                }
                if(!this.gssToken.getEncodingType().equals(
                    BASE64_BINARY_ENCODING))
                {
                    EncodingTypeNotSupportedFaultType fault =
                        new EncodingTypeNotSupportedFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage(
                            "invalidEncodingType",
                            this.gssToken.getEncodingType().toString()));
                    throw fault;
                }

                if(!this.gssToken.getValueType().equals(
                    GSSAPI_GSI_TOKEN_VALUE_TYPE))
                {
                    ValueTypeNotSupportedFaultType fault =
                        new ValueTypeNotSupportedFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage(
                            "invalidValueType",
                            this.gssToken.getValueType().toString()));
                    throw fault;
                }
            }
            else if(requestElement.getQName().equals(
                SECURITY_CONTEXT_TOKEN_QNAME))
            {
                if(secContextToken != null)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage(
                            "invalidSecurityContextTokenCardinality"));
                    throw fault;
                }
                try
                {
                    secContextToken = (SecurityContextTokenType)
                        ObjectDeserializer.toObject(
                            requestElement,
                            SecurityContextTokenType.class);
                }
                catch(DeserializationException e)
                {
                    MalformedMessageFaultType fault =
                        new MalformedMessageFaultType();
                    FaultHelper faultHelper = new FaultHelper(fault);
                    faultHelper.setDescription(
                        i18n.getMessage("deserializationFailed",
                                        requestElement.getQName().toString()));
                    throw fault;
                }
            }
            else
            {
                MalformedMessageFaultType fault =
                    new MalformedMessageFaultType();
                FaultHelper faultHelper = new FaultHelper(fault);
                faultHelper.setDescription(
                    i18n.getMessage("unexpectedElement",
                                    requestElement.getQName().toString()));
                throw fault;
            }
        }
        return gssToken;
    }

    public static MessageElement[] createMessage(
        BinaryExchangeType exchangeToken) throws SerializationException
    {
        return new MessageElement[] {
            requestType, tokenType,
            (MessageElement) ObjectSerializer.toSOAPElement(
                exchangeToken,
                SecureConversationMessage.BINARY_EXCHANGE_QNAME) };
    }

    public static MessageElement[] createMessage(
        BinaryExchangeType exchangeToken, SecurityContextTokenType contextToken)
        throws SerializationException
    {
        return new MessageElement[] {
            requestType, tokenType,
            (MessageElement) ObjectSerializer.toSOAPElement(
                exchangeToken,
                SecureConversationMessage.BINARY_EXCHANGE_QNAME),
            (MessageElement) ObjectSerializer.toSOAPElement(
                contextToken,
                SecureConversationMessage.SECURITY_CONTEXT_TOKEN_QNAME) };
    }

    public static MessageElement[] createMessage(
        SecurityContextTokenType contextToken) throws SerializationException
    {
        return new MessageElement[] {
            requestType, tokenType,
            (MessageElement) ObjectSerializer.toSOAPElement(
                contextToken,
                SecureConversationMessage.SECURITY_CONTEXT_TOKEN_QNAME) };
    }
}
