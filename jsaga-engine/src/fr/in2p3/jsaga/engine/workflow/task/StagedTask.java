package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskTypeType;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StagedTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class StagedTask extends DummyTask {
    /** constructor */
    public StagedTask(String jobName, String dataStagingName, boolean input) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(name(jobName, dataStagingName, input));
        // update XML status
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setType(TaskTypeType.STAGED);
        xmlStatus.setGroup((input?"run":"end")+"_"+jobName);
        xmlStatus.setLabel(dataStagingName);
        xmlStatus.setInput(input);
    }

    public static String name(String jobName, String dataStagingName, boolean input) {
        return (input?"in":"out")+"_"+jobName+"_"+dataStagingName;
    }
}
