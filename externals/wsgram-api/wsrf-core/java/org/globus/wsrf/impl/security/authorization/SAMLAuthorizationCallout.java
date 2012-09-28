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
package org.globus.wsrf.impl.security.authorization;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import javax.xml.rpc.handler.MessageContext;

import org.apache.xml.security.signature.XMLSignature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.types.URI;
import org.apache.axis.utils.XMLUtils;

import org.opensaml.ExtendedAuthorizationDecisionQuery;
import org.opensaml.QName;
import org.opensaml.SAMLAction;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthorizationDecisionStatement;
import org.opensaml.SAMLDecision;
import org.opensaml.SAMLException;
import org.opensaml.SAMLRequest;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLSubject;
import org.opensaml.SimpleAuthorizationDecisionStatement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.globus.axis.gsi.GSIConstants;
import org.globus.gsi.CertUtil;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.JaasGssUtil;
import org.globus.util.I18n;
import org.globus.wsrf.impl.security.SecurityMessageElement;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.encryption.EncryptionCredentials;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;
import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.security.SecurityException;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.security.authorization.AuthorizationServiceAddressingLocator;
import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;
import org.globus.wsrf.security.authorization.SAMLRequestPortType;
import org.globus.wsrf.utils.ContextUtils;
import org.ietf.jgss.GSSException;
import org.opensaml.SAMLAuthorizationDecisionQuery;
import protocol._0._1.SAML.tc.names.oasis.Request;
import protocol._0._1.SAML.tc.names.oasis.Response;

/**
 * Calls out to a configured authorization service. The authorization
 * service is configured using a property <i>authzService</i> in the
 * service deployment descriptor.
 */
public class SAMLAuthorizationCallout implements PDP {

    private static I18n i18n =
        I18n.getI18n(
            Authorization.RESOURCE,
            SAMLAuthorizationCallout.class.getClassLoader()
        );
    private static Log logger =
        LogFactory.getLog(SAMLAuthorizationCallout.class.getName());

    private SAMLRequestPortType authzPort = null;
    // This determines if SimpleAuthzDecisionStmt or
    // AuthzDecisionStmt is requested. By default, simpleDecision
    // is true.
    private boolean simpleDecision = true;
    // Determines if request to authz service should be signed.
    // False by default.
    private boolean sigReq = false;

    // Certificate used to verify response signature and for
    // encryption when using GSI Secure Message
    private X509Certificate authzServiceCert = null;

