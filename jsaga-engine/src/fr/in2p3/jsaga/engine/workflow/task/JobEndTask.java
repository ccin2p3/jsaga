package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.schema.status.Task;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobEndTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobEndTask extends DummyTask {
    /** constructor */
    public JobEndTask(String jobName) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(name(jobName));
        // update XML status
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setGroup(name(jobName));
    }

    public static String name(String jobName) {
        return "end_"+jobName;
    }
}
