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

package org.apache.ws.patched.security;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.EncryptionConstants;

/**
 * Constants in WS-Security spec.
 */
public class WSConstants {
    // the following compliance mode values must have increasing values as new
    // modes are added; a later spec should have a value > value of an an earlier spec. 
    public static final int OASIS_2002_07 = 1;
    public static final int OASIS_2002_12 = 2;
    public static final int OASIS_2003_06 = 3;
    public static final int OASIS_1_0 = 4;

    /**
     * Set the specification compliance mode. This affects namespaces as well
     * as how certain items are constructed in security headers.
     * <p/>
     * Currently this can only be set at compile time. The valid values are:
     * <ul>
     * <li> {@link #OASIS_2002_07} </li>
     * <li> {@link #OASIS_2002_12} </li>
     * <li> {@link #OASIS_2003_06} </li>
     * <li> {@link #OASIS_1_0} OASIS WS-Security v1.0 as released on March 2004. This is the default and recommended setting</li>
     * </ul>
     * <p/>
     * Using {@link #OASIS_2002} enhances chances of interoperability with other
     * WSS implementations that do not fully adhere to the OASIS v1.0 March 2004
     * specs yet.
     *
     * @param specs instructs WSS4J on which standard to follow
     */
    public static final int COMPLIANCE_MODE = OASIS_1_0;

    public static final String WSSE_NS_OASIS_2002_07 = "http://schemas.xmlsoap.org/ws/2002/07/secext";
    public static final String WSSE_NS_OASIS_2002_12 = "http://schemas.xmlsoap.org/ws/2002/12/secext";
    public static final String WSSE_NS_OASIS_2003_06 = "http://schemas.xmlsoap.org/ws/2003/06/secext";
    public static final String WSSE_NS_OASIS_1_0 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static String WSSE_NS = WSSE_NS_OASIS_1_0;
    public static final String[] WSSE_NS_ARRAY =
            new String[]{WSSE_NS_OASIS_1_0,
                         WSSE_NS_OASIS_2003_06,
                         WSSE_NS_OASIS_2002_12,
                         WSSE_NS_OASIS_2002_07};
    public static final String USERNAMETOKEN_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0";
    public static final String SOAPMESSAGE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0";
    public static final String X509TOKEN_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0";
    public static final String WSSE_PREFIX = "wsse";
    public static final String WSSE_LN = "Security";
    public static final String WSU_NS_OASIS_2002_07 = "http://schemas.xmlsoap.org/ws/2002/07/utility";
    public static final String WSU_NS_OASIS_2002_12 = "http://schemas.xmlsoap.org/ws/2002/12/utility";
    public static final String WSU_NS_OASIS_2003_06 = "http://schemas.xmlsoap.org/ws/2003/06/utility";
    public static final String WSU_NS_OASIS_1_0 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public static String WSU_NS = WSU_NS_OASIS_1_0;
    public static final String[] WSU_NS_ARRAY =
            new String[]{WSU_NS_OASIS_1_0,
                         WSU_NS_OASIS_2003_06,
                         WSU_NS_OASIS_2002_12,
                         WSU_NS_OASIS_2002_07};
    public static final String WSU_PREFIX = "wsu";
    public static final String SIG_NS = "http://www.w3.org/2000/09/xmldsig#";
    public static final String SIG_PREFIX = "ds";
    public static final String SIG_LN = "Signature";
    public static final String ENC_NS = "http://www.w3.org/2001/04/xmlenc#";
    public static final String ENC_PREFIX = "xenc";
    public static final String ENC_KEY_LN = "EncryptedKey";
    public static final String REF_LIST_LN = "ReferenceList";
//    public static final String SOAP_SEC_NS = "http://schemas.xmlsoap.org/soap/security/2000-12";
    public static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    public static final String USERNAME_TOKEN_LN = "UsernameToken";
    public static final String BINARY_TOKEN_LN = "BinarySecurityToken";
    public static final String TIMESTAMP_TOKEN_LN = "Timestamp";
    public static final String USERNAME_LN = "Username";
    public static final String PASSWORD_LN = "Password";
    public static final String PASSWORD_TYPE_ATTR = "Type";
    public static final String NONCE_LN = "Nonce";
    public static final String CREATED_LN = "Created";
    public static final String EXPIRES_LN = "Expires";

    public static final String SAML_NS = "urn:oasis:names:tc:SAML:1.0:assertion";
    public static final String SAMLP_NS = "urn:oasis:names:tc:SAML:1.0:protocol";
    public static final String ASSERTION_LN = "Assertion";
    public static final String WSS_SAML_NS = "http://docs.oasis-open.org/wss/2004/XX/oasis-2004XX-wss-saml-token-profile-1.0#";
    public static final String WSS_SAML_ASSERTION = "SAMLAssertion-1.1";

