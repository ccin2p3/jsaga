package org.glite.security.authz;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.util.Collection;

/**
 * Interface that must be implemented by all PAPs in an interceptor chain
 * A PAP is responsible for setting and retrieving policies to clients
 * (typically services or PEPs)
 * {@link ServiceInterceptor} through the id parameter.
 * @see ServiceAuthorizationChain
 * @see ServicePIP
 * @see ServicePDP
 */
public interface ServicePAP extends ServiceInterceptor {
    /**
     * gets the names (typically uris) of all the policies that
     * the PDP supports.
     * @return array of policy names
     */
    String[] getPolicyNames();

    /**
     * gets the current policy of the PDP.
     * @param query may be used to query for a subset of a policy
     * @return a collection of Nodes (the policy)
     * @throws InvalidPolicyException if an invalid policy was detected
     */
    Collection getPolicy(Node query) throws InvalidPolicyException;

    /**
     * sets the current policy of the PDP.
     * @param policy new policy
     * @return optional collection of Nodes (set policy result)
     * @throws InvalidPolicyException if an invalid policy was passed in
     */
    Collection setPolicy(Node policy) throws InvalidPolicyException;
}

