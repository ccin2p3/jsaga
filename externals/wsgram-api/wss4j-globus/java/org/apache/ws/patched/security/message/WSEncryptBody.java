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

package org.apache.ws.patched.security.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.patched.security.SOAPConstants;
import org.apache.ws.patched.security.WSConstants;
import org.apache.ws.patched.security.WSEncryptionPart;
import org.apache.ws.patched.security.WSSConfig;
import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.components.crypto.Crypto;
import org.apache.ws.patched.security.message.token.BinarySecurity;
import org.apache.ws.patched.security.message.token.Reference;
import org.apache.ws.patched.security.message.token.SecurityTokenReference;
import org.apache.ws.patched.security.message.token.X509Security;
import org.apache.ws.patched.security.util.WSSecurityUtil;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Vector;

/**
 * Encrypts a SOAP body inside a SOAP envelope according to WS Specification,
 * X509 profile, and adds the encryption data.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 * @author Werner Dittmann (Werner.Dittmann@siemens.com).
 */
public class WSEncryptBody extends WSBaseMessage {
    private static Log log = LogFactory.getLog(WSEncryptBody.class.getName());
    private static Log tlog = LogFactory.getLog("org.apache.ws.security.TIME");

    protected String symEncAlgo = WSConstants.TRIPLE_DES;
    protected String keyEncAlgo = WSConstants.KEYTRANSPORT_RSA15;
    protected String encCanonAlgo = null;
    protected byte[] embeddedKey = null;
    protected String embeddedKeyName = null;
    protected X509Certificate useThisCert = null;

    /**
     * Symmetric key used in the EncrytpedKey.
     */
    protected SecretKey symmetricKey = null;

    /**
     * Symmetric key that's actually used.
     */
    protected SecretKey encryptionKey = null;

    /**
     * Parent node to which the EncryptedKeyElement should be added.
     */
    protected Element parentNode = null;

    /**
     * SecurityTokenReference to be inserted into EncryptedData/keyInfo element.
     */
    protected SecurityTokenReference securityTokenReference = null;

