/*
 * Copyright (c) Members of the EGEE Collaboration. 2004. 
 * See http://www.eu-egee.org/partners/ for details on the copyright
 * holders.  
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 */

package org.glite.ce.commonj.argus.tomcat;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

import javax.net.ssl.X509TrustManager;
import javax.net.ssl.X509KeyManager;

public class TMSSLServerSocketFactory extends
        org.glite.security.trustmanager.tomcat.TMSSLServerSocketFactory {
        
    private static boolean noManagers = true;
        
    public ServerSocket createSocket(int port, int backlog, InetAddress ifAddress) 
        throws IOException, InstantiationException {
        ServerSocket tmpSocket = super.createSocket(port, backlog, ifAddress);
        storeManagers();
        return tmpSocket;
    }
    
    public ServerSocket createSocket(int port, int backlog)
        throws IOException, InstantiationException {
        ServerSocket tmpSocket = super.createSocket(port, backlog);
        storeManagers();
        return tmpSocket;
    }
    
    public ServerSocket createSocket(int port)
        throws IOException, InstantiationException {
        ServerSocket tmpSocket = super.createSocket(port);
        storeManagers();
        return tmpSocket;
    }
    
    private void storeManagers(){
        if(noManagers){
            TMSSLBean defaultBean = TMSSLBean.getDefaultBean();
            defaultBean.setKeyManager(contextWrapper.getKeyManager());
            if (contextWrapper.trustManager != null) {
                defaultBean.setTrustManager(contextWrapper.trustManager);
            }else{
                defaultBean.setTrustManager(contextWrapper.m_trustmanager);
            }
            noManagers = false;
        }
    }
}
