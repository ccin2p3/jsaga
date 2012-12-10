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

package org.glite.ce.commonj.authz;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.glite.ce.faults.AuthorizationFault;
import org.glite.ce.commonj.Constants;
import org.glite.security.SecurityInfo;
import org.glite.security.SecurityInfoContainer;
import org.glite.security.util.DN;
import org.glite.security.util.DNHandler;
import org.glite.security.util.axis.InitSecurityContext;
import org.glite.voms.FQAN;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;
import org.glite.voms.ac.ACValidator;

public class AuthorizationHandler extends BasicHandler {
    private static final Logger logger = Logger.getLogger(AuthorizationHandler.class.getName());
    private static final long serialVersionUID = 1202907233;
    
    public DirContext rootCtx;

    public AuthorizationHandler() {
        super();
    }

    public void init() {
        super.init();

        if(rootCtx == null) {
            Hashtable<String, String> env = new Hashtable<String, String>(0);
            env.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.naming.java.javaURLContextFactory");
            env.put(Context.URL_PKG_PREFIXES,"org.apache.naming");

            try {
                Context context = new InitialContext(env);
                context = (Context) context.lookup("java:comp/env");

                String confProvider = (String)context.lookup("configuration_provider");
                String confProviderURL = (String)context.lookup("configuration_provider_url");

                if(confProvider == null) {
                    logger.error("Missing configuration_provider parameter");
                    return;
                }

                if(confProviderURL == null) {
                    logger.error("Missing configuration_provider_url parameter");
                    return;
                }
                
                env.put(Context.INITIAL_CONTEXT_FACTORY, confProvider);
                env.put(Context.PROVIDER_URL, confProviderURL);
                
                rootCtx = (DirContext) new InitialDirContext(env);
            } catch(NamingException nEx) {
                logger.error(nEx.getMessage(), nEx);
                throw new RuntimeException("Cannot configure AuthorizationHandler");
            }
        }
    }
    
    protected void check(javax.xml.rpc.handler.MessageContext msgContext) throws AuthorizationException {    	
    	if(rootCtx==null) {
    		throw new AuthorizationException("Cannot find configuration structure");
    	}
    	
        SecurityInfo secInfo = SecurityInfoContainer.getSecurityInfo();

        String tmpDN = secInfo.getClientName();

        if(tmpDN == null || tmpDN.equals("")) {
            throw new AuthorizationException("Cannot retrieve credentials");
        }

        DN objDN = DNHandler.getDN(tmpDN);
        String dnX500 = objDN.getX500();
        String dnRFC2253 = objDN.getRFC2253();

        org.glite.security.util.X500Principal tmpp = new org.glite.security.util.X500Principal();
        tmpp.setName(objDN);

        Subject subject = new Subject();            
        subject.getPrincipals().add(tmpp);
        subject.getPrincipals().add(new javax.security.auth.x500.X500Principal(dnRFC2253));

        msgContext.setProperty(Constants.USERDN_X500_LABEL, dnX500);
        msgContext.setProperty(Constants.USERDN_RFC2253_LABEL, dnRFC2253);

        X509Certificate[] userCertChain = secInfo.getClientCertChain();
        msgContext.setProperty(Constants.USER_CERTCHAIN_LABEL, userCertChain);
        subject.getPublicCredentials().add(userCertChain);

        ACValidator acValidator = AuthZContextListener.getACValidator();
        VOMSValidator mainValidator = new VOMSValidator(userCertChain, acValidator);
        mainValidator.validate();

        List<VOMSAttribute> vomsList = (List<VOMSAttribute>)mainValidator.getVOMSAttributes();
        msgContext.setProperty(Constants.USER_VOMSATTRS_LABEL, vomsList);

        HashSet<String> voSet = new HashSet<String>(0);
        if(vomsList != null) {
            for(VOMSAttribute vomsAttr : vomsList) {
                voSet.add(vomsAttr.getVO());
            }
        }

        msgContext.setProperty(Constants.USER_VO_LABEL, voSet);

        QName operation = this.getOperation(msgContext);
        ServiceAuthorizationChain chain = null;

        if(operation != null) {
            logger.debug("Checking operation " + operation);

            try {
            	BasicAttributes queryAttrs = new BasicAttributes("category", "authzchain");
            	NamingEnumeration<SearchResult> queryResult = rootCtx.search("", queryAttrs);

            	if(queryResult.hasMoreElements()) {
            	    chain = (ServiceAuthorizationChain) queryResult.next().getObject();
            	}
            } catch (Exception ex) {
            	logger.error(ex.getMessage(), ex);
            }

            if(chain == null) {
                throw new AuthorizationException("Cannot retrieve authorization chain");
            }
            
            StringBuffer reqLogInfo = new StringBuffer("request for operation=");
            reqLogInfo.append(operation);
            reqLogInfo.append("; ");
            
            HttpServletRequest httpReq = (HttpServletRequest)msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            if(httpReq != null) {
                reqLogInfo.append("REMOTE_REQUEST_ADDRESS=");
                reqLogInfo.append(httpReq.getRemoteAddr());
                reqLogInfo.append("; ");
                
                msgContext.setProperty(Constants.REMOTE_REQUEST_ADDRESS, httpReq.getRemoteAddr());
            }

            reqLogInfo.append("USER_DN=");
            reqLogInfo.append(dnRFC2253);
            reqLogInfo.append("; ");
            
            if(vomsList != null && vomsList.size() > 0) {
                reqLogInfo.append("USER_FQAN={ ");

                for(VOMSAttribute vomsAttr : vomsList) {
                    List<FQAN> list = vomsAttr.getListOfFQAN();
                    if(list != null) {
                        for(FQAN fqan : list) {
                            reqLogInfo.append(fqan);
                            reqLogInfo.append("; ");                            
                        }
                    }
                }

                reqLogInfo.append("}; ");

                msgContext.setProperty(Constants.REMOTE_REQUEST_ADDRESS, httpReq.getRemoteAddr());
            }
            
            if(chain.isPermitted(subject, msgContext, operation)) {
                reqLogInfo.append(" AUTHORIZED!");
            	logger.info(reqLogInfo);            	
            	
            	return;
            }

            reqLogInfo.append(" NOT AUTHORIZED!");
            logger.info(reqLogInfo);
        }

        throw new AuthorizationException("User " + dnRFC2253 + " not authorized for operation " + operation);
    }

    protected QName getOperation(javax.xml.rpc.handler.MessageContext context) {
        OperationDesc operation = ((MessageContext) context).getOperation();
        
        if(operation == null) {
            return new QName("unknown");
        }
        return operation.getElementQName();
    }

    protected AxisFault getAuthorizationFault(String message) {
        AuthorizationFault fault = new AuthorizationFault();
        fault.setMethodName("invoke");
        fault.setDescription(message);
        fault.setErrorCode("0");
        fault.setFaultCause(message);
        fault.setTimestamp(Calendar.getInstance());
        return fault;
    }

    public void invoke(MessageContext msgContext) throws AxisFault {    	
    	InitSecurityContext.init();
    	
        try {
            this.check(msgContext);
        } catch (AuthorizationException authEx) {
            logger.error(authEx.getMessage());
            throw this.getAuthorizationFault(authEx.getMessage());
        }
    }
}
