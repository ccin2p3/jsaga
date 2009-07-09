package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.workflow.AbstractWorkflowTaskImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DummyTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class DummyTask extends AbstractWorkflowTaskImpl {
    /** constructor */
    public DummyTask(String name) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(null, name);
    }

    //////////////////////////////////////////// abstract methods ////////////////////////////////////////////

    protected void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        super.setState(State.DONE);
    }

    protected void doCancel() {
        super.setState(State.CANCELED);
    }

    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        return super.getState_fromCache();
    }

    public boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        return true;    // do nothing
    }

    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        // do nothing
    }
}
