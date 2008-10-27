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
public interface WorkflowTask<T,E> extends Task<T,E>, TaskCallback<E> {
    /**
     * Gets the name of the task.
     * @return the name of the task.
     */
    public String getName();

    /**
     * Check if task has predecessors.
     * @return true if the task has one or several predecessors.
     */
    public boolean hasPredecessors();

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
     * Check if task has successors.
     * @return true if the task has one or several successors.
     */
    public boolean hasSuccessors();

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
    public fr.in2p3.jsaga.engine.schema.status.Task getStateAsXML() throws TimeoutException, NoSuccessException, NotImplementedException;

    ////////////////////////////////////// interface Task //////////////////////////////////////

    /**
     * TO BE OVERRIDED FOR IMPLEMENTING WORKFLOW BEHAVIOR.
     */
    public void run()
            throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException;

    ////////////////////////////////// interface TaskCallback //////////////////////////////////

    /**
     * TO BE OVERRIDED FOR IMPLEMENTING WORKFLOW BEHAVIOR.
     */
    public void setState(State state);
}
