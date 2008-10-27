package fr.in2p3.jsaga.engine.workflow;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskTypeType;
import fr.in2p3.jsaga.engine.workflow.task.DummyTask;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StartTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class StartTask extends DummyTask {
    public static final String NAME = "start";

    /** constructor */
    StartTask() throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(NAME);
        // update XML status
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setType(TaskTypeType.START);
        xmlStatus.setLabel(NAME);
    }

    /** override super.run() */
    public void run() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        // inconditionally submit task
        if (!this.isCancelled()) {
            this.doSubmit();
        }
    }

    public static String name() {
        return NAME;
    }
}
