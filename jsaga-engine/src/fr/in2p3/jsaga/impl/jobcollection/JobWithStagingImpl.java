package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.engine.jobcollection.DataStagingTaskGenerator;
import fr.in2p3.jsaga.engine.jobcollection.transform.IndividualJobPreprocessor;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.engine.workflow.StartTask;
import fr.in2p3.jsaga.engine.workflow.task.DummyTask;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.instance.JobHandle;
import fr.in2p3.jsaga.impl.job.instance.LateBindedJobImpl;
import fr.in2p3.jsaga.jobcollection.JobWithStaging;
import fr.in2p3.jsaga.workflow.Workflow;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.w3c.dom.Document;

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
public class JobWithStagingImpl extends LateBindedJobImpl implements JobWithStaging {
    private Workflow m_workflow;
    private DummyTask m_jobEnd;
    private DummyTask m_startTask;
    private String m_wrapper;

    /** constructor for submission */
    public JobWithStagingImpl(Session session, XJSDLJobDescriptionImpl jobDesc, JobHandle jobHandle, Workflow workflow, DummyTask jobEnd) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, jobDesc, jobHandle);
        m_workflow = workflow;
        m_jobEnd = jobEnd;
        try {
            m_startTask = (DummyTask) m_workflow.getTask(StartTask.NAME);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        }
    }

    //////////////////////////////////////// implementation of JobWithStaging //////////////////////////////////////

    public String getWrapper() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_wrapper;
    }    

    ////////////////////////////////////// implementation of LateBindedJobImpl /////////////////////////////////////

    /** override super.transformJobDescription() */
    protected XJSDLJobDescriptionImpl transformJobDescription(XJSDLJobDescriptionImpl jobDesc, Resource rm) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        // transform job description
        IndividualJobPreprocessor preprocessor = new IndividualJobPreprocessor(jobDesc, rm);
        Document effectiveJobDescDOM = preprocessor.getEffectiveJobDescription();
        jobDesc = new XJSDLJobDescriptionImpl(jobDesc.getCollectionName(), jobDesc.getJobName(), effectiveJobDescDOM);

        // set wrapper script
        m_wrapper = preprocessor.getWrapper();

        // update workflow
        DataStagingTaskGenerator staging = new DataStagingTaskGenerator(jobDesc.getAsDocument(), jobDesc.getJobName());
        staging.updateWorkflow(m_workflow);

        return jobDesc;
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
            case RUNNING:
                return State.RUNNING;
            case CANCELED:
            case FAILED:
            case SUSPENDED:
                return state;
        }
        state = m_startTask.getState();
        switch(state) {
            case DONE:
                if (super.isAllocated()) {
                    return State.RUNNING;
                }
        }
        return State.NEW;
    }
}
