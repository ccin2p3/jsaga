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
package org.globus.delegation;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import org.apache.ws.patched.security.WSSConfig;
import org.apache.ws.patched.security.message.token.PKIPathSecurity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.encoding.AnyContentType;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReference;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.message.addressing.ReferencePropertiesType;

import org.oasis.wsrf.properties.GetResourcePropertyResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.globus.delegation.service.DelegationHome;
import org.globus.delegation.service.DelegationResource;
import org.globus.delegationService.CertType;
import org.globus.delegationService.DelegationFactoryPortType;
import org.globus.delegationService.DelegationFactoryServiceAddressingLocator;
import org.globus.delegationService.DelegationFactoryServiceLocator;
import org.globus.delegationService.DelegationPortType;
import org.globus.delegationService.DelegationServiceAddressingLocator;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.I18n;
import org.globus.ws.trust.RequestSecurityTokenResponseType;
import org.globus.ws.trust.RequestSecurityTokenType;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.ContextCrypto;

import org.globus.wsrf.security.SecurityException;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.utils.XmlUtils;

public class DelegationUtil {

    static Log logger = LogFactory.getLog(DelegationUtil.class.getName());

    private static I18n i18n =
        I18n.getI18n("org.globus.delegation.errors",
                     DelegationUtil.class.getClassLoader());
    /**
     * Create a token containing service's public certificate (to expose
     * as Resource Property)
     *
     * @param servicePath
     *        Service path name used to store properties in JNDI
     * @param useDefault
     *        Indicates whether default credentials should be used if
     *        service is no configured with credential.
     * @return A token with the certificate chain
     * @throws DelegationException
     */
    public static PKIPathSecurity
        getServiceCertAsToken(String servicePath, boolean useDefault)
        throws DelegationException {

        GlobusCredential cred = getServiceCredential(servicePath, useDefault);

        if (cred == null) {
            throw new DelegationException(i18n.getMessage("insecureService"));
        }

        // First cert is the end cert.
        X509Certificate[] certChain = cred.getCertificateChain();

        PKIPathSecurity publicCertToken = null;
        if (certChain != null) {
            publicCertToken = getPKIToken(certChain, false);
        } else {
            throw new DelegationException(i18n
                                          .getMessage("certChainNotFound"));
        }

        return publicCertToken;
    }

    /**
     * Get private key of the service's credential
     *
     * @param servicePath
     *        Service path name used to store properties in JNDI
     * @param useDefault
     *        Indicates whether default credentials should be used if
     *        service is no configured with credential.
     * @return private key
     * @throws DelegationException
     */
    public static PrivateKey getServicePrivateKey(String servicePath,
                                                  boolean useDefault)
        throws DelegationException {

        GlobusCredential cred = getServiceCredential(servicePath, useDefault);

        if (cred == null) {
            throw new DelegationException(i18n.getMessage("insecureService"));
        }

        return cred.getPrivateKey();
    }


    /**
     * Register the listener object with deleagtion resource represented
     * by EPR. The delegation service must be running in same hosting
     * environment.
     *
     * @param epr
     *        ERP of the delegated credential
     * @param listener
     *        Listener object
     */
    public static void
        registerDelegationListener(EndpointReferenceType epr,
                                   DelegationListener listener)
        throws DelegationException {
        DelegationResource resource = getDelegationResource(epr);
        resource.addRefreshListener(listener);
    }

    public static void
        registerDelegationListener(EndpointReferenceType epr,
                                   DelegationListener listener,
                                   Subject subject)
        throws DelegationException {

        DelegationResource resource = getDelegationResource(epr);
        resource.addRefreshListener(listener, subject);
    }

    /**
     * Remove the listener object with said id on delegation resoruce 
     * represented by EPR. The delegation service must be running in
     * same hosting environment.
     *
     * @param epr
     *        ERP of the delagated credential
     * @param listenerId
     *        Listener id
     */
    public static void
        removeDelegationListener(EndpointReferenceType epr, String listenerId)
        throws DelegationException {
        DelegationResource resource = getDelegationResource(epr);
        resource.removeRefreshListener(listenerId);
    }

