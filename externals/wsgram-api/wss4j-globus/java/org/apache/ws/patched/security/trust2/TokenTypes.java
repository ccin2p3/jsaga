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

package org.apache.ws.patched.security.trust2;



import java.net.URI;
import java.net.URISyntaxException;

import org.apache.ws.patched.security.WSConstants;
import org.apache.ws.patched.security.message.token.X509Security;


/**
 * @author ddelvecc
 *         <p/>
 *         A set of URI constants representing different token types defined by the WS-Security TC. These are typically used
 *         in the WS-Trust <TokenType> element.
 */
public abstract class TokenTypes {
    public static URI USERNAME;

    private static final String x509prefix = WSConstants.X509TOKEN_NS;

    public static URI X509;
    public static URI X509PKIPATH;
    public static URI PKCS7;

    static {
        try {
            USERNAME = new URI(WSConstants.USERNAMETOKEN_NS + "#" + WSConstants.USERNAME_TOKEN_LN);
            X509 = new URI(WSConstants.X509TOKEN_NS + "#" + X509Security.X509_V3);
            X509PKIPATH = new URI(x509prefix + "#X509PKIPathv1");
            PKCS7 = new URI(x509prefix + "#PKCS7");
        } catch (URISyntaxException e) {
        }
    }
}
