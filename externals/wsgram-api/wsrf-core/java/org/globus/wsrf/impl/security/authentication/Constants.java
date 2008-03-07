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
package org.globus.wsrf.impl.security.authentication;

/**
 * Defines <b>internal</b> security constants.
 */
public interface Constants
    extends org.globus.wsrf.security.Constants {

    public static final String GSI_MECH_TYPE =
        "http://www.globus.org/2002/04/xmlenc#gssapi-gsi";

    /** Used by handlers only for storing
     * GSSContext-ContextId mapping
     */
    public static final String CONTEXT = "org.globus.security.context";
    public static final String TTL = "org.globus.security.timeToLive";

    /** Lifetime of the context */
    public static final String CONTEXT_LIFETIME
        = "org.globus.security.context.lifetime";

    public static final String ROUTED = "org.globus.ogsa.router";

    static final String SECURE_NOTIFICATION_FACTORY =
        "secureNotificationFactory";

    /** The invocation JAAS subject object. The appropriate
     * invocation subject is set according to the run-as mode.
     */
    public static final String INVOCATION_SUBJECT = "invocationSubject";

    /** The JAAS subject object of the caller/peer. */
    public static final String PEER_SUBJECT = "callerSubject";

    public static final String USERNAME_AUTH = "userNameAuthz";

    public static final String OPERATION_NAME =
        "org.globus.security.operationName";

    /** A MessageContext property that contains a <code>Map</code>
     * of headers to be secured (signed or encrypted).
     */
    public static final String SECURE_HEADERS =
        "org.globus.security.secure.headers";

    /**
     * A MessageContext property that contains the list of headers
     * a) that were present in the requst b) were used for dispatch
     * c) should have been secured.
     */
    public static final String ENFORCED_SECURE_HEADERS =
        "org.globus.security.secure.headers.enforced";

    /**
     * A MessageContext property that has a boolean value that determines if
     * authorization needs to be done.
     */
    public static final String AUTHZ_REQUIRED =
        "org.globus.security.authz.required";
}
