package org.ogf.saga.session;

import org.ogf.saga.SagaObject;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NotImplemented;

/**
 * A session isolates independent sets of SAGA objects from each other,
 * and support management of security contexts.
 */
public interface Session extends SagaObject {
    /**
     * Attaches a deep copy of the specified security context to the session.
     * @param context the context to be added.
     */
    public void addContext(Context context) throws NotImplemented;

    /**
     * Detaches the specified security context from the session.
     * @param context the context to be removed.
     * @exception DoesNotExist is thrown when the session does not
     *     contain the specified context.
     */
    public void removeContext(Context context) throws NotImplemented,
           DoesNotExist;

    /**
     * Retrieves all contexts attached to the session.
     * An empty array is returned if no context is attached.
     * @return a list of contexts.
     */
    public Context[] listContexts() throws NotImplemented;

    /**
     * Closes a SAGA session. Deviation from the SAGA API, which does not
     * have this method. However, middleware may for instance have threads
     * which may need to be terminated, or the application will hang.
     * This may not be the right place for it, but there is no other place ...
     */
    public void close() throws NotImplemented;

    /**
     * Closes a SAGA session. Deviation from the SAGA API, which does not
     * have this method. However, middleware may for instance have threads
     * which may need to be terminated, or the application will hang.
     * @param timeoutInSeconds the timeout in seconds.
     */
    public void close(float timeoutInSeconds) throws NotImplemented;
}
