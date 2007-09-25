package org.ogf.saga.session;

import java.util.List;

import org.ogf.saga.SagaBase;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.DoesNotExist;

/**
 * A session isolates independent sets of SAGA objects from each other,
 * and support management of security contexts.
 */
public interface Session extends SagaBase {
    /**
     * Attaches a deep copy of the specified security context to the session.
     * @param context the context to be added.
     */
    public void addContext(Context context);

    /**
     * Detaches the specified security context from the session.
     * @param context the context to be removed.
     * @exception DoesNotExist is thrown when the session does not
     *     contain the specified context.
     */
    public void removeContext(Context context) throws DoesNotExist;

    /**
     * Retrieves all contexts attached to the session.
     * @return a list of contexts.
     */
    public List<Context> listContexts();

    /**
     * Closes a SAGA session. Deviation from the SAGA API, which does not
     * have this method. However, middleware may for instance have threads
     * which may need to be terminated, or the application will hang.
     */
    public void close();

    /**
     * Closes a SAGA session. Deviation from the SAGA API, which does not
     * have this method. However, middleware may for instance have threads
     * which may need to be terminated, or the application will hang.
     * @param timeoutInSeconds the timeout in seconds.
     */
    public void close(float timeoutInSeconds);
}
