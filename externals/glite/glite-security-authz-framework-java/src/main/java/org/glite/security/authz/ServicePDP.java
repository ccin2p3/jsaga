package org.glite.security.authz;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

/**
 * Interface that must be implemented by all PDPs in an interceptor chain
 * A PDP is responsible for making decisions whether a subject is
 * allowed to invoke a certain operation. The subject may contain public
 * or private credentials holding attributes collected and verified by PIPs.
 * A PDP is also responsible for managing a policy associated with a service.
 * The service is associated with the PDP in the initialize call in
 * {@link ServiceInterceptor} through the id parameter.
 * @see ServiceAuthorizationChain
 * @see ServicePIP
 */
public interface ServicePDP extends ServiceInterceptor {
    /**
     * this operation is called by the PDP Framework whenever the application
     * needs to call secured operations. The PDP should return true if the
     * local policy allows the subject to invoke the operation. If the PDP
     * has no local knowledge about whether the operation is allowed or not
     * it should return false to allow other PDPs and PIPs in the chain to
     * continue the evaluation. Obligations to be read by other PIPs or PDPs
     * may be set as attributes in the Subject credentials.
     * @param peerSubject authenticated client subject with credentials
     *                    and attributes
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @return true if operation is permitted, false if outcome can not
     *         be decided
     * @throws AuthorizationException if a serious error occured that should
     *                                stop further evaluation
     */
    boolean isPermitted(Subject peerSubject,
                               MessageContext context,
                               QName operation) throws AuthorizationException;
}

