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
package org.globus.wsrf.impl.security;

import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.MessageContext;
import org.globus.gsi.gridmap.GridMap;
import org.globus.gsi.gssapi.jaas.GlobusPrincipal;
import org.globus.gsi.gssapi.jaas.JaasSubject;
import org.globus.gsi.gssapi.jaas.UserNamePrincipal;

import org.globus.util.I18n;
import org.globus.wsrf.Resource;
import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.SecureResourcePropertiesHelper;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityConfig;
import org.globus.wsrf.security.SecurityException;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.utils.ContextUtils;

public class SecurityManagerImpl extends SecurityManager {

    private MessageContext context = null;

    private static Log logger =
        LogFactory.getLog(SecurityManagerImpl.class.getName());

    private static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.error",
                     SecurityManagerImpl.class.getClassLoader());

    public SecurityManagerImpl() {
        this(null);
    }

    public SecurityManagerImpl(SOAPMessageContext ctx) {
        this.context = (MessageContext)ctx;
        if (this.context == null) {
            this.context = MessageContext.getCurrentContext();
        }
    }

    /**
     * Returns the system subject (containing the container
     * credentials).
     *
     * @throws SecurityException if failed to obtain container credentials.
     * @return the system subject.
     */
    public Subject getSystemSubject() throws SecurityException {
        try {
            logger.debug("Get system subject");
            ContainerSecurityConfig config =
                ContainerSecurityConfig.getConfig();
            config.refresh();
            return config.getSubject();
        } catch (Exception e) {
            logger.debug(i18n.getMessage("failContainerCred"), e);
            throw new SecurityException(i18n.getMessage("failContainerCred"), 
                                        e);
        }
    }

    /**
     * Returns effective <i>service</i> subject. Returns subject in
     * this order depending on which is set: <i>service</i> subject,
     * system subject. The target service set in the message context
     * is used.
     *
     * @throws SecurityException if failed to obtain credentials.
     * @return the service subject if set, system subject
     *         otherwise.
     */
    public Subject getServiceSubject() throws SecurityException {

        if (this.context == null) {
            logger.debug(i18n.getMessage("noContext"));
            throw new SecurityException(i18n.getMessage("noContext"));
        }
        String servicePath = ContextUtils.getTargetServicePath(this.context);
        return getServiceSubject(servicePath);
    }

    /**
     * Returns effective <i>service</i> subject. Returns subject in
     * this order depending on which is set: <i>service</i> subject,
     * system subject.
     *
     * @param servicePath service path
     * @throws SecurityException if failed to obtain credentials.
     * @return the service subject if set, system subject
     *         otherwise.
     */
    public Subject getServiceSubject(String servicePath)
        throws SecurityException {
        Subject subject = null;
        try {
            subject = ServiceSecurityConfig.getSubject(servicePath);
        } catch (Exception e) {
            logger.debug(i18n.getMessage("failServiceCred"), e);
            throw new SecurityException(i18n.getMessage("failServiceCred"),
                                        e);
        }
        if (subject == null) {
            try {
                return getSystemSubject();
            } catch (Exception e) {
                logger.debug(i18n.getMessage("failServiceContCred"), e);
                throw new SecurityException(i18n
                                            .getMessage("failServiceContCred"),
                                            e);
            }
        } 
        return subject;
    }

    /**
     * Returns effective <i>resource</i> subject. Returns subject in
     * this order depending on which is set: <i>resource</i> subject,
     * <i>service</i> subject, system subject. Target service
     * associated with message context  is used.
     *
     * @param resource object representing the resource
     * @throws SecurityException if failed to obtain credentials.
     * @return the service subject if set, system subject
     *         otherwise.
     */
    public Subject getSubject(Resource resource)
        throws SecurityException {
        Subject subject = getResourceSubject(resource);
        return (subject == null) ? getServiceSubject() : subject;
    }

    /**
     * Returns effective <i>resource</i> subject. Returns subject in
     * this order depending on which is set: <i>resource</i> subject,
     * <i>service</i> subject, system subject.
     *
     * @param servicePath service path
     * @param resource object representing the resource
     * @throws SecurityException if failed to obtain credentials.
     * @return the service subject if set, system subject
     *         otherwise.
     */
    public Subject getSubject(String servicePath, Resource resource)
        throws SecurityException {
        try {
            Subject subject =
                SecureResourcePropertiesHelper.getResourceSubject(resource);
            return (subject == null) ?
                   getServiceSubject(servicePath) : subject;
        } catch (Exception e) {
            logger.debug(i18n.getMessage("failSystemCred"), e);
            throw new SecurityException(i18n.getMessage("failSystemCred"), e);
        }
    }


    private Subject getResourceSubject(Resource resource)
        throws SecurityException {
        try {
            return SecureResourcePropertiesHelper.getResourceSubject(resource);
        } catch (Exception e) {
            logger.debug(i18n.getMessage("failSystemCred"), e);
            throw new SecurityException(i18n.getMessage("failSystemCred"), e);
        }
    }

    /**
     * Returns the identity of the current caller.
     * Might return null if client did not authenticate.
     *
     * @return the identity of the caller. Might be null.
     */
    public String getCaller() {
        Principal p = getCallerPrincipal();
        return (p == null) ? null : p.getName();
    }


    /**
     * Returns the identity of the current caller.
     * Might return null if client did not authenticate.
     *
     * @return the identity of the caller. Might be null.
     */
    public Principal getCallerPrincipal() {

        if (this.context == null) {
            return null;
        }

        Subject caller =
            (Subject)this.context.getProperty(Constants.PEER_SUBJECT);

        return getCallerPrincipal(caller);
    }

    /**
     * Extracts the principal from caller subject
     *
     * @param caller
     *        Subject
     *
     * @return principal associated with subject
     */
    public Principal getCallerPrincipal(Subject caller) {

        if (caller == null) {
            return null;
        }

        Set principals = caller.getPrincipals();

        if ((principals == null) || principals.isEmpty()) {
            return null;
        }

        return (Principal) principals.iterator().next();
    }

    /**
     * Sets the service object with the current invocation subject.
     * The invocation subject must contain some private credentials.
     * All <code>GlobusPrincipals</code> in invocation subject
     * are added as authorized users to access the service. The
     * service associated with the message context is used.<br>
     * Note: On a GridMap refresh this user data is lost.
     *
     * @return the new service subject object
     * @exception SecurityException if the operation fails.
     */
    public Subject setServiceOwnerFromContext() throws SecurityException {

        if (this.context == null) {
            logger.error(i18n.getMessage("noContext"));
            throw new SecurityException(i18n.getMessage("noContext"));
        }
        String servicePath = ContextUtils.getTargetServicePath(this.context);
        return setServiceOwnerFromContext(servicePath);
    }

    /**
     * Sets the service object with the current invocation subject.
     * The invocation subject must contain some private credentials.
     * All <code>GlobusPrincipals</code> in invocation subject
     * are added as authorized users to access the service.<br>
     * Note: On a GridMap refresh this user data is lost.
     *
     * @param servicePath the service to set the subject on.
     * @return the new service subject object
     * @exception SecurityException if the operation fails.
     */
    public Subject setServiceOwnerFromContext(String servicePath)
        throws SecurityException {

        Subject subject = getSubject();

        try {
            ServiceSecurityConfig.setSubject(subject, servicePath);
        } catch (ConfigException exp) {
            throw new SecurityException(exp);
        }

        GridMap gridMap = null;
        try {
            gridMap = ServiceSecurityConfig.getGridMap(servicePath);

            if (gridMap == null) {
                gridMap = new GridMap();
                ServiceSecurityConfig.setGridMap(gridMap, servicePath);
            }
        } catch (ConfigException exp) {
            throw new SecurityException(exp);
        }

        addAuthorizedUser(subject, gridMap);
        return subject;
    }

    /**
     * Sets the resource descriptor object with the current invocation subject.
     * The invocation subject must contain some private credentials.
     * All <code>GlobusPrincipals</code> in invocation subject
     * are added as authorized users to access the resource. <br>
     * Note: On a GridMap refresh this user data is lost.
     *
     * @param desc the resource security descriptor to set the subject on.
     * @exception SecurityException if the operation fails.
     */
    public void setResourceOwnerFromContext(ResourceSecurityDescriptor desc)
        throws SecurityException {

        if (desc == null)
            return;

        Subject subject = getSubject();
        desc.setSubject(subject);

        GridMap gridMap = desc.getGridMap();
        if (gridMap == null) {
            gridMap = new GridMap();
        }
        desc.setGridMap(gridMap);

        addAuthorizedUser(subject, gridMap);

    }

    // add all GlobusPrincipal as authorized users
    private void addAuthorizedUser(Subject subject, GridMap gridMap) {

        Set principals = subject.getPrincipals(GlobusPrincipal.class);

        if ((principals != null) && !principals.isEmpty()) {
            Iterator iter = principals.iterator();

            while (iter.hasNext()) {
                Principal principal = (Principal) iter.next();
                gridMap.map(principal.getName(),
                            System.getProperty("user.name"));
            }
        }
    }

    private Subject getSubject() throws SecurityException {

        Subject subject = JaasSubject.getCurrentSubject();

        if (subject == null) {
            throw new SecurityException(i18n.getMessage("noSubject"));
        }

        Set creds = subject.getPrivateCredentials();

        if ((creds == null) || creds.isEmpty()) {
            throw new SecurityException(i18n.getMessage("noPrivateCred"));
        }

        // make sure nobody else can modify it
        subject.setReadOnly();

        return subject;
    }

    /**
     * Returns the local user name of the caller as mapped in the configured
     * grid map file. Will be null if GridMap Authorization is not
     * used.
     *
     * @return String[]
     *         Array of usernames the caller assocaited with this
     *         invocation is mapped to.
     */
    public String[] getLocalUsernames() throws SecurityException {

        if (this.context == null) {
            logger.debug(i18n.getMessage("noContext"));
            throw new SecurityException(i18n.getMessage("noContext"));
        }

        Subject subject =
            (Subject)this.context.getProperty(Constants.PEER_SUBJECT);

        if (subject == null) {
            logger.debug(i18n.getMessage("noPeerSubject"));
            throw new SecurityException(i18n.getMessage("noPeerSubject"));
        }

        Set principalSet = subject.getPrincipals();
        Vector userNames = new Vector();
        if (principalSet != null) {
            Iterator it = principalSet.iterator();
            while (it.hasNext()) {
                Object principal = it.next();
                if (principal instanceof UserNamePrincipal) {
                    userNames.add(((UserNamePrincipal)principal).getName());
                }
            }
        }

        String[] userNameArray = null;
        if (userNames.size() > 0) {
            userNameArray = new String[userNames.size()];
            userNames.toArray(userNameArray);
        }
        return userNameArray;
    }
}
