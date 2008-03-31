package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.monitoring.*;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

import java.lang.Exception;
import java.util.concurrent.*;

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
public abstract class AbstractTaskImpl<E> extends AbstractMonitorableImpl implements Task<E>, TaskCallback<E> {
    // metrics
    private MetricImpl<State> m_metric_TaskState;
    // internal
    private Object m_object;
    private E m_result;
    private org.ogf.saga.error.Exception m_exception;

    /** constructor */
    public AbstractTaskImpl(Session session, Object object, boolean create) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session);

        // set metrics
        m_metric_TaskState = this._addMetric(new MetricImpl<State>(
                Task.TASK_STATE,
                "fires on task state change, and has the literal value of the task enum.",
                MetricMode.ReadOnly,
                "1",
                MetricType.Enum,
                create ? State.NEW : null));

        // internal
        m_object = (object!=null ? object : this);
        m_result = null;
        m_exception = null;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractTaskImpl clone = (AbstractTaskImpl) super.clone();
        clone.m_metric_TaskState = m_metric_TaskState;
        clone.m_object = m_object;
        clone.m_result = m_result;
        clone.m_exception = m_exception;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.TASK;
    }

    //////////////////////////////////////////// abstract methods ////////////////////////////////////////////

    /**
     * start the task (e.g. start the thread, submit the job...) and returns immediatly
     */
    protected abstract void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * cancel the task
     * @return true if the task has been synchronously cancelled, else false
     */
    protected abstract boolean doCancel();

    /**
     * query the task state
     * return the new state if it has been queried, else null
     */
    protected abstract State queryState() throws NotImplemented, Timeout, NoSuccess;

    /**
     * start listening to value changes of the metric <code>metric</code>
     * @param metric the metric to monitor
     * @return true if the task is listening, else false
     */
    public abstract boolean startListening(Metric metric) throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    /**
     * stop listening to value changes of the metric <code>metric</code>
     * @param metric the monitored metric
     */
    public abstract void stopListening(Metric metric) throws NotImplemented, Timeout, NoSuccess;

    //////////////////////////////////////////// interface Task ////////////////////////////////////////////

    public void run() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        if (!this.isCancelled()) {
            this.doSubmit();
        }
    }

    public void waitFor() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        this.waitFor(WAIT_FOREVER);
    }

    public boolean waitFor(float timeoutInSeconds) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        this.startListening(m_metric_TaskState); //WARN: do not breakpoint here (because this may change behavior)
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
        this.stopListening(m_metric_TaskState);
        return this.isDone();
    }

    // exit immediatly
    public synchronized void cancel() throws NotImplemented, IncorrectState, NoSuccess {
        switch(m_metric_TaskState.getValue(State.RUNNING)) {
            case NEW:
                throw new IncorrectState("Can not cancel task in 'New' state", this); //as specified in SAGA
            case DONE:
            case CANCELED:
            case FAILED:
                // just ignore
                break;
            case RUNNING:
                new Thread(new Runnable() {
                    public void run() {
                        if (AbstractTaskImpl.this.doCancel()) {
                            m_metric_TaskState.setValue(State.CANCELED, AbstractTaskImpl.this);
                        }
                    }
                }).start();
                break;
        }        
    }

    // wait for task to be cancelled (or done, or failed)
    public void cancel(float timeoutInSeconds) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        this.cancel();
        this.waitFor(timeoutInSeconds);
    }

    public State getState() throws NotImplemented, Timeout, NoSuccess {
        switch(m_metric_TaskState.getValue(State.RUNNING)) {
            case DONE:
            case CANCELED:
            case FAILED:
                return m_metric_TaskState.getValue();
            default:
                if (!m_metric_TaskState.isListening()) {
                    State state = this.queryState();
                    if (state != null) {
                        m_metric_TaskState.setValue(state, this);
                    }
                }
                return m_metric_TaskState.getValue();
        }
    }

    public E getResult() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        this.waitFor();
        switch(m_metric_TaskState.getValue(State.DONE)) {
            case NEW:
            case FAILED:
            case CANCELED:
                throw new IncorrectState("Can not get result for task in state: "+ m_metric_TaskState.getValue().name());
            default:
                return m_result;
        }
    }

    public <T> T getObject() throws NotImplemented, Timeout, NoSuccess {
        return (T) m_object;
    }

    public void rethrow() throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        switch(m_metric_TaskState.getValue(State.FAILED)) {
            case FAILED:
                if (m_exception != null) {
                    try {throw m_exception;}
                    catch(NotImplemented e) {throw e;}
                    catch(IncorrectURL e) {throw e;}
                    catch(AuthenticationFailed e) {throw e;}
                    catch(AuthorizationFailed e) {throw e;}
                    catch(PermissionDenied e) {throw e;}
                    catch(BadParameter e) {throw e;}
                    catch(IncorrectState e) {throw e;}
                    catch(AlreadyExists e) {throw e;}
                    catch(DoesNotExist e) {throw e;}
                    catch(Timeout e) {throw e;}
                    catch(NoSuccess e) {throw e;}
                    catch(org.ogf.saga.error.Exception e) {throw new NoSuccess(m_exception);}
                } else {
                    throw new NoSuccess("task failed with unknown reason", this);
                }
            case CANCELED:
                throw new NoSuccess("task canceled", this);
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
                m_metric_TaskState.setValue(state, this);
        }
    }

    public synchronized void setResult(E result) {
        m_result = result;
    }

    public synchronized void setException(org.ogf.saga.error.Exception exception) {
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
                m_metric_TaskState.setValue(State.CANCELED, this);   //as specified in java.util.concurrent
                return true;
            case DONE:
            case CANCELED:
            case FAILED:
                return false;
            case RUNNING:
                if (mayInterruptIfRunning && this.doCancel()) {
                    m_metric_TaskState.setValue(State.CANCELED, this);
                    return true;
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
        } catch (org.ogf.saga.error.Exception e) {
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
     * @throws TimeoutException if the wait timed out
     */
    public E get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            this.waitFor(unit.toSeconds(timeout));
        } catch (org.ogf.saga.error.Exception e) {
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
                throw new TimeoutException("The wait timed out");
        }
    }

    //////////////////////////////////////////// internal methods ////////////////////////////////////////////

    /** override AbstractMonitorableImpl._addMetric() */
    public MetricImpl _addMetric(MetricImpl metric) throws NoSuccess {
        metric.setTask(this);
        return super._addMetric(metric);
    }
}
