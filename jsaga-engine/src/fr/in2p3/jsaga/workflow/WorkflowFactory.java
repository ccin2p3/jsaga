package fr.in2p3.jsaga.workflow;

import fr.in2p3.jsaga.impl.SagaFactoryImpl;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WorkflowFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class WorkflowFactory {

    private static WorkflowFactory factory;

    private synchronized static void initializeFactory()
        throws NotImplemented, NoSuccess {
        if (factory == null) {
            factory = new SagaFactoryImpl().createWorkflowFactory();
        }
    }

    /**
     * Constructs a <code>Workflow</code> object.
     * This method is to be provided by the factory.
     * @return the workflow.
     */
    protected abstract Workflow doCreateWorkflow()
        throws NotImplemented, BadParameter, Timeout, NoSuccess;

    /**
     * Constructs a <code>Workflow</code> object.
     * This method is to be provided by the factory.
     * @return the workflow.
     */
    public synchronized static Workflow createWorkflow()
        throws NotImplemented, BadParameter, Timeout, NoSuccess {
        initializeFactory();
        return factory.doCreateWorkflow();
    }
}
