package org.ogf.saga.monitoring;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NotImplementedException;

/**
 * Asynchronous handler for metric changes.
 */
public interface Callback {
    /**
     * Asynchronous handler for metric changes.
     * 
     * @param mt
     *      the SAGA Monitorable object which causes the callback
     *      invocation.
     * @param metric
     *      the metric causing the callback invocation.
     * @param ctx
     *      the context associated with the callback causing entity.
     * @return
     *      whether the callback stays registered.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception AuthorizationFailedException
     *      is thrown when the cb method decides not to authorize this
     *      particular invocation.
     */
    public boolean cb(Monitorable mt, Metric metric, Context ctx)
            throws NotImplementedException, AuthorizationFailedException;
}
