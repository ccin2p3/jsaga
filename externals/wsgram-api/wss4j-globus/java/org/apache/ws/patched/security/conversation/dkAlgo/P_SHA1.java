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

package org.apache.ws.patched.security.conversation.dkAlgo;

/**
 *
 <pre>
 P_SHA-1 DEFINITION
 ==================
 <b>P_SHA-1(secret, seed)</b> =
 HMAC_SHA-1(secret, A(1) + seed) +
 HMAC_SHA-1(secret, A(2) + seed) +
 HMAC_SHA-1(secret, A(3) + seed) + ...
 <i>Where + indicates concatenation.</i>
 <br>
 A() is defined as:
 A(0) = seed
 A(i) = HMAC_SHA-1(secret, A(i-1))
 <br>
 <i>Source : RFC 2246 - The TLS Protocol Version 1.0
 Section 5. HMAC and the pseudorandom function</i>
 </pre>
 *
 * @author Ruchith Fernando
 * @version 1.0
 */


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.ws.patched.security.conversation.ConversationException;

public class P_SHA1
        implements DerivationAlgorithm {

    private byte[] secret;
    private String seed;

    public byte[] createKey(byte[] secret, String labelAndNonce, int offset,
                            long length) throws ConversationException {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            byte[] key = new String(P_hash(secret, labelAndNonce.getBytes(), mac,
                    (offset + (int) length))).substring(offset,
                            (int) length).getBytes();

            return key;
        } catch (Exception ex) {
            throw new ConversationException("Key Derivation : P_SHA-1: " +
                    ex.getMessage());
        }
    }

    /**
     * Stolen from WSUsernameToken  :-)
     *
     * @param secret
     * @param seed
     * @param mac
     * @param required
     * @return
     * @throws java.lang.Exception
     */
    private static byte[] P_hash(byte[] secret, byte[] seed, Mac mac,
                                 int required) throws Exception {
        byte[] out = new byte[required];
        int offset = 0, tocpy;
        byte[] A, tmp;
        A = seed;
        while (required > 0) {
            SecretKeySpec key = new SecretKeySpec(secret, "HMACSHA1");
            mac.init(key);
            mac.update(A);
            A = mac.doFinal();
            mac.reset();
            mac.init(key);
            mac.update(A);
            mac.update(seed);
            tmp = mac.doFinal();
            tocpy = min(required, tmp.length);
            System.arraycopy(tmp, 0, out, offset, tocpy);
            offset += tocpy;
            required -= tocpy;
        }
        return out;
    }

    private static int min(int a, int b) {
        return (a > b) ? b : a;
    }

}