    //
    // SOAP-ENV Namespaces
    //
    public static final String URI_SOAP11_ENV =
            "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String URI_SOAP12_ENV =
            "http://www.w3.org/2003/05/soap-envelope";

    public static final String[] URIS_SOAP_ENV = {
        URI_SOAP11_ENV,
        URI_SOAP12_ENV,
    };

    // Misc SOAP Namespaces / URIs
    public static final String URI_SOAP11_NEXT_ACTOR =
            "http://schemas.xmlsoap.org/soap/actor/next";
    public static final String URI_SOAP12_NEXT_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/next";
    public static final String URI_SOAP12_NONE_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/none";
    public static final String URI_SOAP12_ULTIMATE_ROLE =
            "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";

    public static final String ELEM_ENVELOPE = "Envelope";
    public static final String ELEM_HEADER = "Header";
    public static final String ELEM_BODY = "Body";

    public static final String ATTR_MUST_UNDERSTAND = "mustUnderstand";
    public static final String ATTR_ACTOR = "actor";
    public static final String ATTR_ROLE = "role";

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSSAddUsernameToken#build(Document, String, String) UserNameToken}
     * method to use a password digest to send the password information
     * <p/>
     * This is a required method as defined by WS Specification, Username token profile.
     */
    public static final String PW_DIGEST = "PasswordDigest";
    public static final String PASSWORD_DIGEST = USERNAMETOKEN_NS + "#PasswordDigest";

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSSAddUsernameToken#build(Document, String, String) UserNameToken}
     * method to send the password in clear
     * <p/>
     * This is a required method as defined by WS Specification, Username token profile.
     */
    public static final String PW_TEXT = "PasswordText";

    public static final String PASSWORD_TEXT = USERNAMETOKEN_NS + "#PasswordText";

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to encrypt the symmetric data encryption key with the RSA algoritm.
     * <p/>
     * This is a required method as defined by XML encryption.
     */
    public static final String KEYTRANSPORT_RSA15 = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to encrypt the symmetric data encryption key with the RSA algoritm.
     * <p/>
     * This is a required method as defined by XML encryption.
     * <p/>
     * NOTE: This algorithm is not yet supported by WSS4J
     */
    public static final String KEYTRANSPORT_RSAOEP = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to use triple DES as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a required method as defined by XML encryption.
     */
    public static final String TRIPLE_DES = EncryptionConstants.ALGO_ID_BLOCKCIPHER_TRIPLEDES;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to use AES with 128 bit key as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a required method as defined by XML encryption.
     */
    public static final String AES_128 = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to use AES with 256 bit key as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a required method as defined by XML encryption.
     */
    public static final String AES_256 = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to use AES with 192 bit key as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a optional method as defined by XML encryption.
     */
    public static final String AES_192 = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSSignEnvelope#build(Document, Crypto) signature}
     * method to use DSA with SHA1 (DSS) to sign data.
     * <p/>
     * This is a required method as defined by XML signature.
     */
    public static final String DSA = XMLSignature.ALGO_ID_SIGNATURE_DSA;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSSignEnvelope#build(Document, Crypto) signature}
     * method to use RSA with SHA to sign data.
     * <p/>
     * This is a recommended method as defined by XML signature.
     */
    public static final String RSA = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;

