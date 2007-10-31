package org.ogf.saga.monitoring;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

/**
 * The <code>Monitorable</code> interface is implemented by SAGA objects
 * that can be monitored (have one or more associated metrics).
 */
public interface Monitorable {

    /**
     * Lists all metrics associated with the object.
     * @return the names identifying the metrics associated with the object.
     */
    public String[] listMetrics()
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, Timeout, NoSuccess;

    /**
     * Returns a metric instance, identified by name.
     * @param name the name of the metric to be returned.
     * @return the metric.
     */
    public Metric getMetric(String name)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Adds a callback to the specified metric.
     * @param name identifier the metric to which the callback is to be added.
     * @param cb the callback to be added.
     * @return a handle to be used for removal of the callback.
     */
    public int addCallback(String name, Callback cb)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed,
            PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Removes the specified callback.
     * @param name identifier the metric from which the callback is to be
     *     removed.
     * @param cookie identifies the callback to be removed.
     */
    public void removeCallback(String name, int cookie)
        throws NotImplemented, DoesNotExist, BadParameter, Timeout, NoSuccess,
            AuthenticationFailed, AuthorizationFailed, PermissionDenied;
}
