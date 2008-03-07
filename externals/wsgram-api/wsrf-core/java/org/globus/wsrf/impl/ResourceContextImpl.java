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
package org.globus.wsrf.impl;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.MessageContext;

import org.globus.util.I18n;
import org.globus.wsrf.Constants;
import org.globus.wsrf.NoResourceHomeException;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.utils.Resources;

public class ResourceContextImpl extends ResourceContext {

    private static Log logger =
        LogFactory.getLog(ResourceContextImpl.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private SOAPMessageContext context = null;
    private SOAPMessage message = null;

    private ResourceHome home = null;
    private String homeLocation = null;
    private String service = null;
    private URL serviceURL = null;

    public ResourceContextImpl() {
        this(org.apache.axis.MessageContext.getCurrentContext());
    }

    public ResourceContextImpl(SOAPMessageContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "ctx"));
        }
        this.context = ctx;
        this.message = ctx.getMessage();
    }

    public ResourceContextImpl(SOAPMessageContext ctx,
                               SOAPMessage msg) {
        if (ctx == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "ctx"));
        }
        this.context = ctx;
        this.message = msg;
    }

    public ResourceKey getResourceKey()
        throws ResourceContextException {
        ResourceHome home = getResourceHome();
        return getResourceKey(home.getKeyTypeName(),
                              home.getKeyTypeClass());
    }

    public ResourceKey getResourceKey(QName keyName, Class keyClass)
        throws ResourceContextException {
        if (keyName == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("resource key name is null");
            }
            return null;
        }
        SOAPHeaderElement headerElement = getResourceKeyHeader(keyName);
        if (headerElement == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("resource key header is null");
            }
            return null;
        }
        try {
            return new SimpleResourceKey(headerElement, keyClass);
        } catch (Exception e) {
            throw new ResourceContextException("", e);
        }
    }

    public synchronized ResourceHome getResourceHome()
        throws NoResourceHomeException, ResourceContextException {
        if (this.home == null) {
            String homeLoc = getResourceHomeLocation();
            try {
                Context initialContext = new InitialContext();
                this.home = (ResourceHome) initialContext.lookup(homeLoc);
            } catch (NameNotFoundException e) {
                throw new NoResourceHomeException();
            } catch (NamingException e) {
                throw new ResourceContextException("", e);
            }
        }
        return this.home;
    }

    public synchronized String getResourceHomeLocation() {
        if (this.homeLocation == null) {
            this.homeLocation = Constants.JNDI_SERVICES_BASE_NAME +
                getService() + Constants.HOME_NAME;
        }
        return this.homeLocation;
    }

    public SOAPHeaderElement getResourceKeyHeader()
        throws ResourceContextException {
        ResourceHome home = getResourceHome();
        QName keyName = home.getKeyTypeName();
        return (keyName == null) ? null : getResourceKeyHeader(keyName);
    }

    public SOAPHeaderElement getResourceKeyHeader(QName keyName)
        throws ResourceContextException {
        try {
            return ResourceContextImpl.getResourceKeyHeader(this.message,
                                                            keyName,
                                                            null);
        } catch (SOAPException e) {
            throw new ResourceContextException("", e);
        }
    }

    public Resource getResource()
        throws ResourceContextException, ResourceException {
        ResourceHome home = getResourceHome();
        ResourceKey key = getResourceKey(home.getKeyTypeName(),
                                         home.getKeyTypeClass());
        if (logger.isDebugEnabled()) {
            logger.debug("resource key: " + key);
        }

        Resource resource = home.find(key);

        if (logger.isDebugEnabled()) {
            logger.debug("Found resource: " + resource);
        }

        return resource;
    }

    /**
     * Sets the target service of this context.
     *
     * @param service service name.
     */
    public void setService(String service) {
        this.service = service;
        // reset in case
        synchronized(this) {
            this.home = null;
            this.homeLocation = null;
        }
    }

    /**
     * Returns target service associated with this context.
     * @return the target service that was set with
     *         {@link #setService(String) setService()}. If set to
     *         <code>null</code> or not set at all, by default it returns the
     *         target service associated with the underlying SOAP message
     *         context.
     */
    public String getService() {
        return (this.service == null) ?
               getService(this.context) : this.service;
    }

    public URL getServiceURL() {
        return (this.serviceURL == null) ?
               getServiceURL(this.context) : this.serviceURL;
    }

    public boolean containsProperty(String name) {
        return this.context.containsProperty(name);
    }

    public Object getProperty(String name) {
        return this.context.getProperty(name);
    }

    public void removeProperty(String name) {
        this.context.removeProperty(name);
    }

    public void setProperty(String name, Object value) {
        this.context.setProperty(name, value);
    }

    public Iterator getPropertyNames() {
        return this.context.getPropertyNames();
    }

    public static SOAPHeaderElement getResourceKeyHeader(SOAPMessage msg,
                                                         QName keyName,
                                                         String actorURI)
        throws SOAPException {
        if (msg == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "msg"));
        }
        if (keyName == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "keyName"));
        }

        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader header = env.getHeader();

        if (header == null) {
            return null;
        }

        Iterator iter = header.examineHeaderElements(actorURI);
        while(iter.hasNext()) {
            SOAPHeaderElement hE = (SOAPHeaderElement)iter.next();
            Name nm = hE.getElementName();

            if (nm.getLocalName().equals(keyName.getLocalPart()) &&
                nm.getURI().equals(keyName.getNamespaceURI())) {
                // found my header element;
                return hE;
            }
        }

        return null;
    }

    public static String getService(SOAPMessageContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "ctx"));
        }
        if (!(ctx instanceof org.apache.axis.MessageContext)) {
            throw new IllegalArgumentException(
                i18n.getMessage("contextNotMessageContext","ctx"));
        }

        org.apache.axis.MessageContext msgCtx =
            (org.apache.axis.MessageContext)ctx;

        return msgCtx.getTargetService();
    }

    public static URL getServiceURL(SOAPMessageContext ctx)
    {
        if (ctx == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("nullArgument", "ctx"));
        }
        if (!(ctx instanceof org.apache.axis.MessageContext)) {
            throw new IllegalArgumentException(
                i18n.getMessage("contextNotMessageContext","ctx"));
        }

        org.apache.axis.MessageContext msgCtx =
            (org.apache.axis.MessageContext)ctx;
        URL serviceURL = null;

        try
        {
            serviceURL = new URL(
                (String) msgCtx.getProperty(MessageContext.TRANS_URL));
        }
        catch(MalformedURLException e)
        {
            logger.error("", e);
        }
        return serviceURL;
    }
}
