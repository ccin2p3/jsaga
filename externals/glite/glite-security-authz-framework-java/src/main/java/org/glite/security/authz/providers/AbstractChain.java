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
 * The <code>AbstractChain</code> class ties together and evaluates
 * chains of {@link ServicePDP} and {@link ServicePIP} implementations.
 * The chains are evaluated in a strict configuration determined order.
 * Chains can also be linked, in which case the parent chain is
 * evaluated before the child chain. See the
 * {@link ServiceAuthorizationChainSpi} documentation for information about
 * the individual operations.
 */
abstract public class AbstractChain implements ServiceAuthorizationChainSpi {
    private static Log logger =
        LogFactory.getLog(AbstractChain.class.getName());
    private boolean initialized;
    private String[] interceptorName;
    protected ServiceAuthorizationChain parentChain;
    protected ServiceInterceptor[] interceptor;

    /**
     * gets the parent chain.
     * @return the parent chain
     */
    protected ServiceAuthorizationChain getParentChain() {
        return this.parentChain;
    }
    /**
     * gets the chain of interceptors.
     * @return the chain of interceptors
     */
    protected ServiceInterceptor[] getInterceptors() {
        return this.interceptor;
    }
    /**
     * sets the parent chain, which will be evaluated before the current chain
     * all authorization, get- and setPolicy, and getPolicyNames requests are
     * propagated to the parent, wheras initialize and close are always only
     * done on the local chain.
     * @param chain parent chain to connect to this chain.
     */
    public void engineSetParent(ServiceAuthorizationChain chain) {
        this.parentChain = chain;
    }
    /**
     * initializes the chain with a given configuration of PIPs and PDPs.
     * @param config configuration holding the names and classes of the
     *               ServicePDP and ServicePIP inteceptors
     * @param name name of this chain
     * @param id service id associated with this chain
     * @throws InitializeException if the chain was not
     *                             initialized correctly.
     */
    public synchronized void engineInitialize(ChainConfig config,
                                              String name,
                                              String id)
            throws InitializeException {
        if (initialized) {
            return;
        }
        initialized = true;
        init(config);
        for (int i = 0;
             (interceptor != null) && (i < interceptor.length);
             i++) {
            this.interceptor[i].initialize(config,
                                           this.interceptorName[i],
                                           id);
        }
    }
    /**
     * gets the names of the policies implemented by this engine.
     * @return array of policy names
     */
    public String[] engineGetPolicyNames() {
        ArrayList policies = new ArrayList();
        if (this.parentChain != null) {
            String[] parentPolicies = this.parentChain.getPolicyNames();
            for (int policy = 0;
                 (parentPolicies != null) && (policy < parentPolicies.length);
                 policy++) {
                policies.add(parentPolicies[policy]);
            }
        }
        for (int i = 0;
             (interceptor != null) && (i < interceptor.length);
             i++) {
            if (!(interceptor[i] instanceof ServicePAP)) {
                continue;
            }
            ServicePAP pap = (ServicePAP) interceptor[i];
            String[] localPolicies = pap.getPolicyNames();
            for (int policy = 0;
                 (localPolicies != null) && (policy < localPolicies.length);
                 policy++) {
                policies.add(localPolicies[policy]);
            }
        }
        return (String[]) policies.toArray(new String[0]);
    }
    /**
     * gets the policies of all the PDPs in this chain.
     * @param policy possible filter restricting the result of query
     * @return a Collection of Node objects returned by the PAPs
     * @throws InvalidPolicyException if an invalid policy filter was
     *                                specified
     */
    public Collection engineGetPolicy(Node policy)
            throws InvalidPolicyException {
        ArrayList list = new ArrayList();
        try {
            if (this.parentChain != null) {
                list.addAll(this.parentChain.getPolicy(policy));
            }
            for (int i = 0;
                 (interceptor != null) && (i < interceptor.length);
                 i++) {
                if (!(interceptor[i] instanceof ServicePAP)) {
                    continue;
                }
                ServicePAP pap = (ServicePAP) interceptor[i];
                Collection result = pap.getPolicy(policy);
                if (result != null) {
                    list.addAll(result);
                }
            }
        } catch (Exception e) {
            throw new InvalidPolicyException("getPolicy", e);
        }
        return list;
    }

