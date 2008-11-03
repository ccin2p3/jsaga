package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.monitoring.*;
import org.apache.log4j.Logger;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractTaskImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractTaskImpl<T,E> extends AbstractMonitorableImpl implements Task<T,E>, TaskCallback<E> {
    // metrics
    private TaskStateMetricImpl<State> m_metric_TaskState;
    // internal
    private T m_object;
    private E m_result;
    protected SagaException m_exception;
    private boolean m_isWaitingFor;
    /** logger */
    private static Logger s_logger = Logger.getLogger(AbstractTaskImpl.class);

    /** constructor */
    public AbstractTaskImpl(Session session, T object, boolean create) throws NotImplementedException {
        super(session);

        // set metrics
        m_metric_TaskState = new TaskStateMetricFactoryImpl<State>(this).createAndRegister(
                Task.TASK_STATE,
                "fires on task state change, and has the literal value of the task enum.",
                MetricMode.ReadOnly,
                "1",
                MetricType.Enum,
                create ? State.NEW : null);

        // internal
        m_object = object;
        m_result = null;
        m_exception = null;
        m_isWaitingFor = false;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractTaskImpl clone = (AbstractTaskImpl) super.clone();
        clone.m_metric_TaskState = m_metric_TaskState;
        clone.m_object = m_object;
        clone.m_result = m_result;
        clone.m_exception = m_exception;
        clone.m_isWaitingFor = m_isWaitingFor;
        return clone;
    }

    //////////////////////////////////////////// abstract methods ////////////////////////////////////////////

    /**
     * start the task (e.g. start the thread, submit the job...) and returns immediatly
     */
    protected abstract void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * cancel the task (and update its state)
     */
    protected abstract void doCancel();

    /**
     * query the task state
     * return the new state if it has been queried, else null
     */
    protected abstract State queryState() throws NotImplementedException, TimeoutException, NoSuccessException;

    /**
     * start listening to changes of task state
     * @return true if the task is listening, else false
     */
    public abstract boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * stop listening to changes of task state
     */
    public abstract void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException;

    //////////////////////////////////////////// interface Task ////////////////////////////////////////////

    public void run() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (!this.isCancelled()) {
            this.doSubmit();
        }
    }

    public void waitFor() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        this.waitFor(WAIT_FOREVER);
    }

    public boolean waitFor(float timeoutInSeconds) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        // returns immediatly if already finished
        if (this.isDone_LocalCheckOnly()) {
            return true;
        }

        // start listening
        boolean isListening = this.startListening(); //WARN: do not breakpoint here (because this may change behavior)
        this.setWaitingFor(isListening);
        // fixme: the code below avoids querying the status of finished tasks, but it makes test_simultaneousShortJob hanging with personal-gatekeeper...  :-(
        /*int cookie;
        try {
            cookie = m_metric_TaskState.addCallback(new Callback(){
                public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                    boolean stayRegistered;
                    MetricImpl<State> m = (MetricImpl<State>) metric;
                    switch(m.getValue()) {
                        case DONE:
                        case CANCELED:
                        case FAILED:
                            stayRegistered = false;
                            break;
                        default:
                            stayRegistered = true;
                            break;
                    }
                    return stayRegistered;
                }
            });
        }
        catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
        catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
        catch (PermissionDeniedException e) {throw new NoSuccessException(e);}*/

        // loop until task is finished (done, canceled or failed)
        try {
            boolean forever;
            long endTime;
            if (timeoutInSeconds == WAIT_FOREVER) {
                forever = true;
                endTime = -1;
            } else if (timeoutInSeconds == NO_WAIT) {
                forever = false;
                endTime = -1;
            } else {
                forever = false;
                endTime = System.currentTimeMillis() + (long) timeoutInSeconds*1000;
            }
            // read notified status, else query status
            while(!this.isDone() && (forever || System.currentTimeMillis()<endTime)) {
                Thread.currentThread().sleep(100);
            }
        } catch (InterruptedException e) {/*ignore*/}

        // stop listening
        this.stopListening();
        this.setWaitingFor(false);
        /*{
            // callback may have not been removed
            try {
                m_metric_TaskState.removeCallback(cookie);
            }
            catch (BadParameterException e2) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e2) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e2) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e2) {throw new NoSuccessException(e);}
        }*/

        // returns
        return this.isDone_LocalCheckOnly();
    }

    // exit immediatly
    public synchronized void cancel() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        switch(m_metric_TaskState.getValue(State.RUNNING)) {
            case NEW:
                throw new IncorrectStateException("Can not cancel task in 'New' state", this); //as specified in SAGA
            case DONE:
            case CANCELED:
            case FAILED:
                // just ignore
                break;
            case RUNNING:
                // try to cancel synchronously
                this.doCancel();
                if (!this.isDone_LocalCheckOnly()) {
                    // try to cancel asynchronously (every minutes)
                    s_logger.warn("Failed to cancel synchronously, trying asynchronously...");
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                while(!AbstractTaskImpl.this.isDone_LocalCheckOnly()) {
                                    Thread.currentThread().sleep(60000);
                                    AbstractTaskImpl.this.doCancel();
                                }
                            } catch (InterruptedException e) {/*ignore*/}
                            if (AbstractTaskImpl.this.isCancelled()) {
                                s_logger.info("Asynchronous cancel successfull !");
                            }
                        }
                    }).start();
                }
                break;
        }        
    }

    // wait for task to be cancelled (or done, or failed)
    public void cancel(float timeoutInSeconds) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        this.cancel();
        this.waitFor(timeoutInSeconds);
    }

    public State getState() throws NotImplementedException, TimeoutException, NoSuccessException {
        switch(m_metric_TaskState.getValue(State.RUNNING)) {
            case DONE:
            case CANCELED:
            case FAILED:
                return m_metric_TaskState.getValue();
            default:
                if (!m_isWaitingFor && !m_metric_TaskState.isListening()) {
                    State state = this.queryState();
                    if (state != null) {
                        this.setState(state);
                    }
                }
                return m_metric_TaskState.getValue();
        }
    }

    public E getResult() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        this.waitFor();
        switch(m_metric_TaskState.getValue(State.DONE)) {
            case NEW:
            case FAILED:
            case CANCELED:
                throw new IncorrectStateException("Can not get result for task in state: "+ m_metric_TaskState.getValue().name());
            default:
                return m_result;
        }
    }

    public T getObject() throws NotImplementedException, TimeoutException, NoSuccessException {
        return m_object;
    }

    public void rethrow() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        switch(m_metric_TaskState.getValue(State.FAILED)) {
            case FAILED:
                if (m_exception != null) {
                    try {throw m_exception;}
                    catch(NotImplementedException e) {throw e;}
                    catch(IncorrectURLException e) {throw e;}
                    catch(AuthenticationFailedException e) {throw e;}
                    catch(AuthorizationFailedException e) {throw e;}
                    catch(PermissionDeniedException e) {throw e;}
                    catch(BadParameterException e) {throw e;}
                    catch(IncorrectStateException e) {throw e;}
                    catch(AlreadyExistsException e) {throw e;}
                    catch(DoesNotExistException e) {throw e;}
                    catch(TimeoutException e) {throw e;}
                    catch(NoSuccessException e) {throw e;}
                    catch(SagaException e) {throw new NoSuccessException(m_exception);}
                } else {
                    throw new NoSuccessException("task failed with unknown reason", this);
                }
            case CANCELED:
                throw new NoSuccessException("task canceled", this);
        }
    }

    //////////////////////////////////////////// interface TaskCallback ////////////////////////////////////////////

    public synchronized void setState(State state) {
        switch(m_metric_TaskState.getValue(State.RUNNING)) {
            case DONE:
            case CANCELED:
            case FAILED:
                return;
            default:
                m_metric_TaskState.setValue(state);
        }
    }

    public synchronized void setResult(E result) {
        m_result = result;
    }

    public synchronized void setException(SagaException exception) {
        m_exception = exception;
    }

    //////////////////////////////////////////// interface Future<E> ////////////////////////////////////////////

    /**
     * Attempts to cancel execution of this task. This attempt will fail if the task has already completed,
     * already been cancelled, or could not be cancelled for some other reason. If successful, and this task
     * has not started when cancel is called, this task should never run. If the task has already started,
     * then the mayInterruptIfRunning parameter determines whether the thread executing this task should be
     * interrupted in an attempt to stop the task.
     * @param mayInterruptIfRunning true if the thread executing this task should be interrupted; otherwise,
     * in-progress tasks are allowed to complete
     * @return false if the task could not be cancelled, typically because it has already completed normally; true otherwise
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        switch(m_metric_TaskState.getValue(State.RUNNING)) {
            case NEW:
                this.setState(State.CANCELED);   //as specified in java.util.concurrent
                return true;
            case DONE:
            case CANCELED:
            case FAILED:
                return false;
            case RUNNING:
                if (mayInterruptIfRunning) {
                    this.doCancel();
                    return m_metric_TaskState.getValue() == State.CANCELED;
                }
        }
        return false;
    }

    /**
     * Returns true if this task was cancelled before it completed normally.
     * @return true if task was cancelled before it completed
     */
    public boolean isCancelled() {
        return m_metric_TaskState.getValue() == State.CANCELED;
    }

    /**
     * Returns true if this task completed. Completion may be due to normal termination, an exception,
     * or cancellation -- in all of these cases, this method will return true.
     * @return true if this task completed.
     */
    public boolean isDone() {
        State state;
        try {
            state = this.getState();
        } catch (Exception e) {
            s_logger.warn("Failed to get state", e);
            return false;
        }
        switch(state) {
            case DONE:
            case CANCELED:
            case FAILED:
                return true;
            default:
                return false;
        }
    }

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     * @return the computed result
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     */
    public E get() throws InterruptedException, ExecutionException {
        try {
            this.waitFor();
        } catch (SagaException e) {
            throw new ExecutionException(e);
        }
        switch(m_metric_TaskState.getValue(State.DONE)) {
            case DONE:
                return m_result;
            case CANCELED:
                throw new InterruptedException("Task has been cancelled");
            case FAILED:
                throw new ExecutionException(m_exception);
            default:
                throw new ExecutionException("INTERNAL ERROR: unexpected exception", m_exception);
        }
    }

    /**
     * Waits if necessary for at most the given time for the computation to complete, and then retrieves
     * its result, if available.
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     * @throws java.util.concurrent.TimeoutException if the wait timed out
     */
    public E get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException {
        try {
            this.waitFor(unit.toSeconds(timeout));
        } catch (SagaException e) {
            throw new ExecutionException(e);
        }
        switch(m_metric_TaskState.getValue(State.DONE)) {
            case DONE:
                return m_result;
            case CANCELED:
                throw new InterruptedException("Task has been cancelled");
            case FAILED:
                throw new ExecutionException(m_exception);
            default:
                throw new java.util.concurrent.TimeoutException("The wait timed out");
        }
    }

    //////////////////////////////////////////// internal methods ////////////////////////////////////////////

    void setWaitingFor(boolean isWaitingFor) {
        m_isWaitingFor = isWaitingFor;
    }

    boolean isWaitingFor() {
        return m_isWaitingFor;
    }

    protected State getState_LocalCheckOnly() {
        return m_metric_TaskState.getValue();
    }

    boolean isDone_LocalCheckOnly() {
        switch(m_metric_TaskState.getValue()) {
            case DONE:
            case CANCELED:
            case FAILED:
                return true;
            default:
                return false;
        }
    }
}
