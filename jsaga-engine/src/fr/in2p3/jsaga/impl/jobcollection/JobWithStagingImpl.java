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
    private JobCollectionImpl m_workflow;
    private DummyTask m_jobEnd;
    private DummyTask m_startTask;
    private String m_wrapper;

    /** constructor for submission */
    public JobWithStagingImpl(Session session, XJSDLJobDescriptionImpl jobDesc, JobHandle jobHandle, JobCollectionImpl workflow, DummyTask jobEnd) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, jobDesc, jobHandle);
        m_workflow = workflow;
        m_jobEnd = jobEnd;
        try {
            m_startTask = (DummyTask) m_workflow.getTask(StartTask.NAME);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        }
    }

    //////////////////////////////////////// implementation of JobWithStaging //////////////////////////////////////

    public String getWrapper() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_wrapper;
    }    

    ////////////////////////////////////// implementation of LateBindedJobImpl /////////////////////////////////////

    /** override super.transformJobDescription() */
    protected XJSDLJobDescriptionImpl transformJobDescription(JobHandle jobHandle, XJSDLJobDescriptionImpl jobDesc, Resource rm) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        // transform job description
        IndividualJobPreprocessor preprocessor = new IndividualJobPreprocessor(jobDesc, rm);
        Document effectiveJobDescDOM = preprocessor.getEffectiveJobDescription();
        jobDesc = new XJSDLJobDescriptionImpl(jobDesc.getCollectionName(), jobDesc.getJobName(), effectiveJobDescDOM);

        // set wrapper script
        m_wrapper = preprocessor.getWrapper();
        jobHandle.setInputFile(preprocessor.getWrapperFile());

        // update workflow
        DataStagingTaskGenerator staging = new DataStagingTaskGenerator(jobDesc.getJobName(), jobDesc.getAsDocument());
        staging.updateWorkflow(m_session, m_workflow);
        m_workflow.saveStatesAsXML();

        return jobDesc;
    }

    //////////////////////////////////////////// interface TaskCallback ////////////////////////////////////////////

    public synchronized void setState(State state) {
        State jobEndState;
        try {
            jobEndState = m_jobEnd.getState();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        switch(jobEndState) {
            case DONE:
                super.setState(jobEndState);
                break;
            case CANCELED:
            case FAILED:
                try {
                    m_jobHandle.rethrow();
                    m_jobEnd.rethrow();
                } catch (SagaException e) {
                    super.setException(e);
                }
                super.setState(jobEndState);
                break;
            default:
                switch(state) {
                    case DONE:
                    case RUNNING:
                        super.setState(State.RUNNING);
                        break;
                    case CANCELED:
                    case FAILED:
                    case SUSPENDED:
                        super.setState(state);
                        break;
                }
                break;
        }
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    /** override super.queryState() */
    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        State state = m_jobEnd.getState();
        switch(state) {
            case DONE:
            case CANCELED:
            case FAILED:
                return state;
        }
        state = super.queryState();
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