    public void initialize(PDPConfig config, String name, String id)
            throws InitializeException {

        String authzService = null;
        String mechanism;
        Integer protection = Constants.SIGNATURE;
        String authzServiceCertFile = null;

        authzService = (String) config.getProperty(
            name, SAMLAuthorizationConstants.AUTHZ_SERVICE_PROPERTY);
        logger.debug("Authz service " + authzService);
        if ((authzService == null) || (authzService.trim().equals(""))) {
            String err = i18n.getMessage("authzServiceConfig");
            logger.debug(err);
            throw new InitializeException(err);
        }

        authzService = authzService.trim();

        AuthorizationServiceAddressingLocator locator =
            new AuthorizationServiceAddressingLocator();
        EndpointReferenceType endpoint = new EndpointReferenceType();
        try {
            endpoint.setAddress(new Address(authzService));
            this.authzPort =
                locator.getSAMLRequestPortTypePort(endpoint);
        } catch (URI.MalformedURIException e) {
            String err = i18n.getMessage("authzServiceInit", authzService);
            throw new InitializeException(err, e);
        } catch (ServiceException e) {
            String err = i18n.getMessage("authzServiceInit", authzService);
            throw new InitializeException(err, e);
        }

        Stub stub = (Stub) this.authzPort;

        String protectionLevel = (String) config.getProperty(
            name, SAMLAuthorizationConstants.PROTECTION_LEVEL_PROPERTY);

        if (protectionLevel != null &&
            protectionLevel.equals(SAMLAuthorizationConstants.PRIVACY)) {
            protection = Constants.ENCRYPTION;
        }

        mechanism = (String) config.getProperty(
            name, SAMLAuthorizationConstants.SECURITY_MECHANISM_PROPERTY);

        if ((mechanism == null) || (mechanism.trim().equals(""))) {
            if (authzService.startsWith("https")) {
                mechanism = Constants.GSI_TRANSPORT;
            } else {
                mechanism = Constants.GSI_SEC_MSG;
            }
        } else {
            mechanism = mechanism.trim();
            if (mechanism.equals(SAMLAuthorizationConstants.MESSAGE)) {
                mechanism = Constants.GSI_SEC_MSG;
            } else if (mechanism.equals(
                SAMLAuthorizationConstants.CONVERSATION)) {
                mechanism = Constants.GSI_SEC_CONV;
            } else if (mechanism.equals(SAMLAuthorizationConstants.NONE)) {
                mechanism = null;
            } else {
                if (!authzService.startsWith("https")) {
                    mechanism = Constants.GSI_SEC_MSG;
                } else {
                    mechanism = Constants.GSI_TRANSPORT;
                }
            }
        }

        if (mechanism != null) {
            stub._setProperty(mechanism, protection);
            Subject systemSubject = null;
            org.apache.axis.MessageContext ctx =
                org.apache.axis.MessageContext.getCurrentContext();
            SecurityManager manager = SecurityManager.getManager(ctx);
            try {
                systemSubject = manager.getSystemSubject();
            } catch (SecurityException exp) {
                String err = i18n.getMessage("noSystemCreds");
                logger.debug(err, exp);
                throw new InitializeException(err, exp);
            }
            GlobusGSSCredentialImpl credential =
                (GlobusGSSCredentialImpl) JaasGssUtil.getCredential(
                    systemSubject);
            stub._setProperty(GSIConstants.GSI_CREDENTIALS, credential);
        }

        
        String authzServiceIdentity = (String) config.getProperty(
            name, SAMLAuthorizationConstants.AUTHZ_SERVICE_IDENTITY_PROPERTY);

        if ((authzServiceIdentity == null) ||
            (authzServiceIdentity.trim().equals(""))) {
            if (mechanism == null) {
                stub._setProperty(Constants.AUTHORIZATION,
                                  NoAuthorization.getInstance());
            } else {
                stub._setProperty(Constants.AUTHORIZATION,
                                  SelfAuthorization.getInstance());
            }
        } else {
            stub._setProperty(Constants.AUTHORIZATION,
                              new IdentityAuthorization(authzServiceIdentity));
        }
        
        authzServiceCertFile = (String) config.getProperty(
            name, SAMLAuthorizationConstants.AUTHZ_SERVICE_CERT_FILE_PROPERTY);
        if (authzServiceCertFile != null) {
            try {
                this.authzServiceCert =
                    CertUtil.loadCertificate(authzServiceCertFile);
            } catch (IOException ioe) {
                String err = i18n.getMessage("encryptionCert",
                                             authzServiceCertFile);
                throw new InitializeException(err, ioe);
            } catch (GeneralSecurityException gse) {
                String err = i18n.getMessage("encryptionCert",
                                             authzServiceCertFile);
                throw new InitializeException(err, gse);
            }
        } else {
            this.authzServiceCert = (X509Certificate) config.getProperty(
                name, SAMLAuthorizationConstants.AUTHZ_SERVICE_CERT_PROPERTY);
        }

        if (this.authzServiceCert != null) {
            Subject subject = new Subject();
            EncryptionCredentials encryptionCreds =
                new EncryptionCredentials(new X509Certificate[]
                { this.authzServiceCert });
            subject.getPublicCredentials().add(encryptionCreds);
            stub._setProperty(Constants.PEER_SUBJECT, subject);
        } else {
            if (mechanism == Constants.GSI_SEC_MSG &&
                protection == Constants.ENCRYPTION) {
                String err = i18n.getMessage(
                    "encryptionCertProp", new Object[]{
                        SAMLAuthorizationConstants.AUTHZ_SERVICE_CERT_PROPERTY,
                        SAMLAuthorizationConstants.AUTHZ_SERVICE_CERT_FILE_PROPERTY});
                throw new InitializeException(err);
            }

            if (this.sigReq == true) {
                String err = i18n.getMessage(
                    "verificationCertProp", new Object[]{
                        SAMLAuthorizationConstants.AUTHZ_SERVICE_CERT_PROPERTY,
                        SAMLAuthorizationConstants.AUTHZ_SERVICE_CERT_FILE_PROPERTY});
                throw new InitializeException(err);
            }
        }


        String decisionStr = (String) config.getProperty(
            name, SAMLAuthorizationConstants.SIMPLE_DECISION_PROPERTY);
        logger.debug("Decision string " + decisionStr);
        if ((decisionStr != null) && (!decisionStr.trim().equals(""))) {
            simpleDecision =
                (Boolean.valueOf(decisionStr)).booleanValue();
        }

        logger.debug("Decision value " + simpleDecision);

        String signatureRequestedStr = (String) config.getProperty(
            name, SAMLAuthorizationConstants.REQ_SIGNED_PROPERTY);
        logger.debug("Request signed string " + signatureRequestedStr);
        if ((signatureRequestedStr != null) &&
            (!signatureRequestedStr.trim().equals(""))) {
            sigReq =
                (Boolean.valueOf(signatureRequestedStr)).booleanValue();
        }
        logger.debug("Request signed value " + sigReq);
    }