    /**
     * sets the policies of all the PDPs in this chain.
     * @param policy the new policy or an update request
     * @return a Collection of Node values returned by the PDPs
     * @throws InvalidPolicyException if an invalid policy was specified
     */
    public Collection engineSetPolicy(Node policy)
            throws InvalidPolicyException {
        ArrayList list = new ArrayList();
        try {
            if (this.parentChain != null) {
                list.addAll(this.parentChain.setPolicy(policy));
            }
            for (int i = 0;
                 (interceptor != null) && (i < interceptor.length);
                 i++) {
                if (!(interceptor[i] instanceof ServicePAP)) {
                    continue;
                }
                ServicePAP pap = (ServicePAP) interceptor[i];
                Collection result = pap.setPolicy(policy);
                if (result != null) {
                    list.addAll(result);
                }
            }
        } catch (Exception e) {
            throw new InvalidPolicyException("setPolicy", e);
        }
        return list;
    }

    /**
     * traverses the chain of interceptors and implements the policy combining
     * algorithm of the chain. This operation should be overridden by the 
     * various rule combining policy implementations.
     * @param peerSubject subject to be authorized
     * @param context JAX-RPC runtime context
     * @param operation operation requested to be performed
     * @return true if peerSubject was successfully authorized, false if no
     *         PDP in the chain could make a decision
     * @throws AuthorizationException if the authorization must be denied
     *                                without further processing
     */
    abstract protected boolean traverse(Subject peerSubject,
                               MessageContext context,
                               QName operation)
            throws AuthorizationException; 
    /**
     * called by the framework if an authorization decision must be made for an
     * authenticated subject.
     * @param peerSubject subject to be authorized
     * @param context JAX-RPC runtime context
     * @return true if peerSubject was successfully authorized, false if no
     *         PDP in the chain could make a decision
     * @throws AuthorizationException if the authorization must be denied
     *                                without further processing
     */
    public boolean engineAuthorize(Subject peerSubject,
                                   MessageContext context)
            throws AuthorizationException {
        if (!initialized) {
            throw new AuthorizationException("initialize");
        }
        if (peerSubject == null) {
            throw new IllegalArgumentException("noPeerSubject");
        }

        // Get called operation
        QName operation = (QName) context.getProperty(AuthzConstants.ACTION);

        if (operation == null) {
            throw new AuthorizationException("noTargetOperation");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Target operation is \"" + operation.toString()
                             + "\". Called by subject principal \""
                             + AuthzUtil.getIdentity(peerSubject) + "\"");
            }
        }
        return engineIsPermitted(peerSubject, context, operation);
    }
    /**
     * called by the framework if an authorization decision must be made for an
     * authenticated subject.
     * @param peerSubject subject to be authorized
     * @param context JAX-RPC runtime context
     * @param operation operation requested to be performed
     * @return true if peerSubject was successfully authorized, false if no
     *         PDP in the chain could make a decision
     * @throws AuthorizationException if the authorization must be denied
     *                                without further processing
     */
    public boolean engineIsPermitted(Subject peerSubject,
                                   MessageContext context,
                                   QName operation)
            throws AuthorizationException {
        String peerIdentity = AuthzUtil.getIdentity(peerSubject);
        if (peerIdentity == null) {
            throw new AuthorizationException("anonPeer");
        }
        try {
            if (traverse(peerSubject, context, operation)) {
                return true;
            } else {
                logger.debug("notAuthorized " + peerIdentity
                             + " " + operation);
                return false;
            }
        } catch (Exception e) {
            throw new AuthorizationException("policyDecision", e);
        }
    }

    private synchronized void init(ChainConfig config)
            throws InitializeException {
        InterceptorConfig[] interceptorConfig =  config.getInterceptors();
        if (interceptorConfig == null) {
            throw new InitializeException("noInterceptors");
        }
        this.interceptor = new ServiceInterceptor[interceptorConfig.length];
        this.interceptorName = new String[interceptorConfig.length];
        try {
            for (int i = 0; i < interceptorConfig.length; i++) {
                if (interceptorConfig[i].isLoaded()) {
                    this.interceptor[i] = interceptorConfig[i].getInterceptor();
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Trying to load: "
                                 + interceptorConfig[i].getInterceptorClass());
                    }
                    this.interceptor[i] = (ServiceInterceptor)
                        ServiceAuthorizationChain.class.getClassLoader().
                            loadClass(interceptorConfig[i].
                                    getInterceptorClass()).newInstance();
                }
                this.interceptorName[i] = interceptorConfig[i].getName();
            }
        } catch (Exception e) {
            throw new InitializeException("loadChain", e);
        }
    }
    /**
     * called by the framework when the chain shoudl be closed. That is, the
     * information sent in the init call is out of scope after this operation
     * has been invoked.
     * @throws CloseException if there was a problem closing this chain
     */
    public void engineClose() throws CloseException {
        for (int i = 0; i < interceptor.length; i++) {
            if (interceptor[i] != null) {
                interceptor[i].close();
            }
        }
    }
}

