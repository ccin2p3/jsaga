package fr.in2p3.jsaga.engine.workflow;

import fr.in2p3.jsaga.impl.task.TaskContainerImpl;
import fr.in2p3.jsaga.workflow.Workflow;
import fr.in2p3.jsaga.workflow.WorkflowTask;
import org.exolab.castor.xml.Marshaller;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.Exception;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WorkflowImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WorkflowImpl extends TaskContainerImpl implements Workflow {
    private final Map<String,WorkflowTask> m_workflowTasks;

    /** constructor */
    public WorkflowImpl(Session session) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session);
        WorkflowTask startTask = new StartTask();
        m_workflowTasks = Collections.synchronizedMap(new HashMap<String,WorkflowTask>());
        m_workflowTasks.put(startTask.getName(), startTask);
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        WorkflowImpl clone = (WorkflowImpl) super.clone();
        clone.m_workflowTasks.putAll(m_workflowTasks);
        return clone;
    }

    public void add(WorkflowTask task, String predecessorName, String successorName) throws NotImplemented, Timeout, NoSuccess {
        // link to predecessor
        if (predecessorName != null) {
            WorkflowTask predecessor = m_workflowTasks.get(predecessorName);
            if (predecessor == null) {
                throw new NoSuccess("Predecessor not found in workflow: "+predecessorName);
            }
            predecessor.addSuccessor(task);
            task.addPredecessor(predecessor);
        }

        // link to successor
        if (successorName != null) {
            WorkflowTask successor = m_workflowTasks.get(successorName);
            if (successor == null) {
                throw new NoSuccess("Successor not found in workflow: "+successorName);
            }
            task.addSuccessor(successor);
            successor.addPredecessor(task);
        }

        if (! m_workflowTasks.containsKey(task.getName())) {
            // add
            m_workflowTasks.put(task.getName(), task);

            // may run task
            try {
                task.run();
            } catch (IncorrectState e) {
                throw new NoSuccess(e);
            }
        }
    }

    /** Override super.run() */
    public void run() throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        WorkflowTask startTask = m_workflowTasks.get(StartTask.NAME);
        startTask.run();
    }

    public synchronized Document getStatesAsXML() throws NotImplemented, Timeout, NoSuccess {
        // create status bean
        fr.in2p3.jsaga.engine.schema.status.Workflow workflow = new fr.in2p3.jsaga.engine.schema.status.Workflow();
        workflow.setName("todo");
        for (Iterator<WorkflowTask> it = m_workflowTasks.values().iterator(); it.hasNext();) {
            workflow.addTask(it.next().getStateAsXML());
        }

        // serialize it
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller.marshal(workflow, doc);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        return doc;
    }
}
