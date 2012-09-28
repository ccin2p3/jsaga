package org.glite.ce.commonj.authz;

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
	public static final int NO_DECISION = 0;
	public static final int DENIED = 1;
	public static final int ALLOWED = 2;
	public static final int STRONG_DENIED = 4;
	public static final int STRONG_ALLOWED = 8;
	
    /**
     * this operation is called by the PDP Framework whenever the application
     * needs to call secured operations. 
     * @param peerSubject authenticated client subject with credentials
     *                    and attributes
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @return the permission level for that request
     * @throws AuthorizationException if a serious error occured that should
     *                                stop further evaluation
     */
    public int getPermissionLevel(Subject peerSubject,
                                                 MessageContext context,
                                                 QName operation) throws AuthorizationException;
}

