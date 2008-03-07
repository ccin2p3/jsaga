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
package org.globus.wsrf.impl.security.authorization;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;

import org.globus.wsrf.impl.security.util.AuthUtil;

import org.globus.util.I18n;

import org.gridforum.jgss.ExtendedGSSManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;

import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * Performs host based authorization and implements {@link PDP}
 * and {@link Authorization} interface.
 */
public class HostAuthorization implements PDP, Authorization {

    private static I18n i18n =
        I18n.getI18n(
            Authorization.RESOURCE, HostAuthorization.class.getClassLoader()
        );
    private static Log logger =
        LogFactory.getLog(HostAuthorization.class.getName());

    GSSName expected = null;
    private String service = "host";
    private PDPConfig pdpConfig = null;
    private String namePrefix = null;

    public static final String URL_PROPERTY = "url";
    public static final String SERVICE_PROPERTY = "service";

    public HostAuthorization() {
    }

    public HostAuthorization(String service) {
        if (service == null) {
            this.service = service;
        }
    }

    /**
     * Returns an instance of host authentication.
     *
     * @return an instance of this class initialized with
     *         <i>host</i> as a service.
     */
    public synchronized static HostAuthorization getInstance() {
            return new HostAuthorization();
    }

    // FIXME: Last parameter is service path by convention
    public void initialize(PDPConfig config, String name, String _servicePath)
            throws InitializeException {
        this.pdpConfig = config;
        this.namePrefix = name;
    }

    // FIXME
    public String[] getPolicyNames() {
        return null;
    }

    // FIXME
    public Node getPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }

    // FIXME
    public Node setPolicy(Node policy) throws InvalidPolicyException {
        return null;
    }

    public void close() throws CloseException {
    }

    /**
     * Does host based authorization of the client. It reads the
     * property  <i>url</i> from the configured PDPConfig and does
     * authorization based on the host name.
     */
    public boolean isPermitted(Subject peerSubject, MessageContext context,
                               QName op) throws AuthorizationException {

        // Get URL
        URL url = (URL)this.pdpConfig.getProperty(this.namePrefix,
                                                  URL_PROPERTY);
        if (logger.isDebugEnabled()) {
            logger.debug("URL " + url);
        }

        if (url == null) {
            logger.debug(i18n.getMessage("hostNull"));
            throw new AuthorizationException(i18n.getMessage("hostNull"));
        }

        String serviceProp =
            (String)this.pdpConfig.getProperty(this.namePrefix,
                                               SERVICE_PROPERTY);
        if (serviceProp != null) {
            this.service = serviceProp;
        }

        this.expected = getName(url);
        String peerIdentity = AuthUtil.getIdentity(peerSubject);
        return authorize(peerIdentity);
    }

    /**
     * Does host based authorization of the service. The host name is
     * picked up  from the <code>MessageContext</code>
     */
    public void authorize(Subject peerSubject, MessageContext context)
        throws AuthorizationException {

        this.expected = getName(context);
        String peerIdentity = AuthUtil.getIdentity(peerSubject);

        if (!authorize(peerIdentity)) {
            logger.warn(i18n.getMessage(
                "hostAuthFail",
                new Object[] { this.expected, peerIdentity }));
            throw new AuthorizationException(i18n.getMessage(
                "hostAuthFail",
                new Object[] { this.expected, peerIdentity }));
        }
    }

    private boolean authorize(String peerIdentity)
        throws AuthorizationException {

        if (peerIdentity == null) {
            logger.debug(i18n.getMessage("anonPeer"));
            throw new AuthorizationException(i18n.getMessage("anonPeer"));
        }

        try {
            GSSName target =
                ExtendedGSSManager.getInstance().createName(peerIdentity,
                                                            null);
            if (!this.expected.equals(target)) {
                logger.debug( i18n.getMessage("hostAuthFail", new Object[]
                    { this.expected, peerIdentity }));
                return false;
            }
        }  catch (GSSException e) {
            throw new AuthorizationException(i18n.getMessage("authFail"), e);
        }

        return true;
    }

    public GSSName getName(MessageContext ctx) throws AuthorizationException {

        URL endpoint = null;
        try {
            endpoint = AuthUtil.getEndpointAddressURL(ctx);
        } catch (MalformedURLException e) {
            throw new AuthorizationException(i18n.getMessage("authFail"), e);
        }
        return getName(endpoint);

    }

    public GSSName getName(URL endpoint) throws AuthorizationException {

        GSSName expected = null;
        try {
            GSSManager manager = ExtendedGSSManager.getInstance();
            expected = manager.createName(this.service + "@" +
                                          endpoint.getHost(),
                                          GSSName.NT_HOSTBASED_SERVICE);

        }  catch (GSSException e) {
            throw new AuthorizationException(i18n.getMessage("authFail"), e);
        }
        return expected;
    }
}
