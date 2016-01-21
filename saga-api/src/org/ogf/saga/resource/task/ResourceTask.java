package org.ogf.saga.resource.task;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.monitoring.Monitorable;

/**
 * This interface contains part of the Resource interface.
 */
public interface ResourceTask extends SagaObject, Monitorable {

    /**
     * Metric name: fires on resource state change, and has the literal value of the
     * resource state enumeration.
     */
    public static final String RESOURCE_STATE = "resource.state";

    /**
     * Metric name: fires as a resource changes its state detail.
     */
    public static final String RESOURCE_STATEDETAIL = "resource.state_detail";

    /**
     * Gets the state of the resource
     *
     * @return
     *      the state of the resource.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public State getState() throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Waits for the resource end up in a final state. The SAGA API specifies that
     * this method is called <code>wait</code>, for Java the name needs to be
     * changed slightly.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception IncorrectStateException
     *      is thrown when the task is in NEW state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void waitFor() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Waits for the task to end up in a final state. The SAGA API specifies
     * that this method is called <code>wait</code>, for Java the name needs
     * to be changed slightly.
     *
     * @param timeoutInSeconds
     *      maximum number of seconds to wait.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out. Note that this is not thrown when the specified timeout expires.
     *      In that case, <code>false</code> is returned.
     * @exception IncorrectStateException
     *      is thrown when the task is in NEW state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void waitFor(float timeoutInSeconds)
            throws NotImplementedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Waits for the task to end up in specified state. The SAGA API specifies
     * that this method is called <code>wait</code>, for Java the name needs
     * to be changed slightly.
     *
     * @param state
     *      the state to wait for.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out. Note that this is not thrown when the specified timeout expires.
     *      In that case, <code>false</code> is returned.
     * @exception IncorrectStateException
     *      is thrown when the task is in NEW state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void waitFor(State state)
            throws NotImplementedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Waits for the task to end up in specified state. The SAGA API specifies
     * that this method is called <code>wait</code>, for Java the name needs
     * to be changed slightly.
     *
     * @param timeoutInSeconds
     *      maximum number of seconds to wait.
     * @param state
     *      the state to wait for.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out. Note that this is not thrown when the specified timeout expires.
     *      In that case, <code>false</code> is returned.
     * @exception IncorrectStateException
     *      is thrown when the task is in NEW state.
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public void waitFor(float timeoutInSeconds, State state)
            throws NotImplementedException, IncorrectStateException,
            TimeoutException, NoSuccessException;
}