    /**
     * Return the delegation resource referred to by EPR. The
     * delegation  service must be running in same hosting environment.
     *
     * @param epr
     *        ERP of the delagated credential
     */
    public static DelegationResource
        getDelegationResource(EndpointReferenceType epr)
        throws DelegationException {

        // Get address
        String address = epr.getAddress().toString();
        logger.debug("Address: " + address);
        // extract service path
        String servicePath = address.substring(address.lastIndexOf("/") + 1,
                                               address.length());
        logger.debug("Service path: " + servicePath);

        DelegationHome delegHome = null;
        String lookUp = org.globus.wsrf.Constants.JNDI_SERVICES_BASE_NAME
            + servicePath + "/"
            + org.globus.wsrf.Constants.HOME_NAME;
        try {
            Context initialContext = new InitialContext();
            delegHome = (DelegationHome)initialContext.lookup(lookUp);
        } catch (NamingException namingExp) {
            throw new DelegationException(i18n
                                          .getMessage("resourceHomeNotFound",
                                                      new Object[] { lookUp }),
                                          namingExp);
        }

        // Get resource key
        Class keyClass = delegHome.getKeyTypeClass();
        QName keyQName = delegHome.getKeyTypeName();
        String localName = keyQName.getLocalPart();
        String ns = keyQName.getNamespaceURI();
        SimpleResourceKey key = null;

        try {
            ReferencePropertiesType refPropType = epr.getProperties();
            for (int i=0; i<refPropType.size(); i++) {
                MessageElement[] elem = refPropType.get_any();
                Element elem0 = elem[0].getAsDOM();
                // get the delegation reference properties
                if (localName.equals(elem0.getLocalName()) &&
                    (ns.equals(elem0.getNamespaceURI()))) {
                    key = new SimpleResourceKey(elem[i], keyClass);
                    break;
                }
            }
        } catch (Exception exp) {
            throw new DelegationException(i18n.getMessage("referencePropErr"),
                                          exp);
        }

        if (key == null)
            throw new DelegationException(i18n.getMessage("invalidKey"));
        logger.debug("Resource key " + key);

        // get resource
        DelegationResource resource = null;
        try {
            resource = (DelegationResource)delegHome.find(key);
        } catch (ResourceException exp) {
            logger.error(i18n.getMessage("unableToGetResource"), exp);
            throw new DelegationException(i18n
                                          .getMessage("unableToGetResource"),
                                          exp);
        }

        return resource;
    }

    /**
     * Create a new proxy with said lifetime, using the public key of
     * certificate and signed by issuing credential. Return the proxy
     * as a security token.
     *
     * @param issuingCred
     *        Credential issuing the proxy
     * @param certificate
     *        The public certificate of the new proxy
     * @param lifetime
     *        Lifetime of the new proxy in seconds
     * @param fullDelegation
     *        Indicates whether full delegation is required.
     * @return RequestSecurityTokenType
     *         The new proxy as a security token.
     * @see #getTokenToDelegate(X509Certificate[], PrivateKey,
     * PublicKey, int, boolean)
     */
    public static RequestSecurityTokenType
        getTokenToDelegate(GlobusCredential issuingCred,
                           X509Certificate certificate,
                           int lifetime, boolean fullDelegation)
        throws DelegationException {
        return getTokenToDelegate(issuingCred.getCertificateChain(),
                                  issuingCred.getPrivateKey(),
                                  certificate.getPublicKey(), lifetime,
                                  fullDelegation);
    }

    /**
     * Create a new proxy with said lifetime, signed by issuing
     * credential. Return the proxy as a security token.
     *
     * @param issuerCertificateChain
     *        First certificate in this chain is used as issuing
     *        certificate
     * @param issuerKey
     *        New proxy will be signed with this key
     * @param publicKey
     *        The public key of the new proxy
     * @param lifetime
     *        Lifetime of the new proxy in seconds
     * @param fullDelegation
     *        Indicates whether full delegation is required.
     * @return RequestSecurityTokenType
     *         The new proxy as a security token.
     */
    public static RequestSecurityTokenType
        getTokenToDelegate(X509Certificate[] issuerCertificateChain,
                           PrivateKey issuerKey, PublicKey publicKey,
                           int lifetime, boolean fullDelegation)
        throws DelegationException {

        // Uses first certificate in chain  (cert[0])
        BouncyCastleCertProcessingFactory certFactory =
            BouncyCastleCertProcessingFactory.getDefault();
        X509Certificate newCert = null;
        int delegType = GSIConstants.DELEGATION_LIMITED;
        if (fullDelegation)
            delegType = GSIConstants.DELEGATION_FULL;

        try {
            newCert = certFactory
                .createProxyCertificate(issuerCertificateChain[0],
                                        issuerKey,
                                        publicKey, lifetime, delegType,
                                        (X509ExtensionSet)null, null);
        } catch (GeneralSecurityException exp) {
            logger.error(i18n.getMessage("createDelegCred"), exp);
            throw new DelegationException(i18n.getMessage("createDelegCred"),
                                          exp);
        }

        X509Certificate[] newChain =
            new X509Certificate[issuerCertificateChain.length + 1];
        newChain[0] = newCert;
        System.arraycopy(issuerCertificateChain, 0, newChain, 1,
                         issuerCertificateChain.length);

        logger.debug("New delegated chain");
        for (int i=0; i<newChain.length; i++) {
            logger.debug(newChain[i].getSubjectDN());
        }

        PKIPathSecurity token = getPKIToken(newChain, false);

        // New chain
        logger.debug("New certificate chain");
        for (int i=0; i<newChain.length; i++) {
            logger.debug(newChain[i].getSubjectDN());
        }

        MessageElement msgElem =
            new MessageElement(token.getElement());
        RequestSecurityTokenType requestToken =
            new RequestSecurityTokenType();
        requestToken.set_any(new MessageElement[] { msgElem });

        return requestToken;
    }

