package org.ogf.saga.monitoring;

import org.ogf.saga.SagaBase;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.ReadOnly;
import org.ogf.saga.error.Timeout;

/**
 * Metrics represent monitorable entities.
 */
public interface Metric extends SagaBase, Attributes {
    /**
     * Adds the specified callback to the metric.
     * @return the cookie that identifies the callback in the metric.
     */
    public int addCallback(Callback cb)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, Timeout, NoSuccess;

    /**
     * Removes a callback from the metric.
     * @param cookie the cookie that identifies the metric.
     */
    public void removeCallback(int cookie)
        throws NotImplemented, BadParameter, Timeout, NoSuccess;

    /**
     * Pushes the metric value to the backend.
     */
    public void fire()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, ReadOnly, Timeout, NoSuccess;
}
