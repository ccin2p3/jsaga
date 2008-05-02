package fr.in2p3.jsaga.engine.workflow;

import fr.in2p3.jsaga.workflow.Workflow;
import fr.in2p3.jsaga.workflow.WorkflowFactory;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WorkflowFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WorkflowFactoryImpl extends WorkflowFactory {
    protected Workflow doCreateWorkflow() throws NotImplemented, BadParameter, Timeout, NoSuccess {
        return new WorkflowImpl(null);
    }
}
