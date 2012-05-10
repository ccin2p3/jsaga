/*
 * Copyright 1999-2006 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.globus.myproxy;

/**
 * Holds the parameters for the <code>destroy</code> operation.
 */
public class DestroyParams
    extends Params {

    private String credentialName;

    public DestroyParams() {
        super(MyProxy.DESTROY_PROXY);
    }
    
    public DestroyParams(String username, String passphrase) {
        super(MyProxy.DESTROY_PROXY, username, passphrase);
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getCredentialName() {
        return this.credentialName;
    }

    protected String makeRequest(boolean includePassword) {
        StringBuffer buf = new StringBuffer();
        buf.append(super.makeRequest(includePassword));
        add(buf, CRED_NAME, credentialName);
        return buf.toString();
    }
    
}