    /**
     * Constructor.
     */
    public WSEncryptBody() {
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param actor The actor name of the <code>wsse:Security</code>
     *              header
     */
    public WSEncryptBody(String actor) {
        super(actor);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param actor The actor name of the <code>wsse:Security</code> header
     * @param mu    Set <code>mustUnderstand</code> to true or false
     */
    public WSEncryptBody(String actor, boolean mu) {
        super(actor, mu);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param wssConfig Configuration options for processing and building the <code>wsse:Security</code> header
     * @param actor     The actor name of the <code>wsse:Security</code> header
     * @param mu        Set <code>mustUnderstand</code> to true or false
     */
    public WSEncryptBody(WSSConfig wssConfig, String actor, boolean mu) {
        super(wssConfig, actor, mu);
    }

    /**
     * Sets the key to use during embedded encryption.
     * <p/>
     *
     * @param key to use during encryption. The key must fit the
     *            selected symmetrical encryption algorithm
     */
    public void setKey(byte[] key) {
        this.embeddedKey = key;
    }

    /**
     * Sets the algorithm to encode the symmetric key.
     * <p/>
     * Default is the <code>WSConstants.KEYTRANSPORT_RSA15</code>
     * algorithm.
     *
     * @param keyEnc specifies the key encoding algorithm.
     * @see WSConstants.KEYTRANSPORT_RSA15
     * @see WSConstants.KEYTRANSPORT_RSAOEP
     */
    public void setKeyEnc(String keyEnc) {
        keyEncAlgo = keyEnc;
    }

    /**
     * Set the user name to get the encryption certificate. The public
     * key of this certificate is used, thus no password necessary.
     * The user name is a keystore alias usually.
     * <p/>
     *
     * @param user
     */
    public void setUserInfo(String user) {
        this.user = user;
    }

    /**
     * Set the key name for EMBEDDED_KEYNAME
     *
     * @param embeddedKeyName
     */
    public void setEmbeddedKeyName(String embeddedKeyName) {
        this.embeddedKeyName = embeddedKeyName;
    }

    /**
     * Set the X509 Certificate to use for encryption.
     * If this is set <b>and</b> the key identifier is set
     * to <code>DirectReference</code> then use this certificate
     * to get the public key for encryption.
     *
     * @param cert is the X509 certificate to use for encryption
     */
    public void setUseThisCert(X509Certificate cert) {
        useThisCert = cert;
    }

    /**
     * Set the name of the symmetric encryption algorithm to use
     * <p/>
     * This encyrption alogrithm is used to encrypt
     * the data, i.e. the SOAP Body. If the algorithm
     * is not set then Triple DES is used. Refer to
     * WSConstants which algorithms are supported.
     * <p/>
     *
     * @param algo Is the name of the encyrption algorithm
     * @see WSConstants#TRIPLE_DES
     * @see WSConstants#AES_128
     * @see WSConstants#AES_192
     * @see WSConstants#AES_256
     */
    public void setSymmetricEncAlgorithm(String algo) {
        symEncAlgo = algo;
    }

    /**
     * Set the name of an optional canonicalization algorithm to use
     * before encryption
     * <p/>
     * This c14n alogrithm is used to serialize the data before
     * encryption, i.e. the SOAP Body. If the algorithm
     * is not set then a standard serialization is used (provided
     * by XMLCipher, usually a XMLSerializer according to DOM 3
     * specification).
     * <p/>
     *
     * @param algo Is the name of the canonicalization algorithm
     */
    public void setEncCanonicalization(String algo) {
        encCanonAlgo = algo;
    }

    /**
     * Get the name of symmetric encryption algorithm to use
     * <p/>
     * The name of the encyrption alogrithm to encrypt
     * the data, i.e. the SOAP Body. Refer to
     * WSConstants which algorithms are supported.
     * <p/>
     *
     * @return the name of the currently selected symmetric encryption
     *         algorithm
     * @see WSConstants#TRIPLE_DES
     * @see WSConstants#AES_128
     * @see WSConstants#AES_192
     * @see WSConstants#AES_256
     */
    public String getSymmetricEncAlgorithm() {
        return symEncAlgo;
    }

    /**
     * Builds the SOAP envelope with encrypted Body and adds encrypted key.
     * <p/>
     * This function performs several steps:
     * <p/>
     * <ul>
     * <li>    First step: set the encoding namespace in the SOAP:Envelope </li>
     * <li>    Second step: generate a symmetric key (session key) for
     * the selected symmetric encryption alogrithm, and set the cipher
     * into encryption mode.
     * </li>
     * <li> Third step: get the data to encrypt.
     * We always encrypt the complete first child element of
     * the SOAP Body element
     * </li>
     * <li>    Forth step: encrypt data, and set neccessary attributes in
     * <code>xenc:EncryptedData</code>
     * </li>
     * <li>    Fifth step: get the certificate that contains the public key for
     * the    public key algorithm that will encrypt the generated symmetric
     * (session) key. Up to now we support RSA 1-5 as public key
     * algorithm.
     * </li>
     * <li>    Sixth step: setup the <code>wsse:Security</code> header block </li>
     * </ul>
     *
     * @param doc    the SOAP envelope as <code>Document</code> with
     *               plaintext Body
     * @param crypto an instance of the Crypto API to handle keystore and
     *               Certificates
     * @return the SOAP envelope with encrypted Body as <code>Document
     *         </code>
     * @throws WSSecurityException
     */
    public Document build(Document doc, Crypto crypto) throws WSSecurityException {
        doDebug = log.isDebugEnabled();

        if (keyIdentifierType == WSConstants.EMBEDDED_KEYNAME ||
            keyIdentifierType == WSConstants.EMBED_SECURITY_TOKEN_REF) {
            return buildEmbedded(doc, crypto);
        }

        long t0 = 0, t1 = 0, t2 = 0, t3 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }
        if (doDebug) {
            log.debug("Beginning Encryption...");
        }

        /*
         * Second step: generate a symmetric key (session key) for
         * this alogrithm, and set the cipher into encryption mode.
         */
        // This variable is made a classs attribute :: SecretKey symmetricKey = null;
        this.encryptionKey = this.symmetricKey;
        if (encryptionKey == null) {
            KeyGenerator keyGen = getKeyGenerator();
            this.encryptionKey = keyGen.generateKey();
        }
        Vector encDataRefs = doEncryption(doc, this.encryptionKey);

        if (tlog.isDebugEnabled()) {
            t1 = System.currentTimeMillis();
        }

        /*
         * At this point data is encrypted with the symmetric key and can be
         * referenced via the above Id
         */

        /*
         * Fifth step: get the certificate that contains the public key for the
         * public key algorithm that will encrypt
         * the generated symmetric (session) key.
         * Up to now we support RSA 1-5 as public key algorithm
         */
        X509Certificate remoteCert = null;
        if (useThisCert != null) {
            remoteCert = useThisCert;
        } else {
            X509Certificate[] certs = crypto.getCertificates(user);
            if (certs == null || certs.length <= 0) {
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "invalidX509Data",
                        new Object[]{"for Encryption"});
            }
            remoteCert = certs[0];
        }
        String certUri = "EncCertId-" + remoteCert.hashCode();
        if (tlog.isDebugEnabled()) {
            t2 = System.currentTimeMillis();
        }
        Cipher cipher = WSSecurityUtil.getCipherInstance(keyEncAlgo);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, remoteCert);
        } catch (InvalidKeyException e) {
            throw new WSSecurityException(WSSecurityException.FAILED_ENC_DEC, null, null, e);
        }
        byte[] encKey = this.encryptionKey.getEncoded();
        if (doDebug) {
            log.debug("cipher blksize: "
                    + cipher.getBlockSize()
                    + ", symm key length: "
                    + encKey.length);
        }
        if (cipher.getBlockSize() < encKey.length) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "unsupportedKeyTransp",
                    new Object[]{"public key algorithm too weak to encrypt symmetric key"});
        }
        byte[] encryptedKey = null;
        try {
            encryptedKey = cipher.doFinal(encKey);
        } catch (IllegalStateException e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_ENC_DEC, null, null, e1);
        } catch (IllegalBlockSizeException e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_ENC_DEC, null, null, e1);
        } catch (BadPaddingException e1) {
            throw new WSSecurityException(WSSecurityException.FAILED_ENC_DEC, null, null, e1);
        }
        Text keyText =
                WSSecurityUtil.createBase64EncodedTextNode(doc, encryptedKey);

        /*
         * Now we need to setup the wsse:Security header block
         * 1) get (or create) the wsse:Security header block
         * 2) create the xenc:EncryptedKey element. This already includes
         *    the ExcrpytionMethod element with attributes that define
         *    the key transport encryption algorithm
         * 3) Generate ds:KeyInfo element, this wraps the wsse:SecurityTokenReference
         * 4) set up the SecurityTokenReference, either with KeyIdentifier or
         *    X509IssuerSerial. The SecTokenRef defines how to get to security
         *    token used to encrypt the session key (this security token usually
         *    contains a public key)
         * 5) Create the CipherValue element structure and insert the encrypted
         *    session key
         * 6) The last step sets up the reference list that pints to the encrypted
         *    data that was encrypted with this encrypted session key :-)
         */
        Element wsseSecurity = insertSecurityHeader(doc);
        Element xencEncryptedKey = createEnrcyptedKey(doc, keyEncAlgo);
        if (parentNode == null) {
            WSSecurityUtil.prependChildElement(doc,
                    wsseSecurity,
                    xencEncryptedKey,
                    true);
        } else {
            WSSecurityUtil.prependChildElement(doc,
                    parentNode,
                    xencEncryptedKey,
                    true);
        }
        SecurityTokenReference secToken = new SecurityTokenReference(wssConfig, doc);

        switch (keyIdentifierType) {
            case WSConstants.X509_KEY_IDENTIFIER:
                secToken.setKeyIdentifier(remoteCert);
                // build a key id class??
                break;

            case WSConstants.SKI_KEY_IDENTIFIER:
                secToken.setKeyIdentifierSKI(remoteCert, crypto);
                break;

            case WSConstants.ISSUER_SERIAL:
                secToken.setX509IssuerSerial(new XMLX509IssuerSerial(doc, remoteCert));
                break;

            case WSConstants.BST_DIRECT_REFERENCE:
                Reference ref = new Reference(wssConfig, doc);
                ref.setURI("#" + certUri);
                BinarySecurity bstToken = null;
                bstToken = new X509Security(wssConfig, doc);
                ((X509Security) bstToken).setX509Certificate(remoteCert);
                bstToken.setID(certUri);
                ref.setValueType(bstToken.getValueType());
                secToken.setReference(ref);
                WSSecurityUtil.prependChildElement(doc,
                        wsseSecurity,
                        bstToken.getElement(),
                        false);
                break;

            default :
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "unsupportedKeyId");
        }
        KeyInfo keyInfo = new KeyInfo(doc);
        keyInfo.addUnknownElement(secToken.getElement());
        WSSecurityUtil.appendChildElement(
                doc, xencEncryptedKey, keyInfo.getElement());

        Element xencCipherValue = createCipherValue(doc, xencEncryptedKey);
        xencCipherValue.appendChild(keyText);
        createDataRefList(doc, xencEncryptedKey, encDataRefs);
        log.debug("Encryption complete.");
        if (tlog.isDebugEnabled()) {
            t3 = System.currentTimeMillis();
            tlog.debug("EncryptBody: symm-enc= "
                    + (t1 - t0)
                    + ", cert= "
                    + (t2 - t1)
                    + ", key-encrypt= "
                    + (t3 - t2));
        }
        return doc;
    }

    private Vector doEncryption(Document doc, SecretKey encryptionKey)
        throws WSSecurityException {
        return doEncryption(doc, encryptionKey, null);
    }

    private Vector doEncryption(Document doc,
                                SecretKey encryptionKey,
                                KeyInfo keyInfo)
            throws WSSecurityException {
        /*
         * First step: set the encryption encoding namespace in the SOAP:Envelope
         */
        Element envelope = doc.getDocumentElement();
        envelope.setAttributeNS(WSConstants.XMLNS_NS,
                "xmlns:" + WSConstants.ENC_PREFIX,
                WSConstants.ENC_NS);

        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(envelope);

        XMLCipher xmlCipher = null;
        try {
            xmlCipher = XMLCipher.getInstance(symEncAlgo);
        } catch (XMLEncryptionException e3) {
            throw new WSSecurityException(WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e3);
        }

        // if no encryption parts set - use the default
        if (parts == null) {
            parts = new Vector();
            WSEncryptionPart encP =
                    new WSEncryptionPart(soapConstants.getBodyQName().getLocalPart(),
                            soapConstants.getEnvelopeURI(),
                            "Content");
            parts.add(encP);
        }

        Vector encDataRefs = new Vector();

        for (int part = 0; part < parts.size(); part++) {
            WSEncryptionPart encPart = (WSEncryptionPart) parts.get(part);
            String elemName = encPart.getName();
            String nmSpace = encPart.getNamespace();
            String modifier = encPart.getEncModifier();
            /*
             * Third step: get the data to encrypt.
             */
            Element body =
                    (Element) WSSecurityUtil.findElement(envelope,
                            elemName,
                            nmSpace);
            if (body == null) {
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "noEncElement",
                        new Object[]{"{" + nmSpace + "}" + elemName});
            }

            boolean content = modifier.equals("Content") ? true : false;
            String xencEncryptedDataId = "EncDataId-" + body.hashCode();

            /*
             * Forth step: encrypt data, and set neccessary attributes in
             * xenc:EncryptedData
             */
            try {
                xmlCipher.init(XMLCipher.ENCRYPT_MODE, encryptionKey);
                EncryptedData encData = xmlCipher.getEncryptedData();
                encData.setId(xencEncryptedDataId);
                encData.setKeyInfo(keyInfo);
                xmlCipher.doFinal(doc, body, content);
            } catch (Exception e2) {
                throw new WSSecurityException(WSSecurityException.FAILED_ENC_DEC, null, null, e2);
            }
            encDataRefs.add(new String("#" + xencEncryptedDataId));
        }
        return encDataRefs;
    }

    private Document buildEmbedded(Document doc, Crypto crypto)
            throws WSSecurityException {
        doDebug = log.isDebugEnabled();

        long t0 = 0, t1 = 0;
        if (tlog.isDebugEnabled()) {
            t0 = System.currentTimeMillis();
        }
        if (doDebug) {
            log.debug("Beginning Encryption embedded...");
        }

        if (embeddedKey == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "noKeySupplied");
        }

        /*
         * Second step: generate a symmetric key from the specified
         * key (password) for this alogrithm, and set the cipher into
         * encryption mode.
         */
        this.encryptionKey = this.symmetricKey;
        if (this.encryptionKey == null) {
            this.encryptionKey = WSSecurityUtil.prepareSecretKey(
                    symEncAlgo,
                    embeddedKey);
        }

        KeyInfo keyInfo = null;
        if (this.keyIdentifierType == WSConstants.EMBEDDED_KEYNAME) {
            keyInfo = new KeyInfo(doc);
            keyInfo.addKeyName(embeddedKeyName == null ? user : embeddedKeyName);
        } else if (this.keyIdentifierType == WSConstants.EMBED_SECURITY_TOKEN_REF) {
            /* This means that we want to embed a <wsse:SecurityTokenReference>
            * into keyInfo element.
            * If we need this functionality, this.secRef MUST be set before
            * calling the build(doc, crypto) method.
            * So if secRef is null then throw an exception.
            */
            if (this.securityTokenReference == null) {
                throw new WSSecurityException(
                        WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                        "You must set keyInfo element, if the keyIdentifier " +
                        "== EMBED_SECURITY_TOKEN_REF");
            } else {
                keyInfo = new KeyInfo(doc);
                Element tmpE = securityTokenReference.getElement();
                tmpE.setAttributeNS(WSConstants.XMLNS_NS,
                        "xmlns:" + tmpE.getPrefix(),
                        tmpE.getNamespaceURI());
                keyInfo.addUnknownElement(securityTokenReference.getElement());
            }
        }

        Vector encDataRefs = doEncryption(doc, this.encryptionKey, keyInfo);

        /*
         * At this point data is encrypted with the symmetric key and can be
         * referenced via the above Id
         */

        /*
         * Now we need to setup the wsse:Security header block
         * 1) get (or create) the wsse:Security header block
         * 2) The last step sets up the reference list that pints to the encrypted
         *    data that was encrypted with this encrypted session key :-)
         */
        Element wsseSecurity = insertSecurityHeader(doc);

        Element tmpE = doc.createElement("temp");
        Element refList = createDataRefList(doc, tmpE, encDataRefs);
        WSSecurityUtil.prependChildElement(doc, wsseSecurity, refList, true);

        if (tlog.isDebugEnabled()) {
            tlog.debug("EncryptBody embedded: symm-enc= " + (t1 - t0));
        }
        return doc;
    }

    private KeyGenerator getKeyGenerator() throws WSSecurityException {
        KeyGenerator keyGen = null;
        try {
            if (symEncAlgo.equalsIgnoreCase(WSConstants.TRIPLE_DES)) {
                keyGen = KeyGenerator.getInstance("DESede");
            } else if (symEncAlgo.equalsIgnoreCase(WSConstants.AES_128)) {
                keyGen = KeyGenerator.getInstance("2.16.840.1.101.3.4.1.2");
            } else if (symEncAlgo.equalsIgnoreCase(WSConstants.AES_192)) {
                keyGen = KeyGenerator.getInstance("2.16.840.1.101.3.4.1.22");
            } else if (symEncAlgo.equalsIgnoreCase(WSConstants.AES_256)) {
                keyGen = KeyGenerator.getInstance("2.16.840.1.101.3.4.1.42");
            } else {
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new WSSecurityException(WSSecurityException.UNSUPPORTED_ALGORITHM, null, null, e);
        }
        return keyGen;
    }

    /**
     * Create DOM subtree for <code>xenc:EncryptedKey</code>
     *
     * @param doc              the SOAP enevelope parent document
     * @param keyTransportAlgo specifies which alogrithm to use to encrypt the
     *                         symmetric key
     * @return an <code>xenc:EncryptedKey</code> element
     */
    public static Element createEnrcyptedKey(Document doc,
                                             String keyTransportAlgo) {
        Element encryptedKey =
                doc.createElementNS(WSConstants.ENC_NS,
                        WSConstants.ENC_PREFIX + ":EncryptedKey");

        WSSecurityUtil.setNamespace(encryptedKey,
                WSConstants.ENC_NS,
                WSConstants.ENC_PREFIX);
        Element encryptionMethod =
                doc.createElementNS(WSConstants.ENC_NS,
                        WSConstants.ENC_PREFIX + ":EncryptionMethod");
        encryptionMethod.setAttributeNS(null, "Algorithm", keyTransportAlgo);
        WSSecurityUtil.appendChildElement(doc, encryptedKey, encryptionMethod);
        return encryptedKey;
    }

    public static Element createCipherValue(Document doc,
                                            Element encryptedKey) {
        Element cipherData =
                doc.createElementNS(WSConstants.ENC_NS,
                        WSConstants.ENC_PREFIX + ":CipherData");
        Element cipherValue =
                doc.createElementNS(WSConstants.ENC_NS,
                        WSConstants.ENC_PREFIX + ":CipherValue");
        cipherData.appendChild(cipherValue);
        WSSecurityUtil.appendChildElement(doc, encryptedKey, cipherData);
        return cipherValue;
    }

    public static Element createDataRefList(Document doc,
                                            Element encryptedKey,
                                            Vector encDataRefs) {
        Element referenceList =
                doc.createElementNS(WSConstants.ENC_NS,
                        WSConstants.ENC_PREFIX + ":ReferenceList");
        for (int i = 0; i < encDataRefs.size(); i++) {
            String dataReferenceUri = (String) encDataRefs.get(i);
            Element dataReference =
                    doc.createElementNS(WSConstants.ENC_NS,
                            WSConstants.ENC_PREFIX + ":DataReference");
            dataReference.setAttributeNS(null, "URI", dataReferenceUri);
            referenceList.appendChild(dataReference);
        }
        WSSecurityUtil.appendChildElement(doc, encryptedKey, referenceList);
        return referenceList;
    }

    /**
     * Sets the parent node of the EncryptedKeyElement
     *
     * @param element
     */
    public void setParentNode(Element element) {
        parentNode = element;
    }

    /**
     * @return
     */
    public SecretKey getSymmetricKey() {
        return symmetricKey;
    }

    /**
     * Set the symmetric key to be used for encryption
     * @param key
     */
    public void setSymmetricKey(SecretKey key) {
        this.symmetricKey = key;
    }

    /**
     * Get the symmetric key used for encryption. This may be the same as
     * the symmetric key field.
     *
     * @return The symmetric key
     */
    public SecretKey getEncryptionKey() {
        return this.encryptionKey;
    }

    /**
     * @return
     */
    public SecurityTokenReference getSecurityTokenReference() {
        return securityTokenReference;
    }

    /**
     * @param reference
     */
    public void setSecurityTokenReference(SecurityTokenReference reference) {
        securityTokenReference = reference;
    }

}
