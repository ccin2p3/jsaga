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
package org.globus.wsrf.impl.security.authentication.secureconv;

import java.net.URL;
import java.util.Map;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.AddressingHeaders;
import org.apache.axis.message.addressing.To;

import org.gridforum.jgss.ExtendedGSSContext;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import org.globus.axis.gsi.GSIConstants;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.JaasGssUtil;
import org.globus.gsi.gssapi.jaas.JaasSubject;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.secureconv.service.AuthenticationServiceConstants;
import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;
import org.globus.wsrf.impl.security.authentication.wssec.WSSecurityFault;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.security.impl.secconv.SecureConversation;
import org.globus.wsrf.security.impl.secconv.SecureConversationServiceAddressingLocator;
/**
 * A separate instance of the handler is created per service Stub instance
 * (e.g. NotificationSourcePortType, etc.) If multiple method invocations
 * are made on the same Stub instance the secure context will be reused.
 * The method invocations CANNOT be made from multiple threads using the
 * same Stub instance. Separate Stub instances must be used.
 */
public class SecContextHandler extends GenericHandler {


    protected static Log log =
        LogFactory.getLog(SecContextHandler.class.getName());

    private Authenticator authInfo;
    private String lastMode;
    private Integer lastContextLifetime;
    private boolean auto;
    private Thread lastThread;

    private static SecureConversationServiceAddressingLocator locator =
        new SecureConversationServiceAddressingLocator();

    public SecContextHandler() {
        log.debug("Enter: constructor");
    }

