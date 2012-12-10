package org.glite.ce.commonj.authz;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

/**
 * The <code>ServicePIP</code> interface should be implemeted by
 * interceptors that are responsible for collecting attributes
 * for subject that later on can be used by PDPs to determine
 * whether the subject is allowed to invoke the requested operation.
 * The ServicePIPs can be put into interceptor chains together with PDPs.
 * @see ServicePDP
 * @see ServiceAuthorizationChain
 */
public interface ServicePIP extends ServiceInterceptor {
    /**
     * collects attributes and populates the subject with
     * public or private credentials to be checked by subsequent
     * PDPs in the same interceptor chain.
     * @param peerSubject authenticated subject for which attributes
     *                    should be collected
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @throws AttributeException if an error occured while collecting
     *                            the attributes
     */
    void collectAttributes(Subject peerSubject,
                                 MessageContext context,
                                 QName operation) throws AuthorizationException;
}

