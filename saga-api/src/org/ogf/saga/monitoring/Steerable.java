package org.ogf.saga.monitoring;

import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

/**
 * The <code>Steerable</code> interface is implemented by SAGA objects that
 * can be steered (have writable metrics and/or allow to add new metrics).
 */
public interface Steerable {
    /**
     * Adds a metric instance to the application instance.
     * 
     * @param metric
     *            the metric instance to be added.
     * @return success or failure.
     */
    public boolean addMetric(Metric metric) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, AlreadyExistsException,
            TimeoutException, NoSuccessException;

    /**
     * Removes a metric instance.
     * 
     * @param name
     *            the name of the metric to be removed.
     */
    public void removeMetric(String name) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, DoesNotExistException, TimeoutException,
            NoSuccessException;

    /**
     * Pushes a new metric value to the backend.
     * 
     * @param name
     *            the name of the metric to be fired.
     */
    public void fireMetric(String name) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, IncorrectStateException,
            DoesNotExistException, TimeoutException, NoSuccessException;
}
