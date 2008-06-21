package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskTypeType;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SourceTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SourceTask extends DummyTask {
    /** constructor */
    public SourceTask(String url, boolean input, boolean keep) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(url);
        // update XML status
        URLDecomposer u = new URLDecomposer(url);
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setType(TaskTypeType.SOURCE);
        xmlStatus.setGroup(u.getGroup());
        xmlStatus.setLabel(u.getLabel());
        xmlStatus.setContext(u.getContext());
        xmlStatus.setInput(input);
        xmlStatus.setKeep(keep);
    }
}