    public static final String C14N_OMIT_COMMENTS = Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    public static final String C14N_WITH_COMMENTS = Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
    public static final String C14N_EXCL_OMIT_COMMENTS = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
    public static final String C14N_EXCL_WITH_COMMENTS = Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSSignEnvelope#build(Document, Crypto) signing}
     * method to send the signing certificate as a
     * <code>BinarySecurityToken</code>.
     * <p/>
     * The signing method takes the signing certificate, converts it to a
     * <code>BinarySecurityToken</code>, puts it in the security header,
     * and inserts a <code>Reference</code> to the binary security token
     * into the <code>wsse:SecurityReferenceToken</code>. Thus the whole
     * signing certificate is transfered to the receiver.
     * The X509 profile recommends to use {@link #ISSUER_SERIAL} instead
     * of sending the whole certificate.
     * <p/>
     * Please refer to WS Security specification X509 profile, chapter 3.3.2
     * and to WS Security specification, chapter 7.2
     * <p/>
     * Note: only local refernces to BinarySecurityToken are supported
     */
    public static final int BST_DIRECT_REFERENCE = 1;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSSignEnvelope#build(Document, Crypto) signing}
     * or the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to send the issuer name and the serial number of a
     * certificate to the receiver.
     * <p/>
     * In contrast to {@link #BST_DIRECT_REFERENCE} only the issuer name
     * and the serial number of the signiung certificate are sent to the
     * receiver. This reduces the amount of data being sent. The ecnryption
     * method uses the private key associated with this certificate to encrypt
     * the symmetric key used to encrypt data.
     * <p/>
     * Please refer to WS Security specification X509 profile, chapter 3.3.3
     */
    public static final int ISSUER_SERIAL = 2;

    /**
     * Sets the {@link org.apache.ws.patched.security.message.WSEncryptBody#build(Document, Crypto) encryption}
     * method to send the certificate used to encrypt the symmetric key.
     * <p/>
     * The encryption method uses the private key associated with this certificate
     * to encrypr the symmetric key used to encrypt data. The certificate is
     * converted into a <code>KeyIdentfier</code> token and sent to the receiver.
     * Thus the complete certificate data is transfered to receiver.
     * The X509 profile recommends to use {@link #ISSUER_SERIAL} instead
     * of sending the whole certificate.
     * <p/>
     * <p/>
     * Please refer to WS Security specification X509 profile, chapter 7.3
     */
    public static final int X509_KEY_IDENTIFIER = 3;
    /**
     * Sets the
     * {@link org.apache.ws.patched.security.message.WSSignEnvelope#build(Document, Crypto)
     * signing}
     * method to send a <code>SubjectKeyIdentifier</code> to identify
     * the signing certificate.
     * <p/>
     * Refer to WS Security specification X509 profile, chapter 3.3.1
     * This identification token is not yet fully tested by WSS4J. The
     * WsDoAllSender does not include the X.509 certificate as
     * <code>BinarySecurityToken</code> in the request message.
     */
    public static final int SKI_KEY_IDENTIFIER = 4;

    /**
     * Embeds a keyinfo/key name into the EncryptedData element.
     * <p/>
     * Refer to WS Security specification X509 profile
     */
    public static final int EMBEDDED_KEYNAME = 5;
    /**
     * Embeds a keyinfo/wsse:SecurityTokenReference into EncryptedData element.
     */
    public static final int EMBED_SECURITY_TOKEN_REF = 6;
    
    /**
     * <code>UT_SIGNING</code> is used interally only to set a specific Signature
     * behaviour.
     * 
     * The signing token is constructed from values in the UsernameToken according
     * to WS-Trust specification.
     */
    public static final int UT_SIGNING = 7;

    public static final int NO_SECURITY = 0;
    public static final int UT = 0x1; // perform UsernameToken
    public static final int SIGN = 0x2; // Perform Signature
    public static final int ENCR = 0x4; // Perform Encryption

    /*
     * Attention: the signed/Unsigned types identify if WSS4J uses
     * the SAML token for signature, signature key or not. It does
     * not mean if the token contains an enveloped signature.
     */
    public static final int ST_UNSIGNED = 0x8; // perform SAMLToken unsigned
    public static final int ST_SIGNED = 0x10; // perform SAMLToken signed

    public static final int TS = 0x20; // insert Timestamp
    public static final int UT_SIGN = 0x40; // perform sinagture with UT secrect key

    public static final int NO_SERIALIZE = 0x100;
    public static final int SERIALIZE = 0x200;

    /**
     * Length of UsernameToken derived key used by .NET WSE to sign a message.
     */
    public static final int WSE_DERIVED_KEY_LEN = 16;
    public static final String LABEL_FOR_DERIVED_KEY = "WS-Security";
    
    static {
        setComplianceMode();
    }

    /**
     * init various constants to the chosen compliance mode
     */
    private static void setComplianceMode() {
        switch (COMPLIANCE_MODE) {
            case OASIS_1_0:
                WSSE_NS = WSSE_NS_OASIS_1_0;
                WSU_NS = WSU_NS_OASIS_1_0;
                break;
            case OASIS_2003_06:
                WSSE_NS = WSSE_NS_OASIS_2003_06;
                WSU_NS = WSU_NS_OASIS_2003_06;
                break;
            case OASIS_2002_12:
                WSSE_NS = WSSE_NS_OASIS_2002_12;
                WSU_NS = WSU_NS_OASIS_2002_12;
                break;
            case OASIS_2002_07:
                WSSE_NS = WSSE_NS_OASIS_2002_07;
                WSU_NS = WSU_NS_OASIS_2002_07;
                break;
            default:
                WSSE_NS = WSSE_NS_OASIS_1_0;
                WSU_NS = WSU_NS_OASIS_1_0;
        }
    }
}

