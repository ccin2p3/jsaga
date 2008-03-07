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

import org.globus.util.I18n;

import org.apache.axis.AxisFault;
import org.apache.axis.utils.XMLUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.io.Serializable;

import java.util.ArrayList;

import org.globus.wsrf.security.authorization.PIP;
import org.globus.wsrf.security.authorization.PDP;
import org.globus.wsrf.security.authorization.PDPConfig;
import org.globus.wsrf.security.authorization.Interceptor;
import org.globus.wsrf.security.authorization.PDPConstants;

import org.globus.wsrf.security.SecurityException;

import org.globus.wsrf.impl.security.util.AuthUtil;

import org.globus.wsrf.impl.security.authorization.exceptions.CloseException;
import org.globus.wsrf.impl.security.authorization.exceptions.InitializeException;
import org.globus.wsrf.impl.security.authorization.exceptions.InvalidPolicyException;
import org.globus.wsrf.impl.security.authorization.exceptions.AuthorizationException;

/**
 * The <code>ServiceAuthorizationChain</code> class ties together and
 * evaluates chains of {@link PDP} and {@link PIP}
 * implementations.  The chain is evaluated in a strict configuration
 * determined order. If any PIP or PDP throws an Exception the
 * evaluation is stopped, and the Exception is propagated back to the
 * client. If a PDP returns false the evaluation is stopped and the
 * client is notified. If a PDP returns true or a PIP returns
 * indeterminate the next interceptor in the chain is evaluated.  Chains
 * can also be linked, in which case the parent chain is evaluated
 * before the child chain. See the {@link Interceptor}
 * documentation for information about the individual operations.
 */
public class ServiceAuthorizationChain implements Interceptor, Serializable {

    private static I18n i18n =
        I18n.getI18n(PDPConstants.RESOURCE,
                     ServiceAuthorizationChain.class.getClassLoader());

    private static Log logger =
        LogFactory.getLog(ServiceAuthorizationChain.class.getName());

    protected boolean initialized;
    protected Interceptor[] interceptor;
    protected String[] interceptorName;
    private ServiceAuthorizationChain parentChain;
    private boolean chained = false;

    public ServiceAuthorizationChain() {
    }

    /**
     * sets the parent chain, which will be evaluated before the
     * current chain all authorization, get- and setPolicy, and
     * getPolicyNames requests are propagated to the parent, wheras
     * initialize and close are always only done on the local chain.
     */
    public ServiceAuthorizationChain(ServiceAuthorizationChain parentChain) {
        this.parentChain = parentChain;
    }

    /**
     * initializes the chain with a given configuration of PIPs and
     * PDPs
     * @param config configuration holding the names and classes of
     * the ServicePDP and ServicePIP inteceptors
     * @param name name of this chain
     * @param id service id associated with this chain
     */
    public synchronized void initialize(PDPConfig config, String name,
                                        String id) throws InitializeException {
        if (initialized) {
            return;
        }
        initialized = true;
        init(config);
        for (int i = 0;
             (interceptor != null) && (i < interceptor.length);
             i++) {
            this.interceptor[i].initialize(config, this.interceptorName[i], id);
        }
    }

    public String[] getPolicyNames() {
        ArrayList policies = new ArrayList();
        if (this.parentChain != null) {
            String[] parentPolicies = this.parentChain.getPolicyNames();
            for (int policy = 0;
                 (parentPolicies != null) &&
                 (policy < parentPolicies.length); policy++) {
                policies.add(parentPolicies[policy]);
            }
        }
        for (int i = 0; (interceptor != null) &&
                        (i < interceptor.length); i++) {
            if (!(interceptor[i] instanceof PDP)) {
                continue;
            }
            PDP pdp = (PDP) interceptor[i];
            String[] localPolicies = pdp.getPolicyNames();
            for (int policy = 0; (localPolicies != null) &&
                                 (policy < localPolicies.length); policy++) {
                policies.add(localPolicies[policy]);
            }
        }
        return (String[]) policies.toArray(new String[0]);
    }

    public Node getPolicy(Node policy) throws InvalidPolicyException {
        Element element = null;
        try {
            Document doc = XMLUtils.newDocument();
            element = doc.createElementNS(PDPConstants
                                          .SERVICE_AUTHORIZATION_MANAGEMENT_NS,
                                          PDPConstants.SERVICE_POLICIES_TAG);
            doc.appendChild(element);
            if (this.parentChain != null) {
                Node node = this.parentChain.getPolicy(policy);
                if (node != null) {
                    Node importedNode = doc.importNode(node, true);
                    element.appendChild(importedNode);
                }
            }
            for (int i = 0; (interceptor != null) && (i < interceptor.length);
                 i++) {
                if (!(interceptor[i] instanceof PDP)) {
                    continue;
                }
                PDP pdp = (PDP) interceptor[i];
                Node node = pdp.getPolicy(policy);
                if (node != null) {
                    Node importedNode = doc.importNode(node, true);
                    element.appendChild(importedNode);
                }
            }
        } catch (Exception e) {
            throw new InvalidPolicyException(i18n.getMessage("getPolicy"), e);
        }
        return element;
    }