    /**
     * Store the request token (delegated credential) on the
     * delegation service. Lifetime defaults to lifetime of issuing
     * credential.
     *
     * @param delegationServiceUrl
     *        Address of delegation service
     * @param issuingCred
     *        Credential issuing the proxy
     * @param certificate
     *        The public certificate of the new proxy
     * @param fullDelegation
     *        Indicates whether full delegation is required.
     * @param desc
     *        Client security descriptor with relevant security properties.
     */
    public static EndpointReferenceType delegate(String delegationServiceUrl,
                                                 GlobusCredential issuingCred,
                                                 X509Certificate certificate,
                                                 boolean fullDelegation,
                                                 ClientSecurityDescriptor desc)
        throws DelegationException {
        return delegate(delegationServiceUrl, issuingCred, certificate, 
                        (new Long(issuingCred.getTimeLeft())).intValue(), 
                        fullDelegation, desc);
    }

    /**
     * Store the request token (delegated credential) on the
     * delegation service. 
     *
     * @param delegationServiceUrl
     *        Address of delegation service
     * @param issuingCred
     *        Credential issuing the proxy
     * @param certificate
     *        The public certificate of the new proxy
     * @param lifetime
     *        Lifetime of the new proxy in seconds
     * @param fullDelegation
     *        Indicates whether full delegation is required.
     * @param desc
     *        Client security descriptor with relevant security properties.
     */
    public static EndpointReferenceType delegate(String delegationServiceUrl,
                                                 GlobusCredential issuingCred,
                                                 X509Certificate certificate,
                                                 int lifetime,
                                                 boolean fullDelegation,
                                                 ClientSecurityDescriptor desc)
        throws DelegationException {
        RequestSecurityTokenType token = getTokenToDelegate(issuingCred,
                                                            certificate,
                                                            lifetime,
                                                            fullDelegation);

        DelegationFactoryServiceLocator locator =
            new DelegationFactoryServiceLocator();

        EndpointReference ref =  null;
        try {
            URL url = new URL(delegationServiceUrl);
            DelegationFactoryPortType delegationPort =
                locator.getDelegationFactoryPortTypePort(url);
            if (desc != null) {
                ((Stub)delegationPort)
                    ._setProperty(Constants.CLIENT_DESCRIPTOR, desc);
            }

            RequestSecurityTokenResponseType response =
                delegationPort.requestSecurityToken(token);

            // Extract EPR from response
            MessageElement elem[] = response.get_any();

            ref = new EndpointReference(elem[0].getAsDOM());
        } catch (Exception exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }
        return ref;
    }

    /**
     * Refresh credential referred to by EPR. 
     *
     * @param issuingCred
     *        Credential issuing the proxy
     * @param certToSign
     *        The public certificate of the new proxy
     * @param lifetime
     *        Lifetime of the new proxy in seconds
     * @param fullDelegation
     *        Indicates whether full delegation is required.
     * @param authz
     *        Client side authorization to use for the call.
     * @param desc
     *        Client security descriptor with relevant security
     *        properties.
     * @param epr
     *        EPR referring to credential that needs to be replaced.
     */
    public static void refresh(GlobusCredential issuingCred,
                               X509Certificate certToSign,
                               int lifetime,
                               boolean fullDelegation,
                               ClientSecurityDescriptor desc,
                               EndpointReferenceType epr)
        throws DelegationException {

        RequestSecurityTokenType requestToken =
            getTokenToDelegate(issuingCred, certToSign, lifetime,
                               fullDelegation);
        DelegationPortType delegationPort = null;
        try {
            DelegationServiceAddressingLocator locator =
                new DelegationServiceAddressingLocator();
            delegationPort = locator.getDelegationPortTypePort(epr);
        } catch (Exception exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }
        ((Stub)delegationPort)
            ._setProperty(Constants.CLIENT_DESCRIPTOR, desc);

        try {
            delegationPort.refresh(requestToken);
        } catch (Exception exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }
    }

