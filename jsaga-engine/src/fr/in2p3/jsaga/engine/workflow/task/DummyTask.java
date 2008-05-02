package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.workflow.AbstractWorkflowTaskImpl;
import org.ogf.saga.session.Session;
import org.ogf.saga.error.*;
import org.ogf.saga.task.State;
import org.ogf.saga.monitoring.Metric;

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
public class DummyTask extends AbstractWorkflowTaskImpl {
    /** constructor */
    public DummyTask(Session session, String name) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, name);
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

    public boolean startListening(Metric metric) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        return true;    // do nothing
    }

    public void stopListening(Metric metric) throws NotImplemented, Timeout, NoSuccess {
        // do nothing
    }
}
