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

import java.io.Serializable;

import javax.security.auth.Subject;

import javax.xml.rpc.handler.MessageContext;

import org.ietf.jgss.GSSName;

import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * Interface to be implemented by client side authorization mechanism.
 */
public interface Authorization extends Serializable {

    public static final String RESOURCE =
        "org.globus.wsrf.impl.security.authorization.errors";

    public static final String AUTHORIZATION = "authorization";

    public static final String AUTHZ_CLASS = "authzClass";

    /**
     * Constants used to set authorization 
     */
    public static final String AUTHZ_NONE = "none";
    public static final String AUTHZ_SELF = "self";
    public static final String AUTHZ_GRIDMAP = "gridmap";
    public static final String AUTHZ_HOST = "host";
    public static final String AUTHZ_SAML = "samlCallout";
    public static final String AUTHZ_IDENTITY = "identity";
    public static final String AUTHZ_USERNAME = "userName";

    /**
     * Prefix used for authz
     */
    public static final String NONE_PREFIX = "noneAuthz";
    public static final String SELF_PREFIX = "selfAuthz";
    public static final String GRIDMAP_PREFIX = "gridmapAuthz";
    public static final String HOST_PREFIX = "hostAuthz";
    public static final String SAML_PREFIX = "samlAuthz";
    public static final String IDENTITY_PREFIX = "idenAuthz";
    public static final String USERNAME_PREFIX = "userNameAuthz";

    /**
     * Method invoked to authorize the call 
     */
    void authorize(Subject peerSubject,
                   MessageContext context)
        throws AuthorizationException;

    /**
     * Returns the identity of the authorized entity
     */
    GSSName getName(MessageContext ctx) throws AuthorizationException;
}
