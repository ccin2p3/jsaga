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

package org.apache.ws.patched.security.message.token;

import org.apache.ws.patched.security.WSConstants;
import org.apache.ws.patched.security.WSSConfig;
import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.components.crypto.Crypto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * X509 Security Token.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class X509Security extends BinarySecurity {
    private String type;
    public static final String X509_V3 = "X509v3";

    /*
     * Stores the associated X.509 Certificate. This saves numerous
     * crypto loadCertificate operations
     */
    private X509Certificate cachedCert = null;

    /**
     * This constructor creates a new X509 certificate object and initializes
     * it from the data containe in the element.
     *
     * @param wssConfig Configuration options for processing and building the <code>wsse:Security</code> header
     * @param elem      the element containing the X509 certificate data
     * @throws WSSecurityException
     */
    public X509Security(WSSConfig wssConfig, Element elem) throws WSSecurityException {
        super(wssConfig, elem);
        if (wssConfig.isBSTValuesPrefixed()) {
            type = WSConstants.WSSE_PREFIX + ":" + X509_V3;
        } else {
            type = WSConstants.X509TOKEN_NS + "#" + X509_V3;
        }
        if (!getValueType().endsWith(X509_V3)) {
            throw new WSSecurityException(WSSecurityException.INVALID_SECURITY_TOKEN, "invalidValueType", new Object[]{type, getValueType()});
        }
    }

    /**
     * This constructor creates a new X509 certificate element.
     *
     * @param doc
     */
    public X509Security(WSSConfig wssConfig, Document doc) {
        super(wssConfig, doc);
        if (wssConfig.isBSTValuesPrefixed()) {
            type = WSConstants.WSSE_PREFIX + ":" + X509_V3;
        } else {
            type = WSConstants.X509TOKEN_NS + "#" + X509_V3;
        }
        setValueType(type);
    }

    /**
     * Gets the X509Certificate certificate.
     * <p/>
     *
     * @return the X509 certificate converted from the base 64 encoded
     *         element data
     * @throws WSSecurityException
     */
    public X509Certificate getX509Certificate(Crypto crypto) throws WSSecurityException {
        if (cachedCert != null) {
            return cachedCert;
        }
        byte[] data = getToken();
        if (data == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "invalidCertData",
                    new Object[]{new Integer(0)});
        }
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        cachedCert = crypto.loadCertificate(in);
        return cachedCert;
    }

    /**
     * Sets the X509Certificate.
     * This functions takes the X509 certificate, gets the data from it as
     * encoded bytes, and sets the data as base 64 encoded data in the text
     * node of the element
     *
     * @param cert the X509 certificate to store in the element
     * @throws WSSecurityException
     */
    public void setX509Certificate(X509Certificate cert)
            throws WSSecurityException {
        if (cert == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                    "noCert");
        }
        cachedCert = cert;
        try {
            setToken(cert.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new WSSecurityException(WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                    "encodeError");
        }
    }

    public static String getType(WSSConfig wssConfig) {
        if (wssConfig.isBSTValuesPrefixed()) {
            return WSConstants.WSSE_PREFIX + ":" + X509_V3;
        } else {
            return WSConstants.X509TOKEN_NS + "#" + X509_V3;
        }
    }
}
