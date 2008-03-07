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
package org.apache.ws.patched.security.trust;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.trust.message.Info.RequestInfo;
import org.apache.ws.patched.security.trust.message.token.RequestType;
import org.apache.ws.patched.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author Malinda Kaushalye
 *         To be completed later
 */
public class RequestResolver {
    static Log log = LogFactory.getLog(STSManager.class.getName());
    Document doc;
    RequestInfo reqInfo = new RequestInfo();

    /**
     * 
     */
    public RequestResolver(Document doc) {
        this.doc = doc;
    }

    public RequestInfo resolve() throws WSSecurityException {
        //get the request type and base
        
        Element elemRequestType = (Element) WSSecurityUtil.findElement(doc, RequestType.TOKEN.getLocalPart(), RequestType.TOKEN.getNamespaceURI());
        //Element elemBase=(Element)WSSecurityUtil.findElement(doc,BaseToken.TOKEN.getLocalPart(),BaseToken.TOKEN.getNamespaceURI());
    
        //set request type    
        if (elemRequestType != null) {
            log.debug("Resolving request type");
            RequestType requestType = new RequestType(elemRequestType);
            String temp = requestType.getValue();
            this.reqInfo.setRequestType(requestType.getValue());
            log.debug("Resolving request type complete");
        }
        
//        if(elemBase!=null){
//            log.debug("Resolving base ");
//            BaseToken baseToken=new BaseToken(elemBase);    
//                //a more logic
//            this.reqInfo.setBaseType(RequestInfo.X509);//hcoded
//            log.debug("Resolving base complete");
//            
//        }    
        
        return this.reqInfo;

    }

}
