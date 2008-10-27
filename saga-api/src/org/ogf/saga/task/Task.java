package org.ogf.saga.task;

import java.util.concurrent.Future;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.monitoring.Monitorable;

/**
 * Tasks can only be created through asynchronous method calls. The generic
 * parameter <code>T</code> denotes the type of the object that generated this
 * task. Note that <code>T</code> cannot be specified as
 * <code>T extends SagaObject</code> because factories can also generate tasks
 * and factories are not Saga objects. The generic parameter <code>E</code>
 * denotes the type of the return value of the asynchronous method call.
 */
public interface Task<T, E> extends SagaObject, Monitorable, Future<E> {

    /**
     * Metric name: fires on task state change, and has the literal value of the
     * task state enumeration.
     */
    public static final String TASK_STATE = "Task.state";

    /**
     * Starts the asynchronous operation.
     */
    public void run() throws NotImplementedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Waits for the task end up in a final state. The SAGA API specifies that
     * this method is called <code>wait</code>, for Java the name needs to be
     * changed slightly.
     */
    public void waitFor() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Waits for the task to end up in a final state. The SAGA API specifies
     * that this method is called <code>wait</code>, for Java the name needs
     * to be changed slightly.
     * 
     * @param timeoutInSeconds
     *            maximum number of seconds to wait.
     * @return <code>true</code> if the task is finished within the specified
     *         time.
     */
    public boolean waitFor(float timeoutInSeconds)
            throws NotImplementedException, IncorrectStateException,
            TimeoutException, NoSuccessException;

    /**
     * Cancels the asynchronous operation. This is a non-blocking version, which
     * may continue to try and cancel the task in the background. The task state
     * will remain RUNNING until the cancel operation succeeds.
     */
    public void cancel() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Cancels the asynchronous operation. If it does not succeed to cancel the
     * task within the specified timeout, it may continue to try and cancel the
     * task in the background. The task state will remain RUNNING until the
     * cancel operation succeeds.
     * 
     * @param timeoutInSeconds
     *            maximum time for freeing resources.
     */
    public void cancel(float timeoutInSeconds) throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Gets the state of the task.
     * 
     * @return the state of the task.
     */
    public State getState() throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Obtains the result of the asynchronous method call.
     * 
     * @return the result.
     * @exception IncorrectStateException
     *                is thrown when the task is not in state DONE.
     */
    public E getResult() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Gets the object from which the task was created.
     * 
     * @return the object this task was created from.
     */
    public T getObject() throws NotImplementedException, TimeoutException,
            NoSuccessException;

    /**
     * Throws any exception a failed task caught.
     */
    public void rethrow() throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException,
            AlreadyExistsException, DoesNotExistException, TimeoutException,
            NoSuccessException;
}
