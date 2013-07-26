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

import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.impl.security.util.PDPUtils;

import org.globus.wsrf.security.SecurityException;

import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.globus.wsrf.config.ConfigException;

import org.globus.wsrf.utils.ContextUtils;

import org.globus.util.I18n;

import org.apache.axis.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;

import javax.security.auth.Subject;
import org.globus.gsi.gridmap.GridMap;

/**
 * Helper class for initialization of service security descriptor. All
 * values returned from this class are determined by the security
 * descriptor file configured for the said service. If a file was not
 * configured, null is returned.
 */
public class ServiceSecurityConfig extends SecurityConfig {

    private static Log logger =
        LogFactory.getLog(ServiceSecurityConfig.class.getName());

    private static I18n i18n =
        I18n.getI18n(SecurityDescriptor.RESOURCE,
                     SecurityConfig.class.getClassLoader());

    ServiceSecurityConfig(String jndiPath, ServiceSecurityDescriptor desc) {
        super(jndiPath, desc);
    }

    ServiceSecurityConfig(String servicePath, String descFilename)
        throws SecurityException {
        this.jndiPathName = servicePath;
        this.descriptorFile = descFilename;

        logger.debug("Service: " + this.jndiPathName + " Desc file"
                     + this.descriptorFile);
    }

    public static void initialize(MessageContext msgCtx)
        throws ConfigException, SecurityException {
        initialize(ContextUtils.getTargetServicePath(msgCtx), null);
    }

    public static void initialize(String servicePath)
        throws ConfigException, SecurityException {
        initialize(servicePath, null);
    }

    public static void initialize(String servicePath, String serviceDescFile)
        throws ConfigException, SecurityException {
        if (servicePath == null) {
            return;
        }
        if (serviceDescFile == null) {
            serviceDescFile = AuthUtil.getSecurityDescFile(servicePath);
            if (serviceDescFile == null) {
                logger.debug("No security descriptor for: " + servicePath);
                return;
            }
        }
        servicePath = servicePath.intern();
        synchronized (servicePath) {
            if (isInitialized(servicePath)) {
                logger.debug("Already initialized: " + servicePath);
                return;
            }
            logger.debug(i18n.getMessage("serviceDescInit", servicePath));
            ServiceSecurityConfig config =
                new ServiceSecurityConfig(servicePath, serviceDescFile);
            config.initialize();
            logger.debug("Initialized: " + servicePath);
        }
    }

    protected void initialize() throws ConfigException {
        try {
            super.initialize();
        } catch (ConfigException e) {
            throw new ConfigException(
                       i18n.getMessage("serviceInitFail", this.jndiPathName),
                       e);
        }
    }

    protected void initSecurityDescriptor(Document doc)
        throws ConfigException {
        if (doc != null) {
            try {
                this.desc = new ServiceSecurityDescriptor();
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
            ServiceAuthorizationChain authzChain =
                PDPUtils.getServiceAuthzChain(authzType, this.jndiPathName);
            this.desc.setAuthzChain(authzChain);
        }
    }

    // Public methods
    /**
     * Returns subject configured in security descriptor of the
     * service
     */
    public static Subject getSubject(String servicePath)
        throws ConfigException, SecurityException {
        refresh(servicePath);
        return retrieveSubject(servicePath);
    }

    /**
     * Returns authz chain configured in security descriptor of the
     * service
     */
    public static ServiceAuthorizationChain getAuthzChain(String servicePath)
        throws ConfigException {
        return retrieveAuthzChain(servicePath);
    }

    /**
     * Reloads the credentials, if need be.
     * (MessageContext must be associated with the current thread)
     */
    public static void refresh(String jndiPath)
        throws ConfigException, SecurityException {
        logger.debug("Refresh called " + jndiPath);
        if (jndiPath == null) {
            return;
        }

        jndiPath = jndiPath.intern();
        initialize(jndiPath);
        synchronized (jndiPath) {
            boolean refreshReq = isRefreshRequired(jndiPath);
            if (refreshReq) {
                logger.debug(i18n.getMessage("serviceDescRefresh", jndiPath));
                ServiceSecurityDescriptor secDesc =
                    getSecurityDescriptor(jndiPath);
                ServiceSecurityConfig config =
                    new ServiceSecurityConfig(jndiPath, secDesc);
                config.initCredentials();
                config.storeSecurityDescriptor();
            }
        }
    }

    /**
     * Stores the <code>Subject</code> for this service, overwriting whatever
     * exists. <br>
     * Note: If this method is used, then when a <code>refresh</code>
     * is called, it overwrites the subject with whatever subject is
     * generated from the credentials configured in the security
     * descriptor. If no security desacriptor file was specified,
     * these credentials are never refreshed.
     */
    public static void setSubject(Subject subject, String jndiPath)
        throws ConfigException {
        ServiceSecurityDescriptor desc =
            (ServiceSecurityDescriptor)retrieveSecurityDescriptor(jndiPath);
        if (desc == null)
            desc = new ServiceSecurityDescriptor();
        storeSubject(subject, jndiPath, desc);
    }

    /**
     * Stores the <code>GridMap</code> for this service, overwriting whatever
     * exists. <br>
     */
    public static void setGridMap(GridMap gridmap, String jndiPath)
        throws ConfigException {
        ServiceSecurityDescriptor desc =
            (ServiceSecurityDescriptor)retrieveSecurityDescriptor(jndiPath);
        if (desc == null)
            desc = new ServiceSecurityDescriptor();
        storeGridMap(gridmap, jndiPath, desc);
    }

    /**
     * Stores the <code>ServiceAuthorizationChain</code> for this
     * service, overwriting whatever exists. <br>
     */
    public static void setAuthzChain(ServiceAuthorizationChain authzChain,
                                     String jndiPath)
        throws ConfigException {
        ServiceSecurityDescriptor desc =
            (ServiceSecurityDescriptor)retrieveSecurityDescriptor(jndiPath);
        if (desc == null)
            desc = new ServiceSecurityDescriptor();
        storeAuthzChain(authzChain, jndiPath, desc);
    }

    /**
     * Stores the <code>ServiceSecurityDescriptor</code> for this
     * service, overwriting whatever exists. <br>
     */
    public static void setSecurityDescriptor(ServiceSecurityDescriptor desc,
                                             String jndiPath)
        throws ConfigException {
        storeSecurityDescriptor(desc, jndiPath);
    }

    /**
     * Retrieves the <code>GridMap</code> for this service
     */
    public static GridMap getGridMap(String jndiPath)
        throws ConfigException {
        logger.debug("Get gridmap " + jndiPath);
        return retrieveGridMap(jndiPath);
    }

    /**
     * Retrieves the <code>ServiceSecurityDescriptor</code> for this service
     */
    public static ServiceSecurityDescriptor
        getSecurityDescriptor(String jndiPath)
        throws ConfigException {
        logger.debug("Get descriptor " + jndiPath);
        return (ServiceSecurityDescriptor)retrieveSecurityDescriptor(jndiPath);
    }

    /**
     * Retrieves the <code>ServiceSecurityDescriptor</code> for the current
     * service
     */
    public static ServiceSecurityDescriptor
        getSecurityDescriptor()
        throws ConfigException {
        String jndiPath = ContextUtils.getTargetServicePath(
            MessageContext.getCurrentContext());
        logger.debug("Get descriptor " + jndiPath);
        return (ServiceSecurityDescriptor)retrieveSecurityDescriptor(jndiPath);
    }
}