    /**
     * Retrieve certificate chain from resource property on Delegation
     * Factory Service.
     *
     * @param epr
     *        Endpoint reference to delegation factory service
     * @param desc
     *        Client security descriptor with relevant security
     *        properties.
     * @return X509Certificate[]
     *        Certificate chain contained in the token.
     */
    public static X509Certificate[]
        getCertificateChainRP(EndpointReferenceType epr, 
                              ClientSecurityDescriptor desc)
        throws DelegationException {
        QName certChainRp =
            new QName(DelegationConstants.NS, "CertificateChain");
        return getCertificateChainRP(epr, certChainRp, CertType.class, desc);
    }

    /**
     * Retrieve certificate chain from resource property on Delegation
     * Factory Service. The class it deserializes into should contain
     * a <code>BinarySecurity</code> token.
     *
     * @param epr
     *        Endpoint reference to delegation factory service
     * @param qName
     *        QName of the resource property
     * @param rpClass
     *        Class to deserialize it as
     * @param desc
     *        Client security descriptor with relevant security
     *        properties.
     * @return X509Certificate[]
     *        Certificate chain contained in the token.
     */
    public static X509Certificate[]
        getCertificateChainRP(EndpointReferenceType epr, QName qName,
                              Class rpClass, ClientSecurityDescriptor desc)
        throws DelegationException {

        DelegationFactoryPortType delegationPort = null;
        try {
            DelegationFactoryServiceAddressingLocator locator =
                new DelegationFactoryServiceAddressingLocator();
            delegationPort = locator.getDelegationFactoryPortTypePort(epr);
        } catch (Exception exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }

        if (desc != null) {
            ((Stub)delegationPort)._setProperty(Constants.CLIENT_DESCRIPTOR, 
                                                desc);
        }
        
        GetResourcePropertyResponse response = null;
        try {
            response = delegationPort.getResourceProperty(qName);
        } catch (RemoteException exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }

        PKIPathSecurity token = null;
        try {
            MessageElement elem[] = response.get_any();
            AnyContentType certType =
                (AnyContentType)ObjectDeserializer.toObject(elem[0], rpClass);

            elem = certType.get_any();
            token = new PKIPathSecurity(WSSConfig.getDefaultWSConfig(),
                                        elem[0].getAsDOM());
        } catch (Exception exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }

        X509Certificate[] certChain = null;
        // certChain[0] is end cert.
        try {
            certChain = token.getX509Certificates(false,
                                                  new ContextCrypto());
        } catch (IOException exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }

        // Verify this chain, create temp credential
        GlobusCredential credential = new GlobusCredential(null, certChain);
        try {
            credential.verify();
        } catch (Exception exp) {
            throw new DelegationException(exp);
        }

        return certChain;
    }

    private static GlobusCredential getServiceCredential(String servicePath,
                                                         boolean useDefault)
        throws DelegationException {

        SecurityManager manager = SecurityManager.getManager();
        Subject subject = null;
        try {
            subject = manager.getServiceSubject(servicePath);
        } catch (SecurityException exp) {
            throw new DelegationException(exp);
        }

        if (subject == null)
            throw new DelegationException(i18n.getMessage("insecureService"));

        GlobusCredential cred = null;
        Iterator privateCred = subject.getPrivateCredentials().iterator();
        while (privateCred.hasNext()) {
            Object object = privateCred.next();
            if (object instanceof GlobusGSSCredentialImpl) {
                cred =
                    ((GlobusGSSCredentialImpl)object).getGlobusCredential();
                break;
            }
        }

        if ((useDefault) && (cred == null)) {
            try {
                cred =
                    GlobusCredential.getDefaultCredential();
            } catch (GlobusCredentialException exp) {
                throw new DelegationException(exp);
            }
        }
        return cred;
    }

    private static PKIPathSecurity
        getPKIToken(X509Certificate[] certChain, boolean reverse)
        throws DelegationException {

        PKIPathSecurity token = null;
        try {
            Document doc = XmlUtils.newDocument();
            token = new PKIPathSecurity(WSSConfig.getDefaultWSConfig(), doc);
            token.setX509Certificates(certChain, reverse, new ContextCrypto());
        } catch (Exception exp) {
            logger.error(exp);
            throw new DelegationException(exp);
        }
        return token;
    }
}
