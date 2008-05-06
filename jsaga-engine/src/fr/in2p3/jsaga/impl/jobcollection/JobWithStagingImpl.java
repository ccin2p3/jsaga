package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.engine.jobcollection.DataStagingTaskGenerator;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.engine.workflow.task.DummyTask;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.instance.JobHandle;
import fr.in2p3.jsaga.impl.job.instance.LateBindedJobImpl;
import fr.in2p3.jsaga.workflow.Workflow;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobWithStagingImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobWithStagingImpl extends LateBindedJobImpl {
    private Workflow m_workflow;
    private DummyTask m_jobEnd;

    /** constructor for submission */
    public JobWithStagingImpl(Session session, XJSDLJobDescriptionImpl jobDesc, JobHandle jobHandle, Workflow workflow, DummyTask jobEnd) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, jobDesc, jobHandle);
        m_workflow = workflow;
        m_jobEnd = jobEnd;
    }

    /** override super.allocate() */
    public void allocate(Resource rm) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // transform job description and create job
        super.allocate(rm);

        // update workflow
        DataStagingTaskGenerator staging = new DataStagingTaskGenerator(m_jobDesc.getAsDocument());
        staging.updateWorkflow(m_workflow);
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    /** override super.queryState() */
    protected State queryState() throws NotImplemented, Timeout, NoSuccess {
        State state = m_jobEnd.getState();
        switch(state) {
            case DONE:
            case CANCELED:
            case FAILED:
                return state;
        }
        state = m_jobHandle.getState();
        switch(state) {
            case DONE:
                return State.RUNNING;
            default:
                return state;
        }        
    }    
}
