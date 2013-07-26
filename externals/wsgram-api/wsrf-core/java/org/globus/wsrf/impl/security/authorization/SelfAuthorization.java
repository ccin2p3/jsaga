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

import org.ietf.jgss.GSSName;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.security.SecurityException;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceContextException;

import org.globus.axis.gsi.GSIConstants;

import org.gridforum.jgss.ExtendedGSSManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;
import org.globus.gsi.gssapi.JaasGssUtil;
import org.globus.gsi.gssapi.jaas.JaasSubject;

import org.w3c.dom.Node;

import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

import org.globus.util.I18n;

/**
 * Enforces that the client and server have the same identity.
 */
public class SelfAuthorization extends BasicSubjectAuthorization
    implements Authorization, PDP {

    private static Log logger =
        LogFactory.getLog(SelfAuthorization.class.getName());
    String servicePath = null;
    
    static I18n i18n =
        I18n.getI18n(Authorization.RESOURCE, 
                     SelfAuthorization.class.getClassLoader()
        );

    // Just so all references to this need not be changed.
    public static SelfAuthorization getInstance() {
        return new SelfAuthorization();
    }

    // FIXME: Last parameter is service path by convention.
    public void initialize(PDPConfig config, String name, String _servicePath) 
            throws InitializeException {
        this.servicePath = _servicePath;
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
        servicePath = null;
    }


    public boolean isPermitted(Subject peerSubject, MessageContext context, 
                               QName op) throws AuthorizationException {

        Subject serviceSubject = null;
        try {
            Resource resource =  null;
            try {
                org.apache.axis.MessageContext msgCtx = 
                    (org.apache.axis.MessageContext)context;
                ResourceContext resContext = 
                    ResourceContext.getResourceContext(msgCtx);
                resource = resContext.getResource();
            }catch (ResourceContextException exp) {
                // FIXME: quiet catch. need to have more specific exceptions
                // from core.
                logger.debug("Error retrieving resource", exp);
            }catch (ResourceException exp) {
                // FIXME: quiet catch. need to have more specific exceptions
                // from core.
                logger.debug("Error retrieving resource", exp);
            }
            SecurityManager manager = SecurityManager
                .getManager((org.apache.axis.MessageContext)context);
            serviceSubject = manager.getSubject(this.servicePath, resource);
        } catch (SecurityException e) {
            throw new AuthorizationException(i18n.getMessage("authFail"), e);
        }
        return authorize(serviceSubject, peerSubject, context);
    }
    
    public void authorize(Subject peerSubject, MessageContext context) 
        throws AuthorizationException {

        Subject localSubject = null;
        try {
            localSubject = getLocalSubject(context);
        } catch (GSSException e) {
            throw new AuthorizationException(i18n.getMessage("authFail"), e);
        }
        
        if (!authorize(localSubject, peerSubject, context)) {
            throw new AuthorizationException(i18n
                                             .getMessage("authFail"));
        }
    }

    public Subject getLocalSubject() throws GSSException {
        return getLocalSubject(null);
    }

    public Subject getLocalSubject(MessageContext context)
        throws GSSException {

        GSSCredential cred = null;

        if (context != null) {
            cred =
                (GSSCredential) context.getProperty(
                    GSIConstants.GSI_CREDENTIALS
                );

            if (cred != null) {
                logger.debug("Getting subject from context property");
                return JaasGssUtil.createSubject(cred);
            }
        }

        Subject subject = JaasSubject.getCurrentSubject();

        if (subject != null) {
            return subject;
        }
        
        // create the default subject
        GSSManager manager = ExtendedGSSManager.getInstance();
        cred = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
        logger.debug("Getting subject from default credential");

        return JaasGssUtil.createSubject(cred);
    }

    // FIXME
    public GSSName getName(MessageContext ctx) throws AuthorizationException {
        return null;
    }
}
