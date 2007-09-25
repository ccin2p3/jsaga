package org.ogf.saga.monitoring;

import org.ogf.saga.error.AlreadyExists;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.ReadOnly;
import org.ogf.saga.error.Timeout;

/**
 * The <code>Steerable</code> interface is implemented by SAGA objects
 * that can be steered (have writable metrics and/or allow to add new metrics).
 */
public interface Steerable {
    /**
     * Adds a metric instance to the application instance.
     * @param metric the metric instance to be added.
     * @return success or failure.
     */
    public boolean addMetric(Metric metric)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, AlreadyExists, ReadOnly, Timeout, NoSuccess;

    /**
     * Removes a metric instance.
     * @param name the name of the metric to be removed.
     */
    public void removeMetric(String name)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, ReadOnly, Timeout, NoSuccess;

    /**
     * Pushes a new metric value to the backend.
     * @param name the name of the metric to be fired.
     */
    public void fireMetric(String name)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, IncorrectState, DoesNotExist, ReadOnly, Timeout,
            NoSuccess;
}
