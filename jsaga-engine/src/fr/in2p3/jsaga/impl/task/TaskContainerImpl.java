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
    private final Map<Integer,AbstractTaskImpl> m_tasks;

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
        m_tasks = Collections.synchronizedMap(new HashMap<Integer,AbstractTaskImpl>());
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        TaskContainerImpl clone = (TaskContainerImpl) super.clone();
//        clone.m_metric_TaskContainerState = m_metric_TaskContainerState;
        clone.m_tasks.putAll(m_tasks);
        return clone;
    }

    public int add(Task<?,?> task) throws NotImplementedException, TimeoutException, NoSuccessException {
        int cookie = task.hashCode();
        m_tasks.put(cookie, (AbstractTaskImpl) task);
        return cookie;
    }

    public Task<?,?> remove(int cookie) throws NotImplementedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Task task = m_tasks.remove(cookie);
        if (task == null) {
            throw new DoesNotExistException("Task not in task container: "+cookie, this);
        }
        return task;
    }

    public void run() throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        synchronized(m_tasks) {
            for (Task task : m_tasks.values()) {
                task.run();
            }
        }
    }

    public Task<?,?> waitFor(WaitMode mode) throws NotImplementedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.waitFor(WAIT_FOREVER, mode);
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

        Integer cookie = null;
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
            while((cookie=this.getFinished(mode))==null && (forever || System.currentTimeMillis()<endTime)) {
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

        return m_tasks.remove(cookie);
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

    public int[] listTasks() throws NotImplementedException, TimeoutException, NoSuccessException {
        int i=0;
        synchronized(m_tasks) {
            Set<Integer> keys = m_tasks.keySet();
            int[] cookies = new int[keys.size()];
            for (Integer key : keys) {
                cookies[i++] = key;
            }
            return cookies;
        }
    }

    public Task<?,?> getTask(int cookie) throws NotImplementedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Task task = m_tasks.get(cookie);
        if (task == null) {
            throw new DoesNotExistException("Task not in task container: "+cookie, this);
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
                    if (! task.isDone_LocalCheckOnly()) {
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

    private Integer getFinished(WaitMode mode) throws NotImplementedException {
        switch(mode) {
            case ALL:
                return getFinishedAll();
            case ANY:
                return getFinishedAny();
            default:
                throw new NotImplementedException("INTERNAL ERROR: unexpected exception");
        }
    }
    private Integer getFinishedAll() {
        Integer cookie = null;
        synchronized(m_tasks) {
            for (Map.Entry<Integer,AbstractTaskImpl> entry : m_tasks.entrySet()) {
                cookie = entry.getKey();
                AbstractTaskImpl task = entry.getValue();
                if (!task.isDone()) {
                    return null;
                }
            }
        }
        return cookie;
    }
    private Integer getFinishedAny() {
        synchronized(m_tasks) {
            for (Map.Entry<Integer,AbstractTaskImpl> entry : m_tasks.entrySet()) {
                Integer cookie = entry.getKey();
                AbstractTaskImpl task = entry.getValue();
                if (task.isDone()) {
                    return cookie;
                }
            }
        }
        return null;
    }
}
