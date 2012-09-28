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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.w3c.dom.Node;

import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.security.Principal;

import java.util.Set;
import java.io.IOException;
import org.globus.gsi.gssapi.jaas.PasswordCredential;

/**
 * Uses Java Login module to authorize based on user name and password
 * used on the method call. The username and password are passed to
 * the Login module using <code>NameCallback</code> and 
 * <code>PasswordCallback</code>
 */
public class UsernameAuthorization implements PDP {

    // FIXME: Last parameter is service path by convention.
    public void initialize(PDPConfig config, String name, String _servicePath) 
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

        // add in configuration file
        try {
            LoginContext loginContext = 
                new LoginContext("Login", 
                                 new SimpleCallbackHandler(peerSubject));
            loginContext.login();
        } catch (LoginException exp) {
            throw new AuthorizationException("", exp);
        }
        // FIXME: when does this return false ?
        return true;
    }
}

class SimpleCallbackHandler implements CallbackHandler {

    String userName;
    char[] password;

    public SimpleCallbackHandler(Subject peerSubject) {
        Set principals = peerSubject.getPrincipals();
        if ((principals == null) || principals.isEmpty()) {
            this.userName = null;
        } else {
            this.userName = 
                ((Principal) principals.iterator().next()).getName();
        }
        Set privateCreds = 
            peerSubject.getPrivateCredentials(PasswordCredential.class);
        if ((privateCreds == null) || privateCreds.isEmpty()) {
            this.password = null;
        } else {
            this.password = 
                ((PasswordCredential) privateCreds.iterator().next())
                .getPassword().toCharArray();
        }
    }

    public void handle(Callback callbacks[]) 
        throws IOException, UnsupportedCallbackException {

        for (int i=0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback) callbacks[i]).setName(this.userName); 
            } else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback) callbacks[i]).setPassword(this.password);
            }
        }
    }
}
