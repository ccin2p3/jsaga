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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.ietf.jgss.GSSName;

import org.w3c.dom.Node;

import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * No authorization of the peer is done.
 */
public class NoAuthorization implements Authorization, PDP {

    private static Log logger =
        LogFactory.getLog(NoAuthorization.class.getName());

    private static NoAuthorization authorization;

    public synchronized static NoAuthorization getInstance() {
        if (authorization == null) {
            authorization = new NoAuthorization();
        }
        return authorization;
    }

    public void authorize(Subject peerSubject, MessageContext context)
        throws AuthorizationException {
        logger.debug("authorize");
    }

    // FIXME: Name is service path by convention.
    public void initialize(PDPConfig config, String name, String id) 
            throws InitializeException {
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


    public boolean isPermitted(Subject peerSubject, MessageContext context, 
                               QName op) throws AuthorizationException {
        return true;
    }

    public GSSName getName(MessageContext ctx) throws AuthorizationException {
        return null;
    }
}
