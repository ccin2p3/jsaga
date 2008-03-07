/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.impl.security.descriptor;

import org.globus.wsrf.impl.security.util.PDPUtils;

import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.globus.wsrf.config.ConfigException;

import org.globus.security.gridmap.GridMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;

import javax.security.auth.Subject;

/**
 * Helper class for initialization of resource security descriptor. All
 * values returned from this class are determined by the security
 * descriptor object. The <i>initialized</i> property in
 * <code>ResourceSecurityDescriptor<code> is used to determine if
 * credentials/gridmap need to be loaded. If property is set to
 * <i>true</i>, no initialization is done.
 */
public class ResourceSecurityConfig extends SecurityConfig {

    private static Log logger =
        LogFactory.getLog(ResourceSecurityConfig.class.getName());

    public ResourceSecurityConfig(String resourceDescFilename) {
        super(null, resourceDescFilename);
    }

    public ResourceSecurityConfig(ResourceSecurityDescriptor desc) {
        super(desc);
    }
    
    /**
     * Initializes the descriptor, if initialized is set to false.
     */
    public void init() throws ConfigException {
        if (this.desc == null) {
            loadSecurityDescriptor();
        }
        if (!isInitialized()) {
            super.initSecurityDescriptor();
            ((ResourceSecurityDescriptor)this.desc).setInitialized(true);
        }
    }
    
    protected void initSecurityDescriptor(Document doc)
        throws ConfigException {
        if (doc != null) {
            this.desc = new ResourceSecurityDescriptor();
            try {
                this.desc.parse(doc.getDocumentElement());
            } catch (ElementParserException e) {
                throw new ConfigException(e);
            }
        }
    }

    // Initialize credentials
    protected void initCredentials() throws ConfigException {
        try {
            loadCredentials();
        } catch (Exception exp) {
            throw new ConfigException(exp);
        }
    }

    protected void loadAuthorization() throws ConfigException {
        
        if (this.desc == null) {
            return;
        }
        String authzType = this.desc.getAuthz();
        if (authzType != null) {
            // Global properties config is loaded by default. 
            ServiceAuthorizationChain authzChain = 
                PDPUtils.getContainerAuthzChain(authzType, this.jndiPathName);
            this.desc.setAuthzChain(authzChain);
        }
    }

    /** 
     * Reloads the credentials, if need be.
     */
    public void refresh() throws ConfigException {
        logger.debug("Refresh called");

        if (this.desc.isRefreshRequired()) {
            try {
                loadCredentials();
            } catch (Exception exp) {
                throw new ConfigException(exp);
            }
        }
    }

    /**
     * Returns the <code>ResourceSecurityDescriptor</code> object. 
     */
    public ResourceSecurityDescriptor getSecurityDescriptor() {
        return (ResourceSecurityDescriptor)this.desc;
    }

    /** 
     * Retrieves the <code>Subject</code>. <br>
     * Note: If <i>initialized</i> property is set to  <i>true</i>,
     * credentials are not loaded from configured file.
     */
    public Subject getSubject() throws ConfigException {

        if (!isInitialized()) {
            initCredentials();
        }
        else
            refresh();
        if (this.desc != null)
            return this.desc.getSubject();
        return null;
    }

    /** 
     * Retrieves the <code>GridMap</code>. <br> 
     * Note: If <i>initialized</i> property is set to  <i>true</i>,
     * gridmap is not loaded from configured file.
     */
    public GridMap getGridMap() throws ConfigException {

        if (!isInitialized()) {
            loadGridMap();
        }
        if (this.desc != null)
            return this.desc.getGridMap();
        return null;
    }

    /** 
     * Retrieves the <code>ServiceAuthorizationChain</code>. <br> 
     * Note: If <i>initialized</i> property is set to  <i>true</i>,
     * chain is not loaded from configured file.
     */
    public ServiceAuthorizationChain getAuthzChain() throws ConfigException {

        if (!isInitialized()) {
            loadAuthorization();
        }
        if (this.desc != null)
            return this.desc.getAuthzChain();
        return null;
    }

    private boolean isInitialized() {
        if (this.desc != null) {
            return (((ResourceSecurityDescriptor)this.desc).getInitialized());
        }
        return false;
    }
}
