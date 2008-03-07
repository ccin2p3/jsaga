/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ws.patched.security.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.components.crypto.Crypto;
import org.apache.ws.patched.security.components.crypto.CryptoFactory;
import org.apache.ws.patched.security.saml.SAMLIssuer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLStatement;
import org.opensaml.SAMLSubject;
import org.w3c.dom.Document;


import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

/**
 * Builds a WS SAML Assertion and inserts it into the SOAP Envelope. Refer to
 * the WS specification, SAML Token profile
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class SAMLIssuerImpl implements SAMLIssuer {

    private static Log log = LogFactory.getLog(SAMLIssuerImpl.class.getName());

    private SAMLAssertion sa = null;

    private Document instanceDoc = null;

    private Properties properties = null;

    private Crypto issuerCrypto = null;
    private String issuerKeyPassword = null;
    private String issuerKeyName = null;

    private boolean senderVouches = true;

    private String[] confirmationMethods = new String[1];
    private Crypto userCrypto = null;
    private String username = null;

    /**
     * Constructor.
     */
    public SAMLIssuerImpl() {
    }

    public SAMLIssuerImpl(Properties prop) {
        /*
         * if no properties .. just return an instance, the rest will be done
         * later or this instance is just used to handle certificate
         * conversions in this implementatio
         */
        if (prop == null) {
            return;
        }
        properties = prop;

        String cryptoProp =
                properties.getProperty("org.apache.ws.security.saml.issuer.cryptoProp.file");
        if (cryptoProp != null) {
            issuerCrypto = CryptoFactory.getInstance(cryptoProp);
            issuerKeyName =
                    properties.getProperty("org.apache.ws.security.saml.issuer.key.name");
            issuerKeyPassword =
                    properties.getProperty("org.apache.ws.security.saml.issuer.key.password");
        }

        if ("senderVouches"
                .equals(properties.getProperty("org.apache.ws.security.saml.confirmationMethod"))) {
            confirmationMethods[0] = SAMLSubject.CONF_SENDER_VOUCHES;
        } else if (
                "keyHolder".equals(properties.getProperty("org.apache.ws.security.saml.confirmationMethod"))) {
            confirmationMethods[0] = SAMLSubject.CONF_HOLDER_KEY;
            senderVouches = false;
        } else {
            // throw something here - this is a mandatory property
        }
    }

    /**
     * Creates a new <code>SAMLAssertion</code>.
     * <p/>
     * <p/>
     * A complete <code>SAMLAssertion</code> is constructed.
     *
     * @return SAMLAssertion
     */
    public SAMLAssertion newAssertion() { // throws Exception {
        log.debug("Begin add SAMLAssertion token...");

        /*
         * if (senderVouches == false && userCrypto == null) { throw
         * exception("need user crypto data to insert key") }
         */
        // Issuer must enable crypto fubctions to get the issuer's certificate
        String issuer =
                properties.getProperty("org.apache.ws.security.saml.issuer");
        String name =
                properties.getProperty("org.apache.ws.security.saml.subjectNameId.name");
        String qualifier =
                properties.getProperty("org.apache.ws.security.saml.subjectNameId.qualifier");
        try {
            SAMLNameIdentifier nameId =
                    new SAMLNameIdentifier(name, qualifier, "");
            String subjectIP = null;
            String authMethod = null;
            if ("password"
                    .equals(properties.getProperty("org.apache.ws.security.saml.authenticationMethod"))) {
                authMethod =
                        SAMLAuthenticationStatement.AuthenticationMethod_Password;
            }
            Date authInstant = new Date();
            Collection bindings = null;

            SAMLSubject subject =
                    new SAMLSubject(nameId,
                            Arrays.asList(confirmationMethods),
                            null,
                            null);
            SAMLStatement[] statements =
                    {
                        new SAMLAuthenticationStatement(subject,
                                authMethod,
                                authInstant,
                                subjectIP,
                                null,
                                bindings)};
            sa =
                    new SAMLAssertion(issuer,
                            null,
                            null,
                            null,
                            null,
                            Arrays.asList(statements));

            if (!senderVouches) {
                KeyInfo ki = new KeyInfo(instanceDoc);
                try {
                    X509Certificate[] certs =
                            userCrypto.getCertificates(username);
                    X509Data certElem = new X509Data(instanceDoc);
                    certElem.addCertificate(certs[0]);
                    ki.add(certElem);
                } catch (WSSecurityException e) {
                    return null;
                } catch (XMLSecurityException e) {
                    return null;
                }
                subject.setKeyInfo(ki);
                // prepare to sign the SAML token
                try {
                    X509Certificate[] issuerCerts =
                            issuerCrypto.getCertificates(issuerKeyName);

                    String sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_RSA;
                    String pubKeyAlgo =
                            issuerCerts[0].getPublicKey().getAlgorithm();
                    log.debug("automatic sig algo detection: " + pubKeyAlgo);
                    if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                        sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_DSA;
                    }
                    java.security.Key issuerPK =
                            issuerCrypto.getPrivateKey(issuerKeyName,
                                    issuerKeyPassword);
                    sa.sign(sigAlgo, issuerPK, Arrays.asList(issuerCerts));
                } catch (WSSecurityException e1) {
                    e1.printStackTrace();
                    return null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return null;
                }
            }
        } catch (SAMLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.toString());
        }
        return sa;
    }

    /**
     * @param userCrypto The userCrypto to set.
     */
    public void setUserCrypto(Crypto userCrypto) {
        this.userCrypto = userCrypto;
    }

    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Returns the issuerCrypto.
     */
    public Crypto getIssuerCrypto() {
        return issuerCrypto;
    }

    /**
     * @return Returns the issuerKeyName.
     */
    public String getIssuerKeyName() {
        return issuerKeyName;
    }

    /**
     * @return Returns the issuerKeyPassword.
     */
    public String getIssuerKeyPassword() {
        return issuerKeyPassword;
    }

    /**
     * @return Returns the senderVouches.
     */
    public boolean isSenderVouches() {
        return senderVouches;
    }

    /**
     * @param instanceDoc The instanceDoc to set.
     */
    public void setInstanceDoc(Document instanceDoc) {
        this.instanceDoc = instanceDoc;
    }
}
