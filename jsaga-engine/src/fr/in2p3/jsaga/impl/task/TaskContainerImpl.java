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
    private Map<Integer,Task> m_tasks;

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
        m_tasks = new HashMap<Integer,Task>();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        TaskContainerImpl clone = (TaskContainerImpl) super.clone();
//        clone.m_metric_TaskState = m_metric_TaskState;
        clone.m_tasks = clone(m_tasks);
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.TASKCONTAINER;
    }

    public int add(Task task) {
        int cookie = task.hashCode();
        m_tasks.put(cookie, task);
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
        for (Task task : m_tasks.values()) {
            task.run();
        }
    }

    public Task waitFor(WaitMode mode) throws NotImplemented, IncorrectState, DoesNotExist, NoSuccess {
        return this.waitFor(WAIT_FOREVER, mode);
    }

    public Task waitFor(float timeoutInSeconds, WaitMode mode) throws NotImplemented, IncorrectState, DoesNotExist, NoSuccess {
        this.startListening();
        Task task = null;
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
            while((task=this.getFinished(mode))==null && (forever || System.currentTimeMillis()<endTime)) {
                Thread.currentThread().sleep(100);
            }
        } catch(InterruptedException e) {/*ignore*/}
        this.stopListening();
        return task;
    }

    public void cancel() throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        for (Task task : m_tasks.values()) {
            task.cancel();
        }
    }

    public void cancel(float timeoutInSeconds) throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        for (Task task : m_tasks.values()) {
            task.cancel(timeoutInSeconds);
        }
    }

    public int size() throws NotImplemented, Timeout, NoSuccess {
        return m_tasks.size();
    }

    public int[] listTasks() throws NotImplemented, Timeout, NoSuccess {
        Integer[] keys = m_tasks.keySet().toArray(new Integer[m_tasks.size()]);
        int[] cookies = new int[keys.length];
        for (int i=0; i<keys.length; i++) {
            cookies[i] = keys[i];
        }
        return cookies;
    }

    public <T> Task<T> getTask(int cookie) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        Task task = m_tasks.get(cookie);
        if (task == null) {
            throw new DoesNotExist("Task not in task container: "+cookie, this);
        }
        return task;
    }

    public Task[] getTasks() throws NotImplemented, Timeout, NoSuccess {
        return m_tasks.values().toArray(new Task[m_tasks.size()]);
    }

    public State[] getStates() throws NotImplemented, Timeout, NoSuccess {
        List<State> states = new ArrayList<State>();
        for (Task task : m_tasks.values()) {
            states.add(task.getState());
        }
        return states.toArray(new State[states.size()]);
    }

    private boolean startListening() throws NotImplemented, IncorrectState, NoSuccess {
        boolean isListening = true;
        try {
            for (Task task : m_tasks.values()) {
                isListening &= ((AbstractTaskImpl)task).startListening(null);
            }
        } catch(Timeout e) {
            throw new NoSuccess(e);
        }
        return isListening;
    }
    private void stopListening() throws NotImplemented, NoSuccess {
        try {
            for (Task task : m_tasks.values()) {
                ((AbstractTaskImpl)task).stopListening(null);
            }
        } catch(Timeout e) {
            throw new NoSuccess(e);
        }
    }

    private Task getFinished(WaitMode mode) throws NotImplemented {
        switch(mode) {
            case ALL:
                return getFinishedAll();
            case ANY:
                return getFinishedAny();
            default:
                throw new NotImplemented("INTERNAL ERROR: unexpected exception");
        }
    }
    private Task getFinishedAll() {
        Integer cookie = null;
        for (Map.Entry<Integer,Task> entry : m_tasks.entrySet()) {
            cookie = entry.getKey();
            Task task = entry.getValue();
            if (!task.isDone()) {
                return null;
            }
        }
        if (cookie != null) {
            return m_tasks.remove(cookie);
        }
        return null;
    }
    private Task getFinishedAny() {
        for (Map.Entry<Integer,Task> entry : m_tasks.entrySet()) {
            Integer cookie = entry.getKey();
            Task task = entry.getValue();
            if (task.isDone()) {
                return m_tasks.remove(cookie);
            }
        }
        return null;
    }
}
