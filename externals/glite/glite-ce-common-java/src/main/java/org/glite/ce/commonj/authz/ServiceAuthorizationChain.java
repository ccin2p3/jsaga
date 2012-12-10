/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 *
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 */

package org.glite.ce.commonj.authz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.log4j.Logger;
import org.glite.ce.commonj.configuration.CEConfigResource;

public class ServiceAuthorizationChain implements CEConfigResource {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ServiceAuthorizationChain.class.getName());

    private String id;
    private List<ServiceInterceptor> interceptorList = new CopyOnWriteArrayList<ServiceInterceptor>();

    public synchronized void initialize(ChainConfig config, String name, String id) throws InitializeException {
        this.id = id;

        ClassLoader loader = this.getClass().getClassLoader();

        List<InterceptorConfig> interceptorConfigList = config.getInterceptors();
        if (interceptorConfigList == null) {
            throw new InitializeException("No interceptor in configuration");
        }

        try {
            for (InterceptorConfig interceptorConfig : interceptorConfigList) {
                if (interceptorConfig.isLoaded()) {
                    interceptorList.add(interceptorConfig.getInterceptor());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Trying to load: " + interceptorConfig.getInterceptorClass());
                    }

                    Class<?> interceptorClass = loader.loadClass(interceptorConfig.getInterceptorClass());
                    
                    ServiceInterceptor interceptor = (ServiceInterceptor) interceptorClass.newInstance();
                    interceptor.initialize(config, interceptorConfig.getName(), id);
                    
                    interceptorList.add(interceptor);
                }
            }
        } catch (InitializeException initEx) {
            throw initEx;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new InitializeException("Cannot load interceptor chain", e);
        }
    }

    public synchronized void initialize(String id, List<ServiceInterceptor> interceptorList) {
        this.id = id;
        this.interceptorList.addAll(interceptorList);
    }

    public String getId() {
        return id;
    }

    public boolean isPermitted(Subject peerSubject, MessageContext context, QName operation) throws AuthorizationException {
        try {
            Set<X500Principal> pSet = peerSubject.getPrincipals(X500Principal.class);
            String peerIdentity = pSet.size() > 0 ? pSet.iterator().next().toString() : "unknown";

            int level = ServicePDP.NO_DECISION;

            for(ServiceInterceptor interceptor: interceptorList) {
                if (interceptor instanceof ServicePDP) {
                    ServicePDP pdp = (ServicePDP) interceptor;
                    level |= pdp.getPermissionLevel(peerSubject, context, operation);
                } else if (interceptor instanceof ServicePIP) {
                    ((ServicePIP) interceptor).collectAttributes(peerSubject, context, operation);
                }
            }            

            if (level < ServicePDP.ALLOWED) {
                logger.info("User " + peerIdentity + " not authorized for " + operation);
                return false;
            }
            if (level < ServicePDP.STRONG_DENIED) {
                logger.debug("User " + peerIdentity + " authorized for " + operation);
                return true;
            }
            if (level < ServicePDP.STRONG_ALLOWED) {
                logger.info("User " + peerIdentity + " not authorized for " + operation);
                return false;
            }

            logger.debug("User " + peerIdentity + " authorized for " + operation);
            return true;
        } catch (Exception ex) {
            logger.error(ex);
            throw new AuthorizationException("Authorization error: " + ex.getMessage(), ex);
        }
    }

    public List<ServiceInterceptor> getInterceptors() {
        return interceptorList;
    }

    public List<File> getAllTriggers() {
        List<File> tmpList = new ArrayList<File>(interceptorList.size());

        for(ServiceInterceptor interceptor: interceptorList) {
            String[] props = interceptor.getProperties();
            
            for (int j = 0; j < props.length; j++) {
                if (interceptor.isTriggerable(props[j])) {
                    tmpList.add(new File(interceptor.getProperty(props[j])));
                }
            }
        }

        return tmpList;
    }

    public Object clone() {
        ServiceAuthorizationChain result = new ServiceAuthorizationChain();
        result.id = this.id;
        result.interceptorList = new CopyOnWriteArrayList<ServiceInterceptor>();
        
        for(ServiceInterceptor interceptor: interceptorList) {
            result.interceptorList.add((ServiceInterceptor) interceptor.clone());
        }
        
        return result;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ServiceAuthorizationChain)) {
            return false;
        }
        
        return (((ServiceAuthorizationChain) obj).getInterceptors().equals(interceptorList));
    }
}
