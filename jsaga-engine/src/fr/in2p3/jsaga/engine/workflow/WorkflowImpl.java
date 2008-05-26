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
import java.io.*;
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
    private fr.in2p3.jsaga.engine.schema.status.Workflow m_xmlStatus;

    /** constructor */
    public WorkflowImpl(Session session, String workflowName) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session);
        m_workflowTasks = Collections.synchronizedMap(new HashMap<String,WorkflowTask>());
        // set XML status
        m_xmlStatus = new fr.in2p3.jsaga.engine.schema.status.Workflow();
        m_xmlStatus.setName(workflowName);
        // add start task
        WorkflowTask startTask = new StartTask();
        m_workflowTasks.put(startTask.getName(), startTask);
        m_xmlStatus.addTask(startTask.getStateAsXML());
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        WorkflowImpl clone = (WorkflowImpl) super.clone();
        clone.m_workflowTasks.putAll(m_workflowTasks);
        clone.m_xmlStatus = m_xmlStatus;
        return clone;
    }

    /** Override super.run() */
    public void run() throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        WorkflowTask startTask = m_workflowTasks.get(StartTask.NAME);
        startTask.run();
    }

    public void add(WorkflowTask newTask, String predecessorName, String successorName) throws NotImplemented, Timeout, NoSuccess {
        String[] predecessorNamesArray = (predecessorName!=null ? new String[]{predecessorName} : null);
        String[] successorNamesArray = (successorName!=null ? new String[]{successorName} : null);
        this.add(newTask, predecessorNamesArray, successorNamesArray);
    }

    private void add(WorkflowTask newTask, String[] predecessorNames, String[] successorNames) throws NotImplemented, Timeout, NoSuccess {
        // add task to workflow or retrieve task from workflow
        WorkflowTask task = m_workflowTasks.get(newTask.getName());
        if (task == null) {
            m_workflowTasks.put(newTask.getName(), newTask);
            m_xmlStatus.addTask(newTask.getStateAsXML());
            task = newTask;
        }
        
        // link to predecessor
        for (int i=0; predecessorNames!=null && i<predecessorNames.length; i++) {
            WorkflowTask predecessor = m_workflowTasks.get(predecessorNames[i]);
            if (predecessor == null) {
                throw new NoSuccess("Predecessor not found in workflow: "+predecessorNames[i]);
            }
            predecessor.addSuccessor(task);
            task.addPredecessor(predecessor);
        }

        // link to successor
        for (int i=0; successorNames!=null && i<successorNames.length; i++) {
            WorkflowTask successor = m_workflowTasks.get(successorNames[i]);
            if (successor == null) {
                throw new NoSuccess("Successor not found in workflow: "+successorNames[i]);
            }
            task.addSuccessor(successor);
            successor.addPredecessor(task);
        }

        // may run task
        try {
            task.run();
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

    public void remove(String name) throws NotImplemented, Timeout, NoSuccess {
        WorkflowTask task = m_workflowTasks.remove(name);
        if (task != null) {
            m_xmlStatus.removeTask(task.getStateAsXML());
            task.unlink();
        }
    }

    public WorkflowTask getTask(String name) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        WorkflowTask task = m_workflowTasks.get(name);
        if (task != null) {
            return task;
        } else {
            throw new DoesNotExist("Task not in workflow: "+name, this);
        }
    }

    public synchronized Document getStatesAsXML() throws NotImplemented, Timeout, NoSuccess {
        // workaround: if marshalling directly to DOM then namespaces are ignored
        try {
            // first marshall to byte array
            ByteArrayOutputStream xml = new ByteArrayOutputStream();
            this.dumpStatesToWriter(new OutputStreamWriter(xml));
            // then parse the marshalled document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.toByteArray()));
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    protected synchronized void dumpStatesToWriter(Writer writer) throws Exception {
        Marshaller m = new Marshaller(writer);
        m.setNamespaceMapping("", "http://www.in2p3.fr/jsaga/status");
        m.marshal(m_xmlStatus);
    }
}