    public String[] getPolicyNames() {
        return null;
    }

    public Node getPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }

    public Node setPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }

    public void close() throws CloseException {
    }

    // Ideally, properties like simple decision and signed/unsigned
    // query should be read from PDPConfig, but that would imply each
    // service/resource using this will need to configure it. For now,
    // allowing configuration to be set at container level only.
    public boolean isPermitted(Subject peerSubject, MessageContext context,
                               javax.xml.namespace.QName op)
        throws AuthorizationException {

        logger.debug("Authorize invoked");

        String subjectName = null;
        String nameQualifier = null;
        String format = null;
        Vector confirmationMethods = null;

        Set peerPrincipals = null;

        if (peerSubject != null)
            peerPrincipals = peerSubject.getPrincipals();

        org.apache.axis.MessageContext ctx =
            (org.apache.axis.MessageContext)context;
        SecurityManager manager = SecurityManager.getManager(ctx);
        logger.debug("caller identity" + manager.getCaller());
        // If subject is null or anonymous
        if ((peerPrincipals == null) || peerPrincipals.isEmpty()) {
            subjectName = SAMLAuthorizationConstants.ANY_SUBJECT_NAME;
            format = SAMLAuthorizationConstants.ANY_NAME_IDENTIFIER_FORMAT;
        } else {
            // User's identity (Client's identity)
            subjectName = manager.getCaller();
            // FIXME: Issuer's identity (Client) ?
            nameQualifier = null;
            format = SAMLAuthorizationConstants.X509_FORMAT;
            confirmationMethods = new Vector(1);
            confirmationMethods.add(
                        SAMLAuthorizationConstants.X509_CONFIRMATION_METHOD);
        }
        logger.debug("Subject name " + subjectName + " nameQualifier "
                     + nameQualifier + " format " + format);

        SAMLSubject samlSubject = null;
        try {
            samlSubject =
                new SAMLSubject(subjectName, nameQualifier, format,
                                confirmationMethods, null, null);
        } catch (SAMLException exp) {
            String err = i18n.getMessage("samlObjConstruct", "SAMLSubject");
            logger.debug(err, exp);
            throw new AuthorizationException(err, exp);
        }

        // Resource : String representation of EPR
        String resource = null;
        try {
            resource = AuthUtil.getEPRAsString(ctx);
        } catch (SecurityException exp) {
            String err = i18n.getMessage("resourceErr");
            logger.debug(err, exp);
            throw new AuthorizationException(err, exp);
        }

        // Action name (operation name)
        // FIXME: maybe include namespace also ?
        String operationName = op.getLocalPart();
        logger.debug("Operation name " + operationName);

        Vector samlActions = new Vector();
        try {
            SAMLAction samlAction =
                new SAMLAction(SAMLAuthorizationConstants.ACTION_OPERATION_NS,
                               operationName);
            samlActions.add(samlAction);
        } catch (SAMLException exp) {
            String err = i18n.getMessage("samlObjConstruct", "SAMLAction");
            logger.debug(err, exp);
            throw new AuthorizationException(err, exp);
        }

        SAMLAuthorizationDecisionQuery extendedQuery = null;
        try {
            QName signQName = new QName(null, "SAMLResponse");
            extendedQuery =
                new ExtendedAuthorizationDecisionQuery(samlSubject,
                                                       resource,
                                                       samlActions, null,
                                                       simpleDecision,
                                                       signQName, null);
        }  catch (SAMLException exp) {
            String err = i18n.getMessage("samlObjConstruct",
                                         "SAMLAuthzDecisionQuery");
            logger.debug(err, exp);
            throw new AuthorizationException(err, exp);
        }

        // Respond with
        Vector respondWith = new Vector();
        if (simpleDecision) {
            respondWith.add(SAMLAuthorizationConstants.SIMPLE_AUTHZ_DECISION);
        } else {
            respondWith.add(SAMLAuthorizationConstants.AUTHZ_DECISION);
        }

        SAMLRequest request = null;
        try {
            request = new SAMLRequest(respondWith, extendedQuery, null, null);
        } catch (SAMLException exp) {
            String err = i18n.getMessage("samlObjConstruct", "SAMLRequest");
            logger.debug(err, exp);
            throw new AuthorizationException(err, exp);
        }

        if (sigReq) {
            logger.debug("Signature required");
            Subject systemSubject = null;
            try {
                systemSubject = manager.getSystemSubject();
            } catch (SecurityException exp) {
                String err = i18n.getMessage("noSystemCreds");
                logger.debug(err, exp);
                throw new AuthorizationException(err, exp);
            }

            GlobusGSSCredentialImpl credential =
                (GlobusGSSCredentialImpl)JaasGssUtil.getCredential(
                                                     systemSubject);
            Vector certs = getCertificates(credential);

            try {
                    request.sign(XMLSignature.ALGO_ID_SIGNATURE_RSA,
                                 credential.getPrivateKey(), certs, false);

            } catch (SAMLException exp) {
                String err = i18n.getMessage("samlSign");
                logger.debug(err, exp);
                throw new AuthorizationException(err, exp);
            } catch (GSSException e) {                
                logger.debug(e);
                throw new AuthorizationException("Error when getting private key.",e);                
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(XMLUtils.ElementToString((Element) request.toDOM()));
        }

        Element requestElement = (Element) request.toDOM();
        NodeList requestChildren = requestElement.getChildNodes();
        List requestElements = new ArrayList();
        for (int i = 0; i < requestChildren.getLength(); i++) {
            Node child = requestChildren.item(i);
            if (child instanceof Element) {
                requestElements.add(
                    new SecurityMessageElement((Element) child));
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        String issueInstant = formatter.format(request.getIssueInstant());

        Request requestType = new Request(
            (MessageElement[]) requestElements.toArray(
                new MessageElement[requestElements.size()]),
            issueInstant,
            1,
            0,
            request.getRequestId());

        String targetServicePath = ContextUtils.getTargetServicePath(ctx);
        logger.debug("Target Service Path: " + targetServicePath);
        Response responseType = null;
        try {
            responseType = this.authzPort.SAMLRequest(requestType);
        } catch (Exception exp) {
            String err = i18n.getMessage("authzService",
                                         ((Stub) this.authzPort)._getProperty(
                                             Stub.ENDPOINT_ADDRESS_PROPERTY));
            logger.debug(err, exp);
            throw new AuthorizationException(err, exp);
        }

        // This is an error since atleast a response with no actions
        // should have been returned.
        if (responseType == null) {
            String err = i18n.getMessage("nullResponse");
            logger.debug(err);
            throw new AuthorizationException(err);
        }

        SAMLResponse response = null;
        try {
            response = new SAMLResponse(
                ((MessageElement) responseType.get_any()[0].getParentElement()).
                getAsDOM());
        } catch (Exception exp) {
            // Should be an error sent by authz service as part of
            // response. So send in message as in original exception.
            logger.debug("Eception is of type " + exp.getClass().getName());
            if (exp instanceof SAMLException) {
                logger.debug("", exp);
                throw new AuthorizationException(exp.getMessage(), exp);
            }
            // Some exception other than one sent by authz service in
            // SAMLResponse itself.
            String err = i18n.getMessage("badResponse");
            logger.debug(err, exp);
            throw new AuthorizationException(err, exp);
        }

        if (this.sigReq) {
            if (response.isSigned()) {
                try {
                    response.verify(this.authzServiceCert, false);
                } catch (SAMLException samlExp) {
                    String err = i18n.getMessage("badResponseSig");
                    logger.debug(err, samlExp);
                    throw new AuthorizationException(err, samlExp);
                }
            } else {
                String err = i18n.getMessage("responseSigRequired");
                throw new AuthorizationException(err);
            }
        }

        // Assert that request id matches the InResponseTo
        String inResponseTo = response.getInResponseTo();
        if ((inResponseTo == null) ||
            (!inResponseTo.equals(request.getRequestId()))) {
            String err = i18n.getMessage("badInResponseTo");
            logger.debug(err);
            throw new AuthorizationException(err);
        }

        boolean valid = false;
        Iterator assertionsItr = response.getAssertions();
        logger.debug("Getting assertions");
        if ((assertionsItr != null) && (assertionsItr.hasNext())) {
            logger.debug("assertions present");
            SAMLAssertion assertion = (SAMLAssertion)assertionsItr.next();
            Iterator stmtsItr = assertion.getStatements();
            if ((stmtsItr != null) && (stmtsItr.hasNext())) {
                Object obj = stmtsItr.next();
                if (simpleDecision) {
                    if (obj instanceof SimpleAuthorizationDecisionStatement) {
                        valid = processSimpleAuthzStmt(obj, samlSubject);
                    }
                } else {
                    if (obj instanceof SAMLAuthorizationDecisionStatement) {
                        valid = processAuthzStmt(obj, resource, samlActions,
                                                 samlSubject);
                    }

                }
            }
        }

        if (!valid) {
            logger.warn(i18n.getMessage("samlAuthFailed"));
        }

        return valid;
    }

    private boolean processSimpleAuthzStmt(Object statement,
                                           SAMLSubject samlSubject) {
        logger.debug("Process simple authz stmt");
        SimpleAuthorizationDecisionStatement stmt =
            (SimpleAuthorizationDecisionStatement)statement;
        if (stmt.getDecision().equals(SAMLDecision.PERMIT)) {
            logger.debug("decision is permit");
            return (samlSubject.equals(stmt.getSubject()));
        }
        return false;
    }

    private boolean processAuthzStmt(Object statement, String resource,
                                     Vector actions, SAMLSubject samlSubject) {
        logger.debug("Process authz stmt ");
        SAMLAuthorizationDecisionStatement stmt =
            (SAMLAuthorizationDecisionStatement)statement;
        if ((stmt.getDecision().equals(SAMLDecision.PERMIT)) &&
            (stmt.getResource().equals(resource))) {
            logger.debug("permit and resource");
            if (samlSubject.equals(stmt.getSubject())) {
                // all actions for which query was made shld return permit.
                if (stmt.getActionsCol() != null)
                    return stmt.getActionsCol().containsAll(actions);
            }
        }
        return false;
    }

    // Returns certs if it exists, else null
    private Vector getCertificates(GlobusGSSCredentialImpl credential) {

        X509Certificate certArray[] = credential.getCertificateChain();
        Vector certs = null;
        if (certArray.length > 0) {
            logger.debug("Cert array is not null");
            certs = new Vector(certArray.length);
            for (int i=0; i<certArray.length; i++) {
                certs.add(certArray[i]);
            }
        } else
            logger.debug("Null");
        return certs;
    }
}
