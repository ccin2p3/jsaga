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
 * The <code>PermitOverridesChain</code> class ties together and evaluates
 * chains of {@link ServicePDP} and {@link ServicePIP} implementations.
 * The chain is evaluated in a strict configuration determined order. If any
 * PIP throws an Exception or a PDP returns true the evaluation is stopped.
 * The PIP Exception is propagated back to the client. If a PDP throws an
 * Authorization Exception or returns false evaluation continues
 * with the next interceptor in the chain. If at least one
 * PDP throws an Authorization exception and the rest return false the chain 
 * result is deny and the exception will be propagated back to the client. 
 * Chains can also be linked, in which case the parent chain is
 * evaluated before the child chain. See the
 * {@link ServiceAuthorizationChainSpi} documentation for information about
 * the individual operations.
 */
public class PermitOverridesChain extends AbstractChain {
    private static Log logger =
        LogFactory.getLog(PermitOverridesChain.class.getName());
    /**
     * traverses the chain of interceptors and implements the permit overrides
     * policy combining algorithm. If a permit decision is made by
     * a PDP the evaluation stops and the combined decision is permit. If
     * at least one pdp throws an Authorization exception and all the others
     * return false the combined decision is deny, and the exception is
     * propagated back to the caller.
     * @param peerSubject subject to be authorized
     * @param context JAX-RPC runtime context
     * @param operation operation requested to be performed
     * @return true if peerSubject was successfully authorized, false if no
     *         PDP in the chain could make a decision
     * @throws AuthorizationException if the authorization must be denied
     */
    protected boolean traverse(Subject peerSubject,
                               MessageContext context,
                               QName operation)
            throws AuthorizationException {
        AuthorizationException authzException = null;
        if (this.parentChain != null) {
            try {
                if (this.parentChain.isPermitted(peerSubject, 
                                                 context, 
                                                 operation)) {
                    return true;
                }
            } catch (AuthorizationException ae) {
              authzException = ae;
            }
        }
        for (int i = 0;
             (interceptor != null) && (i < interceptor.length);
             i++) {
            if (interceptor[i] instanceof ServicePDP) {
                try {
                    if (((ServicePDP) interceptor[i]).isPermitted(peerSubject,
                                                                  context,
                                                                  operation)) {
                        return true;
                    }
                } catch (AuthorizationException ae) {
                    authzException = ae;
                }
            } else if (interceptor[i] instanceof ServicePIP) {
                ((ServicePIP) interceptor[i]).collectAttributes(peerSubject,
                                                               context,
                                                               operation);
            }
        }
        if (authzException != null) {
            throw authzException;
        }
        return false;
    }
}

