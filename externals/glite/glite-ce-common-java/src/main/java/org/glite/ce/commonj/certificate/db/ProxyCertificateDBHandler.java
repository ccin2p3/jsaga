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
 * Authorization handler for Axis container
 * 
 * Authors: Luigi Zangrando, <luigi.zangrando@pd.infn.it>
 *
 * Version info: $Id: ProxyCertificateDBHandler.java,v 1.8 2010/01/05 11:01:30 pandreet Exp $
 */

package org.glite.ce.commonj.certificate.db;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.log4j.Logger;
import org.glite.ce.commonj.Constants;
import org.glite.ce.commonj.certificate.ProxyCertificate;
import org.glite.ce.commonj.certificate.ProxyCertificateStorageInterface;
import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;
import org.glite.ce.commonj.utils.CEUtils;
import org.glite.voms.VOMSAttribute;

public class ProxyCertificateDBHandler extends BasicHandler {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ProxyCertificateDBHandler.class.getName());

    /**
     * The default constructor of the class.
     * It just calls the BasicHandler constructor.
     */
    public ProxyCertificateDBHandler() {
        super();
    }

    /**
     * This method is invoked by the axis engine during the processing of the
     * request. It stores into the authN database the user proxy certificate
     * and severl other information.
     * 
     * @param msgContext
     *            is the message context for the current request.
     * @throws AxisFault
     *             if an error occurs during the process.
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        if(msgContext == null) {
            throw new AxisFault("empty msgContext found");
        }
        
        String userDN = (String)msgContext.getProperty(Constants.USERDN_RFC2253_LABEL);        
        if(userDN == null) {
            logger.error("USERDN_RFC2253_LABEL not defined in msgContext");
            throw new AxisFault("USERDN_RFC2253_LABEL not defined in msgContext");
        }
        
        userDN = normalize(userDN);
        
        
        HashSet voSet = (HashSet)msgContext.getProperty(Constants.USER_VO_LABEL);
        if(voSet == null || voSet.size() == 0) {
            logger.error("USER_VO_LABEL not defined in msgContext");
            throw new AxisFault("USER_VO_LABEL not defined in msgContext");
        }

        String userVO = (String) voSet.iterator().next().toString();
              
        X509Certificate[] certChain = (X509Certificate[])msgContext.getProperty(Constants.USER_CERTCHAIN_LABEL);
        if(certChain == null || certChain.length == 0) {
            logger.error("USER_CERTCHAIN_LABEL not defined in msgContext");
            throw new AxisFault("USER_CERTCHAIN_LABEL not defined in msgContext");
        }

        String userCert = null;
        try {
            userCert = CEUtils.getPEM(certChain);
        } catch (CertificateEncodingException e) {
            logger.error("problem with the proxy certificate: " + e.getMessage());
            throw new AxisFault("problem with the proxy certificate: " + e.getMessage());
        }
        
        if(userCert == null || userCert.equals("")) {
            logger.error("the user certificate is empty");
            throw new AxisFault("the user certificate is empty");
        }
        
        String fqan = null;
        List vomsList = (List) msgContext.getProperty(Constants.USER_VOMSATTRS_LABEL);
        if(vomsList == null) {
            logger.error("USER_VOMSATTRS_LABEL not defined in msgContext");
            throw new AxisFault("USER_VOMSATTRS_LABEL not defined in msgContext");
        }

        Iterator item = vomsList.iterator();
        VOMSAttribute vomsAttr = null;
        
        if(item != null) {
            while (item.hasNext()) {
                vomsAttr = (VOMSAttribute) item.next();
 
                if (userVO.equals(vomsAttr.getVO())) {
                    List fqanList = vomsAttr.getListOfFQAN();
                    
                    if(fqanList.size() > 0) {
                        fqan = fqanList.iterator().next().toString();
                        fqan = normalize(fqan);
                    }
                
                    break;
                }
            }
        }

        Boolean isAdmin = (Boolean) msgContext.getProperty(Constants.IS_ADMIN);
        boolean isAdministrator = false;

        if(isAdmin != null) {
            isAdministrator = isAdmin.booleanValue();
        }

        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(certChain[0].getNotBefore().getTime());
        
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.setTimeInMillis(certChain[0].getNotAfter().getTime());

        try {
            ProxyCertificateStorageInterface proxyStorage = ProxyCertificateDBManager.getInstance();
            ProxyCertificate authNProxyCertificate = proxyStorage.getProxyCertificate(ProxyCertificateType.AUTHENTICATION.getName(), userDN, fqan);

            if(authNProxyCertificate == null || authNProxyCertificate.getExpirationTime().before(expirationTime)) {                
                authNProxyCertificate = new ProxyCertificate(ProxyCertificateType.AUTHENTICATION.getName(), userDN, fqan, userVO, userCert, startTime, expirationTime, ProxyCertificateType.AUTHENTICATION, isAdministrator);
                authNProxyCertificate.setDescription(CEUtils.getProxyInfo(certChain, vomsList));

                proxyStorage.setProxyCertificate(authNProxyCertificate);
            } else {
                if(authNProxyCertificate.isAdministrator() != isAdministrator) {
                    authNProxyCertificate.setAdministrator(isAdministrator);

                    proxyStorage.setProxyCertificate(authNProxyCertificate);
                }
            }

            authNProxyCertificate.setAdministrator(isAdministrator);

            msgContext.setProperty(ProxyCertificateType.AUTHENTICATION.getName(), authNProxyCertificate);
            logger.info("setUserAuthNCertificate userId=" + userDN + fqan + " userDN=" + userDN + " userVO=" + userVO + " isAdmin=" + isAdministrator);
        } catch (Throwable e) {
            logger.error(e.getMessage());
            throw new AxisFault(e.getMessage());
        }
    }
  
    protected String normalize(String s) {
        if (s != null) {
            return s.replace('=', '_').replace(' ', '_').replace('/', '_').replace(',', '_').replace('@', '_');
        }
        return null;
    }
}
