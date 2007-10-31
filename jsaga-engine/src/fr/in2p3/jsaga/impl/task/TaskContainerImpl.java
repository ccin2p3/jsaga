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
    private Map<Integer,Task> m_tasks;

    /** constructor */
    public TaskContainerImpl(Session session) {
        super(session);
        m_tasks = new HashMap<Integer,Task>();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        TaskContainerImpl clone = (TaskContainerImpl) super.clone();
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

    public void remove(int cookie) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        if (m_tasks.remove(cookie) == null) {
            throw new DoesNotExist("Task not in task container: "+cookie, this);
        }
    }

    public void run() throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        for (Task task : m_tasks.values()) {
            task.run();
        }
    }

    public Task[] waitTasks(WaitMode mode) throws NotImplemented, IncorrectState, DoesNotExist, NoSuccess {
        List<Task> finishedTasks = new ArrayList<Task>();
        try {
            while(true) {
                // check state
                if (isFinished(finishedTasks, mode)) {
                    throw new InterruptedException();
                }

                // wait
                Thread.currentThread().sleep(100);
            }
        } catch(InterruptedException e) {/*ignore*/}
        // return newly finished tasks
        return finishedTasks.toArray(new Task[finishedTasks.size()]);
    }

    public Task[] waitTasks(float timeoutInSeconds, WaitMode mode) throws NotImplemented, IncorrectState, DoesNotExist, NoSuccess {
        List<Task> finishedTasks = new ArrayList<Task>();
        try {
            long endTime = (timeoutInSeconds>=0.0f ? System.currentTimeMillis()+(long)timeoutInSeconds : -1);
            while(endTime>0 && System.currentTimeMillis()<endTime) {
                // check state
                if (isFinished(finishedTasks, mode)) {
                    throw new InterruptedException();
                }

                // wait
                Thread.currentThread().sleep(100);
            }
        } catch(InterruptedException e) {/*ignore*/}
        // return newly finished tasks
        return finishedTasks.toArray(new Task[finishedTasks.size()]);
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

    public Task getTask(int cookie) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        Task task = m_tasks.get(cookie);
        if (task != null) {
            return task;
        } else {
            throw new DoesNotExist("Task not in task container: "+cookie, this);
        }
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

    private boolean isFinished(List<Task> finishedTasks, WaitMode mode) throws NotImplemented, NoSuccess {
        // remove newly finished tasks
        for (Iterator it=m_tasks.values().iterator(); it.hasNext(); ) {
            Task task = (Task) it.next();
            try {
                switch(task.getState()) {
                    case DONE:
                    case CANCELED:
                    case FAILED:
                        finishedTasks.add(task);
                        it.remove();
                }
            } catch (Timeout e) {
                // unknown state: try again on next occurence
            }
        }

        // evaluate stop condition
        switch(mode) {
            case ALL:
                if (finishedTasks.size() == m_tasks.size()) {
                    return true;
                }
                break;
            case ANY:
                if (finishedTasks.size() > 0) {
                    return true;
                }
        }
        return false;
    }
}
