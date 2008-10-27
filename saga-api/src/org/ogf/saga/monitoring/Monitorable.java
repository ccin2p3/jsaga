package org.ogf.saga.monitoring;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

/**
 * The <code>Monitorable</code> interface is implemented by SAGA objects that
 * can be monitored (have one or more associated metrics).
 */
public interface Monitorable {

    /**
     * Lists all metrics associated with the object.
     * 
     * @return the names identifying the metrics associated with the object.
     */
    public String[] listMetrics() throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;

    /**
     * Returns a metric instance, identified by name.
     * 
     * @param name
     *            the name of the metric to be returned.
     * @return the metric.
     */
    public Metric getMetric(String name) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Adds a callback to the specified metric.
     * 
     * @param name
     *            identifier the metric to which the callback is to be added.
     * @param cb
     *            the callback to be added.
     * @return a handle to be used for removal of the callback.
     */
    public int addCallback(String name, Callback cb)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            DoesNotExistException, TimeoutException, NoSuccessException,
            IncorrectStateException;

    /**
     * Removes the specified callback.
     * 
     * @param name
     *            identifier the metric from which the callback is to be
     *            removed.
     * @param cookie
     *            identifies the callback to be removed.
     */
    public void removeCallback(String name, int cookie)
            throws NotImplementedException, DoesNotExistException,
            BadParameterException, TimeoutException, NoSuccessException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException;
}
