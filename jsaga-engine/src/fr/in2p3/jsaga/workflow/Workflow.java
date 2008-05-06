package fr.in2p3.jsaga.workflow;

import org.ogf.saga.error.*;
import org.ogf.saga.task.TaskContainer;
import org.w3c.dom.Document;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Workflow
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface Workflow extends TaskContainer {
    /**
     * Gets the states of all tasks in the workflow.
     * @return the states in a XML document.
     */
    public Document getStatesAsXML()
            throws NotImplemented, Timeout, NoSuccess;

    /**
     * Adds a task to the workflow.
     * @param task the task to add.
     * @param predecessorName name of a predecessor task.
     * @param successorName name of a successor task.
     */
    public void add(WorkflowTask task, String predecessorName, String successorName)
            throws NotImplemented, Timeout, NoSuccess;

    ////////////////////////////////// interface TaskContainer /////////////////////////////////

    /**
     * TO BE OVERRIDED FOR IMPLEMENTING WORKFLOW BEHAVIOR.
     */
    public void run()
            throws NotImplemented, IncorrectState, DoesNotExist, Timeout, NoSuccess;
}