    public boolean handleRequest(MessageContext msgContext) {
        log.debug("Enter: invoke");

        /* since authentication loop goes through the same handlers
        * we must disable this handler during authentication
        */
        Object tmp = msgContext.getProperty(Constants.GSI_SEC_CONV);

        if ((tmp == null) || tmp.equals(Constants.NONE)) {
            // on the next call the context will be renegotiated
            authInfo = null;
            log.debug("Exit: invoke");

            return true;
        }

        // just a sanity check
        if (log.isDebugEnabled()) {
            if (lastThread == null) {
                lastThread = Thread.currentThread();
            } else {
                if (lastThread != Thread.currentThread()) {
                    throw WSSecurityFault.makeFault(
                        new Exception(
                            "Multiple threads accessing the same handler!"
                        )
                    );
                }
            }
        }

        String mode = (String) msgContext.getProperty(GSIConstants.GSI_MODE);
        if (mode == null) {
            mode = GSIConstants.GSI_MODE_NO_DELEG;
        }

        Integer contextLifetime =
            (Integer) msgContext.getProperty(Constants.CONTEXT_LIFETIME);

        // establish a new context if there isn't one or
        // if the gsi mode has changed or lifetime value has changed
        if ((authInfo == null) || (!mode.equals(lastMode)) ||
            ((contextLifetime != null) &&
             (!contextLifetime.equals(lastContextLifetime)))) {

                log.debug("Establishing new context");
                try {
                    String endpointAddress =
                        AuthUtil.getEndpointAddress(msgContext);

                    if (this.auto) {
                        endpointAddress
                        += AuthenticationServiceConstants.AUTH_SERVICE_PATH;
                    }

                    log.debug("Endpoint address is " + endpointAddress);
                    URL url = new URL(endpointAddress);

                    AddressingHeaders headers = (AddressingHeaders)
                        msgContext.getProperty(
                            org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS);
                    To savedTo = headers.getTo();
                    headers.setTo(new To(endpointAddress));

                    SecureConversation authPort =
                        locator.getSecureConversationPort(url);
                    ((Stub)authPort)._setProperty(
                        org.apache.axis.message.addressing.Constants.ENV_ADDRESSING_REQUEST_HEADERS, headers);

                    // Use credentials set in message context. If not
                    // present, use credentials associated with the thread.
                    GSSCredential gssCred = AuthUtil.getCredential(msgContext);
                    if (gssCred == null) {
                        Subject subject = JaasSubject.getCurrentSubject();
                        if (subject != null) {
                            log.debug("Get credentials assocaited with "
                                      + "thread");
                            gssCred = JaasGssUtil.getCredential(subject);
                        }
                    }

                    int lifetime = GSSContext.DEFAULT_LIFETIME;
                    if (contextLifetime != null) {
                        lifetime = contextLifetime.intValue();
                    }
                    log.debug("Lifetime is " + lifetime);
                    GSSManager manager = ExtendedGSSManager.getInstance();

                    boolean anonymous = false;
                    Object anon =
                        msgContext.getProperty(Constants.GSI_ANONYMOUS);

                    if (anon == null) {
                        anon = msgContext.getProperty(
                            Constants.GSI_SEC_CONV_ANON);
                    }

                    if ((anon!= null) && (anon.equals(Boolean.TRUE))) {
                        log.debug("Anonymous is true");
                        anonymous = true;
                    }

                    GSSName name = null;
                    if (anonymous) {
                        name = manager.createName((String)null,
                                                  (Oid)null);
                        gssCred = manager.createCredential(
                            name,
                            GSSCredential.DEFAULT_LIFETIME,
                            (Oid)null,
                            GSSCredential.INITIATE_ONLY);
                    }

                    // Get configured authz
                    Authorization author =
                        AuthUtil.getClientAuthorization(msgContext);

                    // Default to host
                    if (author == null) {
                        author = HostAuthorization.getInstance();
                    }

                    // If its self, then get default credential if
                    // no credential was configured.
                    log.debug("Authz is " + author.getClass().getName());
                    if (author instanceof SelfAuthorization) {
                        if ((!anonymous) && (gssCred == null)) {
                            gssCred = manager.createCredential(
                                            GSSCredential.INITIATE_ONLY);
                        }
                        name = gssCred.getName();
                    } else {
                        // get target name.
                        name = author.getName(msgContext);
                    }

                    ((Stub) authPort)._setProperty(Constants.AUTHORIZATION,
                                                   author);

                    ExtendedGSSContext context =
                        (ExtendedGSSContext) manager.createContext(
                            name,
                            GSSConstants.MECH_OID, gssCred,
                            lifetime
                        );

                    context.requestConf(true);

                    if (anonymous) {
                        log.debug("Setting anonyumous true");
                        context.requestAnonymity(true);
                    }

                    log.debug("Delegation mode: " + mode);

                    if (mode.equalsIgnoreCase(GSIConstants.GSI_MODE_LIMITED_DELEG)) {
                        context.setOption(
                            GSSConstants.DELEGATION_TYPE,
                            GSIConstants.DELEGATION_TYPE_LIMITED
                        );
                        context.requestCredDeleg(true);
                    } else if (
                        mode.equalsIgnoreCase(GSIConstants.GSI_MODE_FULL_DELEG)
                    ) {
                        context.setOption(
                            GSSConstants.DELEGATION_TYPE,
                            GSIConstants.DELEGATION_TYPE_FULL
                        );
                        context.requestCredDeleg(true);
                    } else if (
                        mode.equalsIgnoreCase(GSIConstants.GSI_MODE_NO_DELEG)
                    ) {
                        context.requestCredDeleg(false);
                    } else {
                        throw new Exception("Invalid GSI MODE: " + mode);
                    }

                    this.lastMode = mode;
                    this.lastContextLifetime = contextLifetime;
                    authInfo = new Authenticator(context);

                    // performs authentication loop
                    authInfo.authenticate(authPort);

                    headers.setTo(savedTo);
                    log.debug("Context established");
                } catch (Exception e) {
                    // reset it in case something failed
                    authInfo = null;
                    log.debug("Failed to establish security context", e);
                    throw WSSecurityFault.makeFault(e);
                }
            } else {
                log.debug("Reusing existing context");
            }

        // set context property
        SecurityContext secContext =
            new SecurityContext(authInfo.getContext(),
                                authInfo.getContextId());
        msgContext.setProperty(Constants.CONTEXT, secContext);

        log.debug("Exit: invoke");
        return true;
    }

    public void init(HandlerInfo config) {
        if (config == null) {
            return;
        }

        Map options = config.getHandlerConfig();

        if (options == null) {
            return;
        }

        String type = (String) options.get("authService");
        this.auto = ((type != null) && type.equals("auto"));
    }

    public QName[] getHeaders() {
        return null;
    }
}
