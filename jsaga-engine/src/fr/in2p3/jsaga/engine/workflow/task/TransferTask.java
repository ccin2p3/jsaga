package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskTypeType;
import fr.in2p3.jsaga.engine.workflow.AbstractWorkflowTaskImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TransferTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TransferTask extends AbstractWorkflowTaskImpl {
    /** constructor */
    public TransferTask(String url, boolean input) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(null, url);
        // update XML status
        URLDecomposer u = new URLDecomposer(url);
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setType(TaskTypeType.TRANSFER);
        xmlStatus.setGroup(u.getGroup());
        xmlStatus.setLabel(u.getLabel());
        xmlStatus.setContext(u.getContext());
        xmlStatus.setInput(input);
    }

    //////////////////////////////////////////// abstract methods ////////////////////////////////////////////

    protected void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        super.setState(State.DONE);
    }

    protected void doCancel() {
        super.setState(State.CANCELED);
    }

    protected State queryState() throws NotImplemented, Timeout, NoSuccess {
        return super.getState_LocalCheckOnly();
    }

    public boolean startListening() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        return true;    // do nothing
    }

    public void stopListening() throws NotImplemented, Timeout, NoSuccess {
        // do nothing
    }
}
