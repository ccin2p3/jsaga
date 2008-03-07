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
package org.globus.wsrf.security;

import javax.xml.rpc.handler.soap.SOAPMessageContext;

import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

import javax.security.auth.Subject;

import java.security.Principal;

import org.globus.wsrf.Resource;

import org.globus.wsrf.impl.security.SecurityManagerImpl;

public abstract class SecurityManager {


    /**
     * Returns an instance of the security manager.
     *
     * @return an instance of the security manager.
     */
    public static SecurityManager getManager() {
        return new SecurityManagerImpl();
    }

    /**
     * Returns an instance of the security manager.
     *
     * @return an instance of the security manager.
     */
    public static SecurityManager getManager(SOAPMessageContext ctx) {
        return new SecurityManagerImpl(ctx);
    }

    /**
     * Returns the system subject (containing the container
     * credentials).
     *
     * @throws SecurityException if failed to obtain container credentials.
     * @return the system subject.
     */
    public abstract Subject getSystemSubject() throws SecurityException;


    /**
     * Returns effective <i>service</i> subject. Returns subject in
     * this order depending on which is set: <i>service</i> subject,
     * system subject.
     *
     * @throws SecurityException if failed to obtain credentials.
     * @return the service subject if set, system subject
     *         otherwise.
     */
    public abstract Subject getServiceSubject() throws SecurityException;

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
    public abstract Subject getServiceSubject(String servicePath)
        throws SecurityException;

    public abstract Subject getSubject(Resource resource) 
        throws SecurityException;

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
    public abstract Subject getSubject(String servicePath, Resource resource)
        throws SecurityException;

    /**
     * Returns the identity of the current caller.
     * Might return null if client did not authenticate.
     *
     * @return the identity of the caller. Might be null.
     */
    public abstract String getCaller();

    /**
     * Returns the identity of the current caller.
     * Might return null if client did not authenticate.
     *
     * @return the identity of the caller. Might be null.
     */
    public abstract Principal getCallerPrincipal();

    /**
     * Returns the identity of credentials in the subject.
     *
     * @param caller Subject to return identity for.
     * @return the identity of the caller.
     */
    public abstract Principal getCallerPrincipal(Subject caller);

    /**
     * Sets the service with the current invocation subject.
     * The invocation subject must contain some private credentials.
     * All <code>GlobusPrincipals</code> in invocation subject
     * are added as authorized users to access the service.<br>
     * Note: On a GridMap refresh this user data is lost.
     *
     * @param servicePath the service to set the subject on.
     * @return the new service subject object
     * @exception SecurityException if the operation fails.
     */
    public abstract Subject setServiceOwnerFromContext(String servicePath)
        throws SecurityException;

    /**
     * Sets the target service with the current invocation subject.
     * The invocation subject must contain some private credentials.
     * All <code>GlobusPrincipals</code> in invocation subject
     * are added as authorized users to access the service.<br>
     * Note: On a GridMap refresh this user data is lost.
     *
     * @return the new service subject object
     * @exception SecurityException if the operation fails.
     */
    public abstract Subject setServiceOwnerFromContext() 
        throws SecurityException;

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
    public abstract void 
        setResourceOwnerFromContext(ResourceSecurityDescriptor desc)
        throws SecurityException;

    /**
     * Returns the local user name of the caller as mapped in the configured
     * grid map file. Will be null if GridMap Authorization is not used.
     */
    public abstract String[] getLocalUsernames() throws SecurityException;
}
