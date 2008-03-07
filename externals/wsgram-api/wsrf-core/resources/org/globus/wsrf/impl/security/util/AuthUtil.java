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
package org.globus.wsrf.impl.security.util;

import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.StringTokenizer;

import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.AddressingHeaders;

import org.ietf.jgss.GSSCredential;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.globus.axis.gsi.GSIConstants;
import org.globus.gsi.jaas.GlobusPrincipal;
import org.globus.util.I18n;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.NoResourceHomeException;
import org.globus.wsrf.config.ConfigException;

import org.globus.wsrf.impl.security.authentication.Constants;

import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.GridMapAuthorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.IdentityAuthorization;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.globus.wsrf.impl.security.authorization.SAMLAuthorizationCallout;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.authorization.UsernameAuthorization;

import org.globus.wsrf.impl.security.descriptor.ContainerSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.SecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityConfig;

import org.globus.wsrf.security.SecurityException;
import org.globus.wsrf.utils.ContextUtils;

/**
 * Utility class, mostly methods that deal with message context
 */
public class AuthUtil {

    private static Log logger =
        LogFactory.getLog(AuthUtil.class.getName());

    private static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.error",
                     AuthUtil.class.getClassLoader());

    public static final String EPR_DELIMITER = "?";

    public static GSSCredential getCredential(MessageContext ctx)
        throws SecurityException {
        Object tmp = ctx.getProperty(GSIConstants.GSI_CREDENTIALS);
        if (tmp == null || tmp instanceof GSSCredential) {
            return (GSSCredential)tmp;
        } else {
            throw new SecurityException(
                i18n.getMessage("invalidType",
                                new Object[] {GSIConstants.GSI_CREDENTIALS,
                                              GSSCredential.class.getName()}));
        }
    }

    public static Authorization getClientAuthorization(MessageContext ctx)
        throws SecurityException {
        Object tmp = ctx.getProperty(Constants.AUTHORIZATION);
        if (tmp == null || tmp instanceof Authorization) {
            return (Authorization)tmp;
        } else {
            String err = i18n.getMessage("invalidType",
                                         new Object[] {
                                             Constants.AUTHORIZATION,
                                             Authorization.class.getName()});
            throw new SecurityException(err);
        }
    }

    public static Authorization getClientAuthorization(String authzString) {

        Authorization authz = null;
        if (authzString == null)
            return null;

        if (authzString.equals(Authorization.AUTHZ_NONE)) {
            authz = NoAuthorization.getInstance();
        } else if (authzString.equals(Authorization.AUTHZ_SELF)) {
            authz = new SelfAuthorization();
        } else if (authzString.equals(Authorization.AUTHZ_HOST)) {
            authz = HostAuthorization.getInstance();
        } else {
            authz = new IdentityAuthorization(authzString);
        }
        return authz;
    }

    public static String getPDPName(String authz) {
        if (authz == null)
            return null;
        if (authz.equals(Authorization.AUTHZ_NONE)) {
            return Authorization.NONE_PREFIX + ":"
                + NoAuthorization.class.getName();
        } else if (authz.equals(Authorization.AUTHZ_SELF)) {
            return Authorization.SELF_PREFIX + ":"
                + SelfAuthorization.class.getName();
        } else if (authz.equals(Authorization.AUTHZ_GRIDMAP)) {
           return Authorization.GRIDMAP_PREFIX + ":"
               + GridMapAuthorization.class.getName();
        } else if (authz.equals(Authorization.AUTHZ_IDENTITY)) {
            return Authorization.IDENTITY_PREFIX + ":"
                + IdentityAuthorization.class.getName();
        } else if (authz.equals(Authorization.AUTHZ_HOST)) {
            return Authorization.HOST_PREFIX + ":"
                + HostAuthorization.class.getName();
        } else if (authz.equals(Authorization.AUTHZ_SAML)) {
            return Authorization.SAML_PREFIX + ":"
                + SAMLAuthorizationCallout.class.getName();
        } else if (authz.equals(Authorization.AUTHZ_USERNAME)) {
            return Authorization.USERNAME_PREFIX + ":"
                + UsernameAuthorization.class.getName();
        } else {
            return authz;
        }
    }

    public static String substitutePDPNames(String authzString) {
        if (authzString == null) {
            return null;
        }

        StringTokenizer strTok = new StringTokenizer(authzString);
        if (strTok.hasMoreTokens()) {
            String tok = strTok.nextToken();
            StringBuffer interceptorStringBuf =
                new StringBuffer(getPDPName(tok));

            while (strTok.hasMoreTokens()) {
                tok = strTok.nextToken();
                interceptorStringBuf.append(" ").append(getPDPName(tok));
            }

            return interceptorStringBuf.toString();
        } else {
            return null;
        }
    }

    public static String getIdentity(Subject subject) {
        if (subject == null) {
            return null;
        }

        Set principals = subject.getPrincipals(GlobusPrincipal.class);

        if ((principals == null) || principals.isEmpty()) {
            return null;
        }

        Iterator iter = principals.iterator();
        GlobusPrincipal principal = (GlobusPrincipal) iter.next();

        return principal.toString();
    }

    public static URL getEndpointAddressURL(MessageContext ctx)
        throws MalformedURLException {
        return new URL(getEndpointAddress(ctx));
    }

    public static String getEndpointAddress(MessageContext ctx) {
        // get tranport url from axis-specific property first
        String endpoint = (String) ctx.getProperty("transport.url");

        if (endpoint == null) {
            endpoint =
                (String) ctx.getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
        }

        return endpoint;
    }

    public static QName getOperationName(
                        org.apache.axis.MessageContext messageContext)
        throws AxisFault, SecurityException {

        Message msg = messageContext.getCurrentMessage();
        SOAPEnvelope env = msg.getSOAPEnvelope();
        SOAPBodyElement body = env.getFirstBody();
        QName paramQName = body.getQName();
        logger.debug("Trying to find " + paramQName);

        SOAPService service = messageContext.getService();
        if (service == null) {
            throw new AxisFault(org.apache.axis.Constants
                                .QNAME_NO_SERVICE_FAULT_CODE,
                                i18n.getMessage("noService",
                                                messageContext
                                                .getTargetService()),
                                null, null );
        }
        ServiceDesc serviceDesc = service.getServiceDescription();
        ArrayList operations = serviceDesc.getOperations();
        for (int i=0; i<operations.size(); i++) {
            OperationDesc operation = (OperationDesc)operations.get(i);
            logger.debug("Operation in question "
                         + operation.getElementQName());
            ParameterDesc desc =  operation.getParamByQName(paramQName);
            if (desc != null) {
                return operation.getElementQName();
            }
        }
        throw new SecurityException(i18n
                                    .getMessage("operationNameIndeterminate"));
    }

    /**
     * Returns the security descriptor file for the said
     * service. Current message context is used. Null is returned
     * if not configured or if current message context is null.
     */
    public static String getSecurityDescFile(String servicePath)
        throws SecurityException {
        org.apache.axis.MessageContext ctx =
            org.apache.axis.MessageContext.getCurrentContext();
        return (ctx != null) ? getSecurityDescFile(ctx, servicePath) : null;
    }

    /**
     * Returns the security descriptor file for the said service, null
     * if not configured.
     */
    public static String getSecurityDescFile(
                         org.apache.axis.MessageContext msgCtx,
                         String servicePath)
        throws SecurityException {
        try {
            return (String)ContextUtils.getServiceProperty(
                                        msgCtx,
                                        servicePath,
                                        SecurityConfig.SECURITY_DESCRIPTOR);
        } catch (AxisFault af) {
            throw new SecurityException(af);
        }
    }

    /**
     * Initalizes container security descriptor and security decriptor
     * for said service, if configured.
     */
    public static void initializeSecurityDesc(
                       org.apache.axis.MessageContext msgCtx,
                       String servicePath)
        throws SecurityException {

        try {
            ContainerSecurityConfig config =
                ContainerSecurityConfig.getConfig();
            String descFile = getSecurityDescFile(msgCtx, servicePath);
            logger.debug("Descriptor file for " + servicePath  + " is "
                         + descFile);
            ServiceSecurityConfig.initialize(servicePath, descFile);
        } catch (ConfigException exp) {
            throw new SecurityException(exp);
        }
    }

    /**
     * Returns the addressing headers from the context
     */
    public static AddressingHeaders
        getAddressingHeaders(org.apache.axis.MessageContext ctx) {
        return
            (AddressingHeaders)ctx.getProperty(org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
    }

    /**
     * Returns a string representation of EPR. Service endpoint (to
     * header) + / + hash(resource key header)
     */
    public static String getEPRAsString(org.apache.axis.MessageContext ctx)
        throws SecurityException {

        AddressingHeaders addressingHeaders = getAddressingHeaders(ctx);
        return getEPRAsString(addressingHeaders, ctx);
    }

    public static String getEPRAsString(AddressingHeaders addressingHeaders,
                                        org.apache.axis.MessageContext ctx)
        throws SecurityException {

        if (addressingHeaders == null) {
            throw new IllegalArgumentException(i18n.getMessage("noAddHeader"));
        }
        String resource = addressingHeaders.getTo().toString();
        if (addressingHeaders.getReferenceProperties().size()  == 0) {
            return resource;
        } else {
            return getEPRAsString(resource, ctx);
        }
    }

    public static String getEPRAsString(String serviceEndpoint,
                                        org.apache.axis.MessageContext ctx)
        throws SecurityException {
        SOAPHeaderElement resHeader = null;
        try {
            resHeader = getResourceHeader(ctx);
        } catch(NoResourceHomeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found no resource home for service " +
                             serviceEndpoint, e);
            }
            return serviceEndpoint;
        } catch (ResourceContextException exp) {
            throw new SecurityException(i18n.getMessage("resourceHeaderError"),
                                        exp);
        }
        return getEPRAsString(resHeader, serviceEndpoint);
    }

    public static String getEPRAsString(SOAPHeaderElement resHeader,
                                        String resource)
        throws SecurityException {

        String resHeaderDigest = getResourceHeaderDigest(resHeader);
        if (resHeaderDigest != null) {
            resHeaderDigest = URLEncoder.encode(resHeaderDigest);
            resource = resource + EPR_DELIMITER + resHeaderDigest;
        }
        logger.debug("EPR is : " + resource);
        return resource;
    }

    /**
     * Returns the digest value of the resource key header, if one is present
     */
    public static String
        getResourceHeaderDigest(org.apache.axis.MessageContext ctx)
        throws SecurityException {

        if (ctx == null)
            throw new IllegalArgumentException(i18n.getMessage("noContext"));
        SOAPHeaderElement resHeader = null;
        try {
            resHeader = getResourceHeader(ctx);
        } catch (ResourceContextException exp) {
            throw new SecurityException(i18n.getMessage("resourceHeaderError"),
                                        exp);
        }
        return getResourceHeaderDigest(resHeader);
    }

    public static String getResourceHeaderDigest(SOAPHeaderElement resHeader)
        throws SecurityException {

        if (resHeader == null)
            return null;
        Document doc = null;
        Node importedNode = null;
        try {
            doc = org.globus.wsrf.utils.XmlUtils.newDocument();
            importedNode =
                (Element)doc.importNode(((MessageElement)resHeader).getAsDOM(),
                                        true);
        } catch (Exception exp) {
            throw new SecurityException(i18n.getMessage("resHeaderElement"),
                                        exp);
        }
        byte[] c14nBytes = null;
        try {
            Canonicalizer c14n = Canonicalizer
                .getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            c14nBytes = c14n.canonicalizeSubtree(importedNode);
        } catch (CanonicalizationException exp) {
            throw new SecurityException(i18n.getMessage("c14nError"), exp);
        } catch (InvalidCanonicalizerException exp) {
            throw new SecurityException(i18n.getMessage("c14nError"), exp);
        }

        MessageDigestAlgorithm mda = null;
        try {
            mda = MessageDigestAlgorithm
                .getInstance(doc,
                             MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1);
        } catch (XMLSignatureException exp) {
            throw new SecurityException(i18n.getMessage("mdaError"), exp);
        }
        mda.reset();
        mda.update(c14nBytes);
        byte[] digest = mda.digest();
        return Base64.encode(digest);
    }

    /**
     * Returns the header with the resource key, if present
     */
    public static SOAPHeaderElement getResourceHeader(
        org.apache.axis.MessageContext ctx)
        throws ResourceContextException {

        ResourceContext context = ResourceContext.getResourceContext(ctx);
        return context.getResourceKeyHeader();
    }

    // Note about closing the stream
    public static void writeSubject(Subject subject, ObjectOutputStream oos)
        throws SecurityException {

        try {
            // serialize subject object (does not serialize credentials)
            oos.writeObject(subject);
            if (subject == null) {
                return;
            }
            Set publicCreds = subject.getPublicCredentials();
            if ((publicCreds != null) && (!publicCreds.isEmpty())) {
                Vector vector = new Vector(publicCreds);
                // Will definitely work if instance of
                // X509Certificate[], X509Certificate, EncryptionCredentials
                oos.writeObject(vector);
            }

            Set privateCreds = subject.getPrivateCredentials();
            if ((privateCreds != null) && (!privateCreds.isEmpty())) {
                Vector vector = new Vector(privateCreds);
                // Will definitely work if instance of
                // PasswordCredential and GlobusGSSCredentialImpl
                oos.writeObject(vector);
            }
        } catch (IOException exp) {
            throw new SecurityException(exp);
        }
    }

    public static Subject readSubject(FixedObjectInputStream ois)
        throws SecurityException, ClassNotFoundException {

        Subject subject = null;
        try {
            subject = (Subject)ois.readObject();
            Vector publicCreds = (Vector)ois.readObject();
            subject.getPublicCredentials().addAll(publicCreds);

            Vector privateCreds = (Vector)ois.readObject();
            subject.getPrivateCredentials().addAll(privateCreds);
        } catch (IOException exp) {
            throw new SecurityException(exp);
        }
        return subject;
    }
}
