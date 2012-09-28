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
 * Version info: $Id: AuthZConfigChain.java,v 1.4 2009/03/09 13:42:06 zangran Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.glite.ce.commonj.authz.ChainConfig;
import org.glite.ce.commonj.authz.ConfigException;
import org.glite.ce.commonj.authz.InitializeException;
import org.glite.ce.commonj.authz.InterceptorConfig;
import org.glite.ce.commonj.authz.ServiceAuthorizationChain;
import org.glite.ce.commonj.configuration.CEConfigResource;

public class AuthZConfigChain implements CEConfigResource, ChainConfig {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AuthZConfigChain.class.getName());

    private ServiceAuthorizationChain chain;
    private List<InterceptorConfig> interceptorList = null;
    private HashMap<String, HashMap<String, Object>> interceptorConfigs = null;

    private String chainID;
    private String chainName;

    public AuthZConfigChain(String id, String cName, HashMap<String, HashMap<String, Object>> configItems) throws ClassNotFoundException, IllegalArgumentException, InitializeException {
        if(id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if(cName == null) {
            throw new IllegalArgumentException("cName not specified!");
        }
        if(configItems == null) {
            throw new IllegalArgumentException("configItems not specified!");
        } 
        
        chainID = id;
        chainName = cName;
        chain = new ServiceAuthorizationChain();

        interceptorList = new ArrayList<InterceptorConfig>(configItems.size());
        interceptorConfigs = new HashMap<String, HashMap<String, Object>>(0);

        for(String pName : configItems.keySet()) {
            HashMap<String, Object> pMap = (HashMap<String, Object>)configItems.get(pName);
            
            interceptorList.add(new InterceptorConfig(pName, (String) pMap.get(AuthZConfigParser.PLUGIN_CLASS_LABEL)));

            HashMap<String, Object> newMap = (HashMap<String, Object>)pMap.clone();
            newMap.remove(AuthZConfigParser.PLUGIN_CLASS_LABEL);
            
            interceptorConfigs.put(pName, newMap);
        }

        chain.initialize(this, chainName, id);
    }

    protected AuthZConfigChain(String id, String cName, List<InterceptorConfig> intConfig, HashMap<String, HashMap<String, Object>> interceptorMap, ServiceAuthorizationChain sChain) throws ClassNotFoundException, IllegalArgumentException {
        if(id == null) {
            throw new IllegalArgumentException("id not specified!");
        }
        if(cName == null) {
            throw new IllegalArgumentException("cName not specified!");
        }
        if(intConfig == null) {
            throw new IllegalArgumentException("intConfig not specified!");
        }
        if(interceptorMap == null) {
            throw new IllegalArgumentException("interceptorMap not specified!");
        }
        if(sChain == null) {
            throw new IllegalArgumentException("sChain not specified!");
        } 
        
        chainID = id;
        chainName = cName;
        interceptorList = intConfig;
        interceptorConfigs = (HashMap<String, HashMap<String, Object>>) interceptorMap.clone();
        chain = sChain;
    }

    public ServiceAuthorizationChain getChain() {
        return chain;
    }

    public boolean equals(HashMap<String, HashMap<String, Object>> configItems) throws ConfigException {
        if (interceptorList.size() != configItems.size()) {
            return false;
        }

        int index = 0;
        
        for (String pName : configItems.keySet()) {
            HashMap<String, Object> pMap = configItems.get(pName);
            String pClass = (String) pMap.get(AuthZConfigParser.PLUGIN_CLASS_LABEL);

            if (!pName.equals(interceptorList.get(index).getName()) || !pClass.equals(interceptorList.get(index).getInterceptorClass())) {
                return false;
            }
            
            index++;
            
            HashMap<String, Object> newMap = interceptorConfigs.get(pName);

            if (newMap == null) {
                return false;
            }

            for(String arg : newMap.keySet()) {
                Object tmpo = newMap.get(arg);
                
                if (tmpo == null || tmpo.equals(pMap.get(arg))) {
                    return false;
                }                
            }            
        }

        return true;
    }

    public Object clone() {
        try {
            return new AuthZConfigChain(chainID, chainName, interceptorList, interceptorConfigs, chain);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        return null;
    }

    public List<InterceptorConfig> getInterceptors() throws ConfigException {
        return interceptorList;
    }

    public Object getProperty(String name, String property) {
        HashMap<String, Object> config = interceptorConfigs.get(name);
        
        if (config == null) {
            return null;
        }
        
        return config.get(property);
    }

    public void setProperty(String name, String property, Object value) {
        HashMap<String, Object> config = interceptorConfigs.get(name);
        
        if (config != null) {
            config.put(property, value);
        }        
    }
}
