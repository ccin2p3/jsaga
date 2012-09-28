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

import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSCredential;

import org.gridforum.jgss.ExtendedGSSManager;

import org.globus.wsrf.impl.security.util.PDPUtils;
import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.config.ConfigException;

import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;

import javax.security.auth.Subject;

import java.util.Map;
import java.util.HashMap;

import org.apache.axis.MessageContext;
import org.globus.gsi.gridmap.GridMap;
import org.globus.gsi.gssapi.JaasGssUtil;

/**
 * Helper class for initialization of container security descriptor. All
 * values returned from this class are determined by the security
 * descriptor file configured for the container. If a file was not
 * configured, null is returned for all values, other than subject. If
 * no file was configured or credential was not configured, then
 * default credentials, if present, are used.
 */
public class ContainerSecurityConfig extends SecurityConfig {

    private static Log logger =
        LogFactory.getLog(ContainerSecurityConfig.class.getName());

    private static I18n i18n =
        I18n.getI18n(SecurityDescriptor.RESOURCE,
                     SecurityConfig.class.getClassLoader());

    private static ContainerSecurityConfig securityConfig;
    // is not null only if default credentials were used.
    GSSCredential credential = null;

    static Map properties = null;

    ContainerSecurityConfig(String descFile) {
        this.descriptorFile = descFile;
    }

    /**
     * Returns container config object, assuming container security
     * config filename is set in the global deployment descriptor as
     * paramater <code>CONT_SEC_DESCRIPTOR</code>
     */
    public static synchronized ContainerSecurityConfig
        getConfig() throws ConfigException {
        return getConfig(getSecurityDescFile());
    }

    public static synchronized ContainerSecurityConfig
        getConfig(String secDescFilename)
        throws ConfigException {
        logger.debug("initialize called");
        if (securityConfig == null) {
            logger.debug("The file is " + getSecurityDescFile());
            properties = new HashMap();
            securityConfig =
                new ContainerSecurityConfig(secDescFilename);
            securityConfig.initialize();
        }
        return securityConfig;
    }

    protected void storeSecurityDescriptor() throws ConfigException {
        properties.put(SECURITY_DESCRIPTOR, this.desc);
    }

    protected void initialize() throws ConfigException {
        logger.debug(i18n.getMessage("containerDescInit"));
        try {
            super.initialize();
        } catch (ConfigException e) {
            throw new ConfigException(i18n.getMessage("containerInitFail"), e);
        }
    }

    protected static boolean isInitialized()
        throws ConfigException {
        Boolean value = (Boolean)properties.get(SECURITY_INIT_NAME);
        return (value == null) ? false : value.booleanValue();
    }

    protected void setInitialized(boolean init)
        throws ConfigException {
        properties.put(SECURITY_INIT_NAME, new Boolean(init));
    }

    protected void initSecurityDescriptor(Document doc)
        throws ConfigException {
        this.desc = new ContainerSecurityDescriptor();
        if (doc != null) {
            try {
                this.desc.parse(doc.getDocumentElement());
            } catch (ElementParserException e) {
                throw new ConfigException(e);
            }
        }
    }

    protected void loadAuthorization() throws ConfigException {

        if (this.desc == null) {
            return;
        }
        String authzType = this.desc.getAuthz();
        if (authzType != null) {
            ServiceAuthorizationChain authzChain =
                PDPUtils.getContainerAuthzChain(authzType, this.jndiPathName);
            this.desc.setAuthzChain(authzChain);
        }
    }

    // Initialize credentials - if not set, try default creds.
    protected void initCredentials() throws ConfigException {
        logger.debug("Init creds called");
        Subject subject = null;
        if (this.desc != null) {
            logger.debug("Not null, calling load creds");
            try {
                loadCredentials();
                subject = this.desc.getSubject();
            } catch (Exception exp) {
                throw new ConfigException(exp);
            }
        }
        if (subject == null) {
            logger.debug("No configured subject, credential is not null");
            subject = getDefaultSubject();
            this.desc.setSubject(subject);
        }  else {
            logger.debug("Configured subject, credential is null");
            this.credential = null;
        }
    }

    private Subject getDefaultSubject() throws ConfigException {
        Subject subject = null;
        try {
            GSSManager manager = ExtendedGSSManager.getInstance();
            this.credential =
                manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
            subject = JaasGssUtil.createSubject(this.credential);
            subject.setReadOnly();
            return subject;
        } catch (Exception exp) {
            // if not, its an insecure container.
            if (this.descriptorFile != null)
                throw new ConfigException(exp);
        }
        return subject;
    }

