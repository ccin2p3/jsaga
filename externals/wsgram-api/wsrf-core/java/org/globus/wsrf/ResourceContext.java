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
package org.globus.wsrf;

import java.util.Iterator;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPHeaderElement;

/**
 * <code>ResourceContext</code> wraps around a {@link SOAPMessageContext
 * SOAPMessageContext} and provides convenience functions for obtaining
 * {@link ResourceKey ResourceKey}, {@link ResourceHome ResourceHome},
 * and resource objects associated with the given SOAP message (referenced
 * in <code>SOAPMessageContext</code>).
 */
public abstract class ResourceContext {

    protected ResourceContext() {}

    /**
     * Returns <code>ResourceContext</code> associated with the current thread.
     *
     * @return <code>ResourceContext</code> associated with the current thread.
     */
    public static ResourceContext getResourceContext()
        throws ResourceContextException {
        return new org.globus.wsrf.impl.ResourceContextImpl();
    }

    /**
     * Returns <code>ResourceContext</code> initialized with a specific
     * <code>SOAPMessageContext</code>.
     *
     * @return <code>ResourceContext</code> initialized with a specific
     *         <code>SOAPMessageContext</code>.
     */
    public static ResourceContext getResourceContext(SOAPMessageContext ctx)
        throws ResourceContextException {
        return new org.globus.wsrf.impl.ResourceContextImpl(ctx);
    }

    /**
     * Gets <code>ResourceKey</code> instance using <code>ResourceHome</code>
     * lookup.
     *
     * @return <code>ResourceKey</code> of name and type associated with the
     *         target service' <code>ResourceHome</code>. Returns null if key
     *         is not found in the message.
     * @throws ResourceContextException if error occurs during lookup.
     * @see #getResourceHome()
     */
    public abstract ResourceKey getResourceKey()
        throws ResourceContextException;

    /**
     * Gets <code>ResourceKey</code> of a given name and type.
     *
     * @return <code>ResourceKey</code> of the given type and name. Returns
     *         null if the key of the specified name was not found in the
     *         message.
     * @throws ResourceContextException if error occurs during lookup.
     */
    public abstract ResourceKey getResourceKey(QName keyName, Class keyClass)
        throws ResourceContextException;

    /**
     * Gets <code>SOAPHeaderElement</code> that contains the resource key
     * for the service using <code>ResourceHome</code> lookup.
     *
     * @return The <code>SOAPHeaderElement</code> that contains the resource
     *         key for the service. Returns null if one not found.
     * @throws ResourceContextException if error occurs during lookup.
     * @see #getResourceHome()
     */
    public abstract SOAPHeaderElement getResourceKeyHeader()
        throws ResourceContextException;

    /**
     * Gets <code>SOAPHeaderElement</code> of the specified name that contains
     * the resource key.
     *
     * @return The <code>SOAPHeaderElement</code> that contains the resource
     *         key of the given name. Returns null if one not found.
     * @throws ResourceContextException if error occurs during lookup.
     */
    public abstract SOAPHeaderElement getResourceKeyHeader(QName keyName)
        throws ResourceContextException;

    /**
     * Gets <code>ResourceHome</code> associated with the service.
     *
     * @return <code>ResourceHome</code> associated with the service. Cannot be
     *         null.
     * @throws NoResourceHomeException if <code>ResourceHome</code> is not
     *         configured for the specified service.
     * @throws ResourceContextException if any other error.
     */
    public abstract ResourceHome getResourceHome()
        throws ResourceContextException, NoResourceHomeException;

    /**
     * Gets path of the service.
     *
     * @return The path of the service.
     */
    public abstract String getService();

    /**
     * Gets the full URL of the service
     *
     * @return The URL of the service
     */
    public abstract URL getServiceURL();

    /**
     * Gets actual resource.
     *
     * @return The actual resource object. Cannot be null.
     * @throws NoSuchResourceException if resource
     *         was not specified in the request or it does not exist.
     * @throws ResourceContextException if error occurs during resource lookup.
     */
    public abstract Resource getResource()
        throws ResourceContextException, ResourceException;

    /**
     * @see SOAPMessageContext#containsProperty(String)
     */
    public abstract boolean containsProperty(String name);

    /**
     * @see SOAPMessageContext#getProperty(String)
     */
    public abstract Object getProperty(String name);

    /**
     * @see SOAPMessageContext#removeProperty(String)
     */
    public abstract void removeProperty(String name);

    /**
     * @see SOAPMessageContext#setProperty(String, Object)
     */
    public abstract void setProperty(String name, Object value);

    /**
     * @see SOAPMessageContext#getPropertyNames()
     */
    public abstract Iterator getPropertyNames();

}
