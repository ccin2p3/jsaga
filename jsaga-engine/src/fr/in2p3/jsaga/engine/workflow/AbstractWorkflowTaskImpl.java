package fr.in2p3.jsaga.engine.workflow;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskStateType;
import fr.in2p3.jsaga.impl.task.AbstractTaskImpl;
import fr.in2p3.jsaga.workflow.WorkflowTask;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractWorkflowTaskImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractWorkflowTaskImpl extends AbstractTaskImpl implements WorkflowTask {
    private String m_name;
    private final Map<String,WorkflowTask> m_predecessors;
    private final Map<String,WorkflowTask> m_successors;
    private Task m_xmlStatus;

    /** constructor */
    public AbstractWorkflowTaskImpl(Session session, String name) throws NotImplementedException {
        super(session, null, true);
        m_name = name;
        m_predecessors = new HashMap<String,WorkflowTask>();
        m_successors = new HashMap<String,WorkflowTask>();
        // set XML status
        m_xmlStatus = new Task();
        m_xmlStatus.setName(m_name);
        m_xmlStatus.setState(TaskStateType.NEW);
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cloning workflow tasks is not supported yet");
    }

    //////////////////////////////////////////////// interface Task ////////////////////////////////////////////////

    public void run() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        // run current task if predecessors are all DONE
        boolean areAllDone = (m_predecessors.size()>0);
        for (Iterator<WorkflowTask> it=m_predecessors.values().iterator(); areAllDone && it.hasNext();) {
            areAllDone &= State.DONE.equals(it.next().getState());
        }
        if (areAllDone) {
            super.run();
        }
    }

    //////////////////////////////////////////// interface TaskCallback ////////////////////////////////////////////

    /** override super.setState() */
    public synchronized void setState(State state) {
        // update state
        super.setState(state);

        // update XML status
        m_xmlStatus.setState(toTaskStateType(state));

        // run successors if state of current task is DONE
        switch(state) {
            case DONE:
                for (Iterator<WorkflowTask> it=m_successors.values().iterator(); it.hasNext(); ) {
                    WorkflowTask successor = it.next();
                    try {
                        successor.run();
                    } catch (Exception e) {
                        successor.setException(new NoSuccessException(e));
                        successor.setState(State.FAILED);
                    }
                }
                break;
            case FAILED:
            case CANCELED:
                for (Iterator<WorkflowTask> it=m_successors.values().iterator(); it.hasNext(); ) {
                    WorkflowTask successor = it.next();
                    successor.setException(super.m_exception);
                    successor.setState(state);
                }
                break;
        }
    }
    
    ///////////////////////////////////////// interface WorkflowTask /////////////////////////////////////////

    public String getName() {
        return m_name;
    }

    public synchronized boolean hasPredecessors() {
        return (! m_predecessors.isEmpty());
    }

    public synchronized void addPredecessor(WorkflowTask predecessor) {
        if (! m_predecessors.containsKey(predecessor.getName())) {
            m_predecessors.put(predecessor.getName(), predecessor);
        }
    }

    public synchronized void removePredecessor(String predecessorName) {
        m_predecessors.remove(predecessorName);
    }

    public synchronized boolean hasSuccessors() {
        return (! m_successors.isEmpty());
    }

    public synchronized void addSuccessor(WorkflowTask successor) {
        if (! m_successors.containsKey(successor.getName())) {
            m_successors.put(successor.getName(), successor);
            // update XML status
            m_xmlStatus.addSuccessor(successor.getName());
        }
    }

    public synchronized void removeSuccessor(String successorName) {
        m_successors.remove(successorName);
        // update XML status
        m_xmlStatus.removeSuccessor(successorName);
    }

    public synchronized void unlink() {
        for (Iterator<WorkflowTask> it=m_predecessors.values().iterator(); it.hasNext(); ) {
            WorkflowTask predecessor =  it.next();
            predecessor.removeSuccessor(m_name);
        }
        for (Iterator<WorkflowTask> it=m_successors.values().iterator(); it.hasNext(); ) {
            WorkflowTask successor = it.next();
            successor.removePredecessor(m_name);
        }
    }

    public Task getStateAsXML() throws NotImplementedException, TimeoutException, NoSuccessException {
        return m_xmlStatus;
    }

    private static TaskStateType toTaskStateType(State state) {
        switch(state) {
            case NEW:
                return TaskStateType.NEW;
            case RUNNING:
                return TaskStateType.RUNNING;
            case DONE:
                return TaskStateType.DONE;
            case CANCELED:
                return TaskStateType.CANCELED;
            case FAILED:
                return TaskStateType.FAILED;
            case SUSPENDED:
                return TaskStateType.SUSPENDED;
            default:
                return null;
        }
    }
}
