package org.glite.security.authz;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.util.Collection;

/**
 * The <code>ServiceAuthorizationChainSpi</code> is the interface that should
 * be implemented by ServiceAuthorizationChain providers.
 * @see ServiceAuthorizationChain
 */
public interface ServiceAuthorizationChainSpi {
    /**
     * sets the parent chain, which will be evaluated before the current chain
     * all authorization, get- and setPolicy, and getPolicyNames requests are
     * propagated to the parent, wheras initialize and close are always only
     * done on the local chain.
     * @param parentChain parent chain to connect to this chain.
     */
    void engineSetParent(ServiceAuthorizationChain parentChain);
    /**
     * initializes the chain with a given configuration of PIPs and PDPs.
     * @param config configuration holding the names and classes of the
     *               ServicePDP and ServicePIP inteceptors
     * @param name name of this chain
     * @param id service id associated with this chain
     * @throws InitializeException if the chain was not initialized correctly.
     */
    void engineInitialize(ChainConfig config, String name, String id)
        throws InitializeException;
    /**
     * gets the names of the policies implemented by this engine.
     * @return array of policy names
     */
    String[] engineGetPolicyNames();
    /**
     * gets the policies of all the PDPs in this chain.
     * @param policy possible filter restricting the result of query
     * @return a Collection of Node objects returned by the PDPs
     * @throws InvalidPolicyException if an invalid policy filter was
     *                                specified
     */
    Collection engineGetPolicy(Node policy)
        throws InvalidPolicyException;
    /**
     * sets the policies of all the PDPs in this chain.
     * @param policy the new policy or an update request
     * @return a Collection of Node values returned by the PDPs
     * @throws InvalidPolicyException if an invalid policy was specified
     */
    Collection engineSetPolicy(Node policy)
        throws InvalidPolicyException;
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
    boolean engineAuthorize(Subject peerSubject,
                                   MessageContext context)
        throws AuthorizationException;
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
    boolean engineIsPermitted(Subject peerSubject,
                              MessageContext context,
                              QName operation)
            throws AuthorizationException;
    /**
     * called by the framework when the chain should be closed. That is, the
     * information sent in the init call is out of scope after this operation
     * has been invoked.
     * @throws CloseException if there was a problem closing this chain
     */
    void engineClose() throws CloseException;
}