    public Node setPolicy(Node policy) throws InvalidPolicyException {
        Element element = null;
        try {
            Document doc = XMLUtils.newDocument();
            element = doc.createElementNS(PDPConstants
                                          .SERVICE_AUTHORIZATION_MANAGEMENT_NS,
                                          PDPConstants.SERVICE_POLICIES_TAG);
            if (this.parentChain != null) {
                Node node = this.parentChain.setPolicy(policy);
                if (node != null) {
                    Node importedNode = doc.importNode(node, true);
                    element.appendChild(importedNode);
                }
            }
            for (int i = 0; (interceptor != null) && (i < interceptor.length);
                 i++) {
                if (!(interceptor[i] instanceof PDP)) {
                    continue;
                }
                PDP pdp = (PDP) interceptor[i];
                Node node = pdp.setPolicy(policy);
                if (node != null) {
                    element.appendChild(node);
                }
            }
        } catch (Exception e) {
            throw new InvalidPolicyException(i18n.getMessage("setPolicy"), e);
        }
        return element;
    }

    private boolean intercept(Subject peerSubject, MessageContext context,
                              QName operation)  throws AuthorizationException {
        boolean permitted = true;
        if (this.parentChain != null) {
            if (this.parentChain.authorize(peerSubject, context, operation)) {
                return true;
            }
        }
        for (int i = 0; (interceptor != null) && (i < interceptor.length);
             i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Interceptor "  +
                             interceptor[i].getClass().getName());
            }
            if (interceptor[i] instanceof PDP) {
                if (!((PDP)interceptor[i]).isPermitted(peerSubject,
                                                       context,
                                                       operation)) {
                    permitted = false;
                    break;
                }
            } else if (interceptor[i] instanceof PIP) {
                ((PIP)interceptor[i]).collectAttributes(peerSubject,
                                                        context,
                                                        operation);
            }
        }
        return permitted;
    }

    public void authorize(Subject peerSubject,
                          MessageContext context,
                          String service) throws AuthorizationException {
        authorize(peerSubject, context);
    }

    public boolean authorize(Subject peerSubject, MessageContext context)
        throws AuthorizationException {

        if (!initialized) {
            throw new AuthorizationException(i18n.getMessage("initialize"));
        }

        // Get called operation
        QName operation = null;
        try {
            // FIXME: Where is this set ? Grep shows no other use of
            // this constant.
            operation = (QName) context.getProperty(PDPConstants.ACTION);
            if (operation == null) {
                operation = AuthUtil
                    .getOperationName((org.apache.axis.MessageContext)context);
            }
        } catch (AxisFault e) {
            operation = null;
        } catch (SecurityException e) {
            operation = null;
        }

        if (operation == null) {
            throw new AuthorizationException(i18n.getMessage(
                "noTargetOperation"));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Target operation is \"" + operation.toString() +
                             "\". Called by subject \""
                             + AuthUtil.getIdentity(peerSubject) + "\"");
            }
        }

        return authorize(peerSubject, context, operation);
    }

    public boolean authorize(Subject peerSubject,
                             MessageContext context,
                             QName operation)
        throws AuthorizationException {

        String peerIdentity = AuthUtil.getIdentity(peerSubject);
        try {
            if (intercept(peerSubject, context, operation)) {
                // For now, put this here. The overall structure of things is
                // not really clear to me at this point...
                logger.info(i18n.getMessage("authorized",
                                            new Object[] { peerIdentity,
                                                           operation }));
                return true;
            } else if (this.chained) {
                return false;
            } else {
                logger.warn(i18n.getMessage("notAuthorized",
                                            new Object[] { peerIdentity,
                                                           operation }));
            }
        } catch (Exception e) {
            throw new AuthorizationException(i18n.getMessage("policyDecision"),
                                             e);
        }
        // If we reach this point, operation access was denied.
        throw new AuthorizationException
            (i18n.getMessage("notAuthorized", new Object[] { peerIdentity,
                                                             operation }));
    }

    public void setChained(boolean chained) {
        this.chained = chained;
    }

    public boolean isChained() {
        return this.chained;
    }

    private synchronized void init(PDPConfig config)
        throws InitializeException {
        InterceptorConfig[] interceptorConfig =  config.getInterceptors();
        if (interceptorConfig == null) {
            throw new InitializeException(i18n.getMessage("noInterceptors"));
        }
        this.interceptor = new Interceptor[interceptorConfig.length];
        this.interceptorName = new String[interceptorConfig.length];
        try {
            for (int i = 0; i < interceptorConfig.length; i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Trying to load: " + interceptorConfig[i]
                                                      .getInterceptorClass());
                }
                this.interceptor[i] = (Interceptor)
                    ServiceAuthorizationChain.class.getClassLoader()
                    .loadClass(interceptorConfig[i].getInterceptorClass())
                    .newInstance();
                this.interceptorName[i] = interceptorConfig[i].getName();
            }
        } catch (Exception e) {
            throw new InitializeException(i18n.getMessage("loadChain"), e);
        }
    }

    public void close() throws CloseException {
        for (int i = 0; i < interceptor.length; i++) {
            if (interceptor[i] != null) {
                interceptor[i].close();
            }
        }
    }
}