    private boolean isContainerRefreshRequired(String descFilename)
        throws ConfigException {
        logger.debug("is refresh required " + (this.credential == null));

        try {
            if ((this.credential != null) &&
                (this.credential.getRemainingLifetime() < 60)) {
                logger.debug("true");
                return true;
            }
            logger.debug("false");
        } catch (Exception exp) {
            throw new ConfigException(exp);
        }


        return isRefreshRequired();
    }

    // Returns the security descriptor file configured for container,
    // if any
    private static String getSecurityDescFile() throws ConfigException {
        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx != null) {
            return ContainerConfig.getConfig(ctx.getAxisEngine()).getOption(
                                                 CONT_SEC_DESCRIPTOR);
        } else {
            // resort to default
            return ContainerConfig.getConfig().getOption(CONT_SEC_DESCRIPTOR);
        }
    }

    // Public methods
    /**
     * Reloads the credentials, if need be.
     */
    public synchronized void refresh() throws ConfigException {

        String descFilename = getSecurityDescFile();

        if (!isInitialized()) {
            logger.debug("Not initialized");

            ContainerSecurityConfig config =
                new ContainerSecurityConfig(descFilename);
            config.initialize();
        }
        // if desc file is null, no refresh is possible
        boolean refreshReq;
        // Attempt to load, maybe proxy has been created.
        if ((descFilename == null) && (this.credential == null)) {
            refreshReq = true;
        } else {
            refreshReq = isContainerRefreshRequired(descFilename);
        }
        if (refreshReq) {
            logger.debug(i18n.getMessage("containerDescRefresh"));
            initCredentials();
            storeSecurityDescriptor();
        }
    }

    /**
     * Retrieves the <code>Subject</code> for the container
     */
    public Subject getSubject() throws ConfigException {
        SecurityDescriptor desc =
            (SecurityDescriptor)properties.get(SECURITY_DESCRIPTOR);
        return (desc == null) ? null : desc.getSubject();
    }

    /**
     * Retrieves the <code>GridMap</code> for the container
     */
    public GridMap getGridMap() throws ConfigException {
        SecurityDescriptor desc =
            (SecurityDescriptor)properties.get(SECURITY_DESCRIPTOR);
        return (desc == null) ? null : desc.getGridMap();
    }

    /**
     * Retrieves the <code>ServiceAuthorizationChain</code> for the container
     */
    public ServiceAuthorizationChain getAuthzChain() throws ConfigException {
        SecurityDescriptor desc =
            (SecurityDescriptor)properties.get(SECURITY_DESCRIPTOR);
        return (desc == null) ? null : desc.getAuthzChain();
    }

    /**
     * Retrieves the <code>ContainerSecurityDescriptor</code> for this service
     */
    public ContainerSecurityDescriptor getSecurityDescriptor()
        throws ConfigException {
        return (ContainerSecurityDescriptor)properties
            .get(SECURITY_DESCRIPTOR);
    }

    /**
     * Stores the <code>Subject</code> for container, overwriting whatever
     * exists. <br>
     * Note: If this method is used, then when a <code>refresh</code>
     * is called, it overwrites the subject with whatever subject is
     * generated from the credentials configured in the security
     * descriptor or default credential
     */
    public void setSubject(Subject subject) throws ConfigException {
        this.credential = null;
        // descriptor never null for container
        this.desc.setSubject(subject);
        storeSecurityDescriptor();
    }

    /**
     * Stores the <code>GridMap</code> for container, overwriting whatever
     * exists. <br>
     */
    public void setGridMap(GridMap gridMap) throws ConfigException {
        // descriptor never null for container
        this.desc.setGridMap(gridMap);
        storeSecurityDescriptor();
    }

    /**
     * Stores the <code>ServiceAuthorizationChain</code> for
     * container, overwriting whatever exists. <br>
     */
    public void setAuthzChain(ServiceAuthorizationChain authzChain)
        throws ConfigException {
        // descriptor never null for container
        this.desc.setAuthzChain(authzChain);
        storeSecurityDescriptor();
    }

    /**
     * Stores the <code>ContainerSecurityDescriptor</code> for this
     * service, overwriting whatever exists. <br>
     */
    public void setSecurityDescriptor(ContainerSecurityDescriptor desc)
        throws ConfigException {
        this.desc = desc;
        properties.put(SECURITY_DESCRIPTOR, desc);
    }

    /**
     * Returns the container security descriptor filename
     */
    public String getSecurityDescriptorFile() {
        return this.descriptorFile;
    }

    private boolean isRefreshRequired() {
        SecurityDescriptor desc =
            (SecurityDescriptor)properties.get(SECURITY_DESCRIPTOR);
        return (desc == null) ? false : desc.isRefreshRequired();
    }
}
