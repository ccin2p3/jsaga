package org.glite.security.authz.providers;

// TODO internationalize according to gLite standards
// import org.globus.util.I18n;

import org.glite.security.authz.ServiceAuthorizationChainSpi;
import org.glite.security.authz.ServiceAuthorizationChain;
import org.glite.security.authz.ServiceInterceptor;
import org.glite.security.authz.InitializeException;
import org.glite.security.authz.InvalidPolicyException;
import org.glite.security.authz.AuthorizationException;
import org.glite.security.authz.CloseException;
import org.glite.security.authz.AuthzUtil;
import org.glite.security.authz.AuthzConstants;
import org.glite.security.authz.ServicePAP;
import org.glite.security.authz.ServicePDP;
import org.glite.security.authz.ServicePIP;
import org.glite.security.authz.ChainConfig;
import org.glite.security.authz.InterceptorConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The <code>FirstApplicableChain</code> class ties together and evaluates
 * chains of {@link ServicePDP} and {@link ServicePIP} implementations.
 * The chain is evaluated in a strict configuration determined order. If any
 * PIP or PDP throws an Exception the evaluation is stopped, and the
 * Exception is propagated back to the client. If a PDP returns true the
 * evaluation is stopped and the operation is let through. If a PDP returns
 * false or a PIP returns normally the next interceptor in the chain is
 * evaluated. Chains can also be linked, in which case the parent chain is
 * evaluated before the child chain. See the
 * {@link ServiceAuthorizationChainSpi} documentation for information about
 * the individual operations.
 */
public class FirstApplicableChain extends AbstractChain {
    private static Log logger =
        LogFactory.getLog(FirstApplicableChain.class.getName());
    /**
     * traverses the chain of interceptors and implements the first applicable
     * policy combining algorithm. If a deny or permit decision is made by
     * a PDP the evaluation stops and the combined decision is the same as the 
     * PDP decision.
     * @param peerSubject subject to be authorized
     * @param context JAX-RPC runtime context
     * @param operation operation requested to be performed
     * @return true if peerSubject was successfully authorized, false if no
     *         PDP in the chain could make a decision
     * @throws AuthorizationException if the authorization must be denied
     *                                without further processing
     */
    protected boolean traverse(Subject peerSubject,
                               MessageContext context,
                               QName operation)
            throws AuthorizationException {
        boolean permitted = false;
        if (this.parentChain != null) {
            if (this.parentChain.isPermitted(peerSubject, context, operation)) {
                return true;
            }
        }
        for (int i = 0;
             (interceptor != null) && (i < interceptor.length);
             i++) {
            if (interceptor[i] instanceof ServicePDP) {
                if (((ServicePDP) interceptor[i]).isPermitted(peerSubject,
                                                             context,
                                                             operation)) {
                    permitted = true;
                    break;
                }
            } else if (interceptor[i] instanceof ServicePIP) {
                ((ServicePIP) interceptor[i]).collectAttributes(peerSubject,
                                                               context,
                                                               operation);
            }
        }
        return permitted;
    }
}

