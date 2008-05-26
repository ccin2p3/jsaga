package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.monitoring.AbstractMonitorableImpl;
import org.ogf.saga.ObjectType;
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
//    private MetricImpl<State> m_metric_TaskState;
    // internal
    private final Map<Integer,AbstractTaskImpl> m_tasks;

    /** constructor */
    public TaskContainerImpl(Session session) throws NoSuccess {
        super(session);

        // set metrics
/*
        m_metric_TaskState = this._addMetric(new MetricImpl<State>(
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
//        clone.m_metric_TaskState = m_metric_TaskState;
        clone.m_tasks.putAll(m_tasks);
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.TASKCONTAINER;
    }

    public int add(Task task) throws NotImplemented, Timeout, NoSuccess {
        int cookie = task.hashCode();
        m_tasks.put(cookie, (AbstractTaskImpl) task);
        return cookie;
    }

    public <T> Task<T> remove(int cookie) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        Task task = m_tasks.remove(cookie);
        if (task == null) {
            throw new DoesNotExist("Task not in task container: "+cookie, this);
        }
        return task;
    }

    public void run() throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        synchronized(m_tasks) {
            for (Task task : m_tasks.values()) {
                task.run();
            }
        }
    }

    public Task waitFor(WaitMode mode) throws NotImplemented, IncorrectState, DoesNotExist, NoSuccess {
        return this.waitFor(WAIT_FOREVER, mode);
    }

    public Task waitFor(float timeoutInSeconds, WaitMode mode) throws NotImplemented, IncorrectState, DoesNotExist, NoSuccess {
        if (m_tasks.isEmpty()) {
            throw new DoesNotExist("Task container is empty", this);
        }
        this.startListening();
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
        return m_tasks.remove(cookie);
    }

    public void cancel() throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        synchronized(m_tasks) {
            for (Task task : m_tasks.values()) {
                task.cancel();
            }
        }
    }

    public void cancel(float timeoutInSeconds) throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        synchronized(m_tasks) {
            for (Task task : m_tasks.values()) {
                task.cancel(timeoutInSeconds);
            }
        }
    }

    public int size() throws NotImplemented, Timeout, NoSuccess {
        return m_tasks.size();
    }

    public int[] listTasks() throws NotImplemented, Timeout, NoSuccess {
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

    public <T> Task<T> getTask(int cookie) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        Task task = m_tasks.get(cookie);
        if (task == null) {
            throw new DoesNotExist("Task not in task container: "+cookie, this);
        }
        return task;
    }

    public Task[] getTasks() throws NotImplemented, Timeout, NoSuccess {
        synchronized(m_tasks) {
            return m_tasks.values().toArray(new Task[m_tasks.size()]);
        }
    }

    public State[] getStates() throws NotImplemented, Timeout, NoSuccess {
        int i=0;
        synchronized(m_tasks) {
            State[] states = new State[m_tasks.size()];
            for (Task task : m_tasks.values()) {
                states[i++] = task.getState();
            }
            return states;
        }
    }

    private void startListening() throws NotImplemented, IncorrectState, NoSuccess {
        try {
            synchronized(m_tasks) {
                for (AbstractTaskImpl task : m_tasks.values()) {
                    boolean isListening = task.startListening();
                    task.setWaitingFor(isListening);
                }
            }
        } catch(Timeout e) {
            throw new NoSuccess(e);
        }
    }
    private void stopListening() throws NotImplemented, NoSuccess {
        try {
            synchronized(m_tasks) {
                for (AbstractTaskImpl task : m_tasks.values()) {
                    task.stopListening();
                    task.setWaitingFor(false);
                }
            }
        } catch(Timeout e) {
            throw new NoSuccess(e);
        }
    }

    private Integer getFinished(WaitMode mode) throws NotImplemented {
        switch(mode) {
            case ALL:
                return getFinishedAll();
            case ANY:
                return getFinishedAny();
            default:
                throw new NotImplemented("INTERNAL ERROR: unexpected exception");
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
