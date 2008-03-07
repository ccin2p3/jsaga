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

import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.gsi.jaas.GlobusPrincipal;

import org.gridforum.jgss.ExtendedGSSManager;

import javax.security.auth.Subject;

import org.globus.util.I18n;

import java.security.Principal;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;

import java.util.Set;

import org.w3c.dom.Node;

import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * Does identity authorization and and implements {@link PDP}
 * and {@link Authorization} interface. 
 */
public class IdentityAuthorization extends BasicSubjectAuthorization
    implements Authorization, PDP {
    protected Subject subject;

    static final String IDENTITY_PROP = "identity";

    static I18n i18n =
        I18n.getI18n(Authorization.RESOURCE, 
                     IdentityAuthorization.class.getClassLoader()
        );

    public IdentityAuthorization() {
    }

    public IdentityAuthorization(Subject subject_) {
        this.subject = subject_;
    }

    public IdentityAuthorization(String globusIdentity) {
        this.subject = new Subject();
        this.subject.getPrincipals().add(new GlobusPrincipal(globusIdentity));
    }

    public void initialize(PDPConfig config, String name, String id) 
            throws InitializeException {

        if (this.subject == null) {
            if (config == null) {
                throw new InitializeException(i18n.getMessage("pdpConfigReq"));
            }
            
            String identity = 
                (String) config.getProperty(Authorization.IDENTITY_PREFIX, 
                                            IDENTITY_PROP);
            
            if (identity == null) {
                throw new InitializeException(i18n.getMessage("idenitytNull"));
            }
            this.subject = new Subject();
            this.subject.getPrincipals().add(new GlobusPrincipal(identity));
        }
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
     * Does identity authorization of the client.The expected identity can be
     * set by a) using contructor by passing an identity/subject b) 
     * configuring a property <i>idenAuthz-identity</i>in the
     * PDPConfig  object passed. If the PDPConfig implementation used is 
     * ServicePropertiesPDPConfig, then the
     * property needs to be set in service deployment descriptor, if
     * ResourcePDPConfig is used, then the property needs to be
     * populated in the hashmap in that class, if ContainerPDPConfig is
     * used then the property needs to be set as a global parameter in the
     * deployment descriptor.
     */
    public boolean isPermitted(Subject peerSubject, MessageContext context, 
                               QName op) throws AuthorizationException {
        
        return authorize(this.subject, peerSubject, context);
    }

    /** 
     * Does identity authorization of the server. The identity is set
     * by passing the value in the constructor.
     */
    public void authorize(Subject peerSubject, MessageContext context) 
        throws AuthorizationException {
        if (!authorize(this.subject, peerSubject, context)) {
            throw new AuthorizationException(i18n
                                             .getMessage("authFail"));
        }
    }

    public String getIdentity() {
        return AuthUtil.getIdentity(this.subject);
    }

    public GSSName getName(MessageContext ctx) throws AuthorizationException {

        if (this.subject == null) {
            throw new IllegalArgumentException(
                i18n.getMessage("noLocalSubject")
            );
        }

        try {
            Set principals = this.subject.getPrincipals();

            if ((principals == null) || principals.isEmpty()) {
                return null;
            }

        String identity = ((Principal) principals.iterator().next()).getName();
        GSSManager manager = ExtendedGSSManager.getInstance();
        return manager.createName(identity, null);

        }  catch (GSSException e) {
            throw new AuthorizationException(i18n.getMessage("authFail"), e);
        }
    }
}
