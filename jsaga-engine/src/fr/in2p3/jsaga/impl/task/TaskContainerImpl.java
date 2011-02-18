package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.monitoring.AbstractMonitorableImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.*;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskContainerImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskContainerImpl extends AbstractMonitorableImpl implements TaskContainer {
    // metrics
//    private MetricImpl<Task> m_metric_TaskContainerState;
    // internal
    private final Map<String,AbstractTaskImpl> m_tasks;

    /** constructor */
    public TaskContainerImpl(Session session) throws NoSuccessException {
        super(session);

        // set metrics
/*
        m_metric_TaskContainerState = this._addMetric(new MetricImpl<Task>(
                this,
                TaskContainer.TASKCONTAINER_STATE,
                "fires on state changes of any task in the container, and has the value of that task's handle",
                MetricMode.ReadOnly,
                "1",
                MetricType.Enum,
                null));
*/

        // internal
        m_tasks = Collections.synchronizedMap(new HashMap<String,AbstractTaskImpl>());
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        TaskContainerImpl clone = (TaskContainerImpl) super.clone();
//        clone.m_metric_TaskContainerState = m_metric_TaskContainerState;
        clone.m_tasks.putAll(m_tasks);
        return clone;
    }

    public void add(Task<?,?> task) throws NotImplementedException, TimeoutException, NoSuccessException {
        m_tasks.put(task.getId(), (AbstractTaskImpl) task);
    }

    public void remove(Task<?,?> task) throws NotImplementedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Task removed = m_tasks.remove(task.getId());
        if (removed== null) {
            throw new DoesNotExistException("Task not in task container: "+task.getId(), this);
        }
    }

    public void run() throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        synchronized(m_tasks) {
        	if (m_tasks.isEmpty())
        		throw new DoesNotExistException("Container is empty");
            for (Task task : m_tasks.values()) {
                task.run();
            }
        }
    }

    public Task<?, ?> waitFor() throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.waitFor(WAIT_FOREVER, WaitMode.ALL);
    }

    public Task<?,?> waitFor(WaitMode mode) throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.waitFor(WAIT_FOREVER, mode);
    }

    public Task<?, ?> waitFor(float timeoutInSeconds) throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.waitFor(timeoutInSeconds, WaitMode.ALL);
    }

    public Task<?,?> waitFor(float timeoutInSeconds, WaitMode mode) throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_tasks.isEmpty()) {
            throw new DoesNotExistException("Task container is empty", this);
        }

        this.startListening();
        // todo: use addCallback instead of startListening
        /*int tcCookie;
        try {
            tcCookie = m_metric_TaskContainerState.addCallback(new Callback(){
                public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                    return !m_tasks.isEmpty();
                }
            });
        }
        catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
        catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
        catch (PermissionDeniedException e) {throw new NoSuccessException(e);}*/

        String id = null;
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
                endTime = System.currentTimeMillis() + (long) timeoutInSeconds;
            }
            while((id=this.getFinished(mode))==null && (forever || System.currentTimeMillis()<endTime)) {
                Thread.currentThread().sleep(100);
            }
        } catch(InterruptedException e) {/*ignore*/}

        this.stopListening();
        /*{
            // callback may have not been removed
            try {
                m_metric_TaskContainerState.removeCallback(tcCookie);
            }
            catch (BadParameterException e2) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e2) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e2) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e2) {throw new NoSuccessException(e);}
        }*/

        return m_tasks.remove(id);
    }

    public void cancel() throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        synchronized(m_tasks) {
            for (Task task : m_tasks.values()) {
                task.cancel();
            }
        }
    }

    public void cancel(float timeoutInSeconds) throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        synchronized(m_tasks) {
            for (Task task : m_tasks.values()) {
                task.cancel(timeoutInSeconds);
            }
        }
    }

    public int size() throws NotImplementedException, TimeoutException, NoSuccessException {
        return m_tasks.size();
    }

    public Task<?,?> getTask(String id) throws NotImplementedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Task task = m_tasks.get(id);
        if (task == null) {
            throw new DoesNotExistException("Task not in task container: "+id, this);
        }
        return task;
    }

    public Task<?,?>[] getTasks() throws NotImplementedException, TimeoutException, NoSuccessException {
        synchronized(m_tasks) {
            return m_tasks.values().toArray(new Task[m_tasks.size()]);
        }
    }

    public State[] getStates() throws NotImplementedException, TimeoutException, NoSuccessException {
        int i=0;
        synchronized(m_tasks) {
            State[] states = new State[m_tasks.size()];
            for (Task task : m_tasks.values()) {
                states[i++] = task.getState();
            }
            return states;
        }
    }

/*
    protected void notifyStateChange(Task task) {
        m_metric_TaskContainerState.setValue(task);
    }
*/

    private void startListening() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        try {
            synchronized(m_tasks) {
                for (AbstractTaskImpl task : m_tasks.values()) {
                    if (! task.isDone_fromCache()) {
                        boolean isListening = task.startListening();
                        task.setWaitingFor(isListening);
                    }
                }
            }
        } catch(TimeoutException e) {
            throw new NoSuccessException(e);
        }
    }
    private void stopListening() throws NotImplementedException, NoSuccessException {
        try {
            synchronized(m_tasks) {
                for (AbstractTaskImpl task : m_tasks.values()) {
                    if (task.isWaitingFor()) {
                        task.stopListening();
                        task.setWaitingFor(false);
                    }
                }
            }
        } catch(TimeoutException e) {
            throw new NoSuccessException(e);
        }
    }

    private String getFinished(WaitMode mode) throws NotImplementedException {
        switch(mode) {
            case ALL:
                synchronized(m_tasks) {
                    String id = null;
                    for (Map.Entry<String,AbstractTaskImpl> entry : m_tasks.entrySet()) {
                        id = entry.getKey();
                        AbstractTaskImpl task = entry.getValue();
                        if (!task.isDone()) {
                            return null;
                        }
                    }
                    return id;
                }
            case ANY:
                synchronized(m_tasks) {
                    for (Map.Entry<String,AbstractTaskImpl> entry : m_tasks.entrySet()) {
                        String id = entry.getKey();
                        AbstractTaskImpl task = entry.getValue();
                        if (task.isDone()) {
                            return id;
                        }
                    }
                    return null;
                }
            default:
                throw new NotImplementedException("INTERNAL ERROR: unexpected exception");
        }
    }
}
