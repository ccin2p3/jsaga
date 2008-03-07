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

package org.apache.ws.patched.security.trust.verify;

import org.apache.ws.patched.security.trust.WSTrustException;
import org.w3c.dom.Document;


/**
 * @author Ruchith
 *         This is the interface for three verifiers used by the Trust engine
 */
public interface STVerifier {
    /**
     * Verification for the Trust engine
     *
     * @param doc  The request document
     * @param porp configuration information
     * @return Verified/NOT ?
     * @throws TrustException
     */
    public boolean verify(Document doc) throws WSTrustException;
}
