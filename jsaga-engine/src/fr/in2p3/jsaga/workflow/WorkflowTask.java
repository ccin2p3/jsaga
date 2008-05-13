package fr.in2p3.jsaga.workflow;

import fr.in2p3.jsaga.impl.task.TaskCallback;
import org.ogf.saga.error.*;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WorkflowTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface WorkflowTask<E> extends Task<E>, TaskCallback<E> {
    /**
     * Gets the name of the task.
     * @return the name of the task.
     */
    public String getName();

    /**
     * Adds a predecessor to the task.
     * @param predecessor the predecessor to add.
     */
    public void addPredecessor(WorkflowTask predecessor);

    /**
     * Removes a predecessor from the task.
     * @param predecessorName the name of the predecessor to remove.
     */
    public void removePredecessor(String predecessorName);

    /**
     * Adds a successor to the task.
     * @param successor the successor to add.
     */
    public void addSuccessor(WorkflowTask successor);

    /**
     * Removes a successor from the task.
     * @param successorName the name of the successor to remove.
     */
    public void removeSuccessor(String successorName);

    /**
     * Unlink this task from its predecessors and successors.
     */
    public void unlink();

    /**
     * Gets the state of the task.
     * @return the state of the task in a XML bean.
     */
    public fr.in2p3.jsaga.engine.schema.status.Task getStateAsXML() throws Timeout, NoSuccess, NotImplemented;

    ////////////////////////////////////// interface Task //////////////////////////////////////

    /**
     * TO BE OVERRIDED FOR IMPLEMENTING WORKFLOW BEHAVIOR.
     */
    public void run()
            throws NotImplemented, IncorrectState, Timeout, NoSuccess;

    ////////////////////////////////// interface TaskCallback //////////////////////////////////

    /**
     * TO BE OVERRIDED FOR IMPLEMENTING WORKFLOW BEHAVIOR.
     */
    public void setState(State state);
}
