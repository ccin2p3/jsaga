package fr.in2p3.jsaga.engine.task;

import fr.in2p3.jsaga.engine.base.AbstractMonitorableImpl;
import org.ogf.saga.SagaBase;
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
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskContainerImpl extends AbstractMonitorableImpl implements TaskContainer {
    private Map m_tasks;

    /** constructor */
    public TaskContainerImpl(Session session) {
        super(session);
        m_tasks = new HashMap();
    }

    /** constructor for deepCopy */
    protected TaskContainerImpl(TaskContainerImpl source) {
        super(source);
        m_tasks = deepCopy(source.m_tasks);
    }
    public SagaBase deepCopy() {
        return new TaskContainerImpl(this);
    }

    public int add(Task task) {
        m_tasks.put(new Integer(task.getId()), task);
        return task.getId();
    }

    public void remove(int cookie) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        if (m_tasks.remove(new Integer(cookie)) == null) {
            throw new DoesNotExist("Task not in task container: "+cookie, this);
        }
    }

    public void run() throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        for (Iterator it=m_tasks.values().iterator(); it.hasNext(); ) {
            Task task = (Task) it.next();
            task.run();
        }
    }

    public Task[] doWait(float timeout, WaitMode mode) throws NotImplemented, IncorrectState, DoesNotExist, NoSuccess {
        List finishedTasks = new ArrayList();
        try {
            long endTime = (timeout>=0.0f ? System.currentTimeMillis()+(long)timeout : -1);
            while(endTime>0 && System.currentTimeMillis()<endTime) {
                // remove newly finished tasks
                for (Iterator it=m_tasks.values().iterator(); it.hasNext(); ) {
                    Task task = (Task) it.next();
                    try {
                        switch(task.getState().getValue()) {
                            case State.DONE_TYPE:
                            case State.CANCELED_TYPE:
                            case State.FAILED_TYPE:
                                finishedTasks.add(task);
                                it.remove();
                        }
                    } catch (Timeout e) {
                        // unknown state: try again on next occurence
                    }
                }

                // evaluate stop condition
                switch(mode.getValue()) {
                    case WaitMode.ALL_TYPE:
                        if (finishedTasks.size() == m_tasks.size()) {
                            throw new InterruptedException();
                        }
                        break;
                    case WaitMode.ANY_TYPE:
                        if (finishedTasks.size() > 0) {
                            throw new InterruptedException();
                        }
                }

                // wait
                Thread.currentThread().sleep(100);
            }
        } catch(InterruptedException e) {
        }
        // return newly finished tasks
        return (Task[]) finishedTasks.toArray(new Task[finishedTasks.size()]);
    }

    public void cancel(float timeout) throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        for (Iterator it=m_tasks.values().iterator(); it.hasNext(); ) {
            Task task = (Task) it.next();
            task.cancel(timeout);
        }
    }

    public int size() throws NotImplemented, Timeout, NoSuccess {
        return m_tasks.size();
    }

    public int[] listTasks() throws NotImplemented, Timeout, NoSuccess {
        Integer[] keys = (Integer[]) m_tasks.keySet().toArray(new Integer[m_tasks.size()]);
        int[] cookies = new int[keys.length];
        for (int i=0; i<keys.length; i++) {
            cookies[i] = keys[i].intValue();
        }
        return cookies;
    }

    public Task getTask(int cookie) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        Task task = (Task) m_tasks.get(new Integer(cookie));
        if (task != null) {
            return task;
        } else {
            throw new DoesNotExist("Task not in task container: "+cookie, this);
        }
    }

    public Task[] getTasks() throws NotImplemented, Timeout, NoSuccess {
        return (Task[]) m_tasks.values().toArray(new Task[m_tasks.size()]);
    }

    public State[] getStates() throws NotImplemented, Timeout, NoSuccess {
        List states = new ArrayList();
        for (Iterator it=m_tasks.values().iterator(); it.hasNext(); ) {
            Task task = (Task) it.next();
            states.add(task.getState());
        }
        return (State[]) states.toArray(new State[states.size()]);
    }
}
