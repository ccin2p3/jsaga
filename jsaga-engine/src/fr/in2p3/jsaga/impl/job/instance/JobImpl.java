package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.*;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.EngineProperties;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobImpl extends AbstractAsyncJobImpl implements Job, JobMonitorCallback {
    /** Job attribute (deviation from SAGA specification) */
    public static final String NATIVEJOBDESCRIPTION = "NativeJobDescription";
    /** Job metric (deviation from SAGA specification) */
    public static final String JOB_SUBSTATE = "job.sub_state";

    private JobControlAdaptor m_controlAdaptor;
    private JobMonitorService m_monitorService;
    private JobAttributes m_attributes;
    private JobMetrics m_metrics;
    private JobDescription m_jobDescription;
    private String m_nativeJobId;

    /** constructor for submission */
    public JobImpl(Session session, JobDescription jobDesc, String nativeJobDesc, JobControlAdaptor controlAdaptor, JobMonitorService monitorService) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        this(session, controlAdaptor, monitorService, true);
        m_attributes.m_NativeJobDescription.setObject(nativeJobDesc);
        m_jobDescription = jobDesc;
        m_nativeJobId = null;
    }

    /** constructor for control and monitoring only */
    public JobImpl(Session session, String nativeJobId, JobControlAdaptor controlAdaptor, JobMonitorService monitorService) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        this(session, controlAdaptor, monitorService, false);
        m_jobDescription = null;
        m_nativeJobId = nativeJobId;
    }

    /** common to all contructors */
    private JobImpl(Session session, JobControlAdaptor controlAdaptor, JobMonitorService monitorService, boolean create) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, create);
        m_attributes = new JobAttributes(this);
        m_metrics = new JobMetrics(this);
        m_controlAdaptor = controlAdaptor;
        m_monitorService = monitorService;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobImpl clone = (JobImpl) super.clone();
        clone.m_attributes = m_attributes.clone();
        clone.m_metrics = m_metrics.clone();
        clone.m_controlAdaptor = m_controlAdaptor;
        clone.m_monitorService = m_monitorService;
        clone.m_jobDescription = m_jobDescription;
        clone.m_nativeJobId = m_nativeJobId;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.JOB;
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    private static boolean s_checkMatch = EngineProperties.getBoolean(EngineProperties.JOB_CONTROL_CHECK_MATCH);
    protected void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        try {
            String nativeJobDesc = m_attributes.m_NativeJobDescription.getObject();
            m_nativeJobId = m_controlAdaptor.submit(nativeJobDesc, s_checkMatch);
            String monitorUrl = m_monitorService.getURL().toString().replaceAll("%20", " ");
            String sagaJobId = "["+monitorUrl+"]-["+m_nativeJobId+"]";
            m_attributes.m_JobId.setObject(sagaJobId);
        } catch (PermissionDenied e) {
            throw new NoSuccess(e);
        }
    }

    protected boolean doCancel() {
        if (m_nativeJobId == null) {
            throw new RuntimeException("INTERNAL ERROR: JobID not initialized");
        }
        try {
            m_controlAdaptor.cancel(m_nativeJobId);
            return true;
        } catch (org.ogf.saga.error.Exception e) {
            return false;
        }
    }

    protected State queryState() throws NotImplemented, Timeout, NoSuccess {
        JobStatus status = m_monitorService.getState(m_nativeJobId);
        this.setJobState(status.getSagaState(), status.getStateDetail(), status.getSubState());
        return status.getSagaState();
    }

    public boolean startListening(Metric metric) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        if (m_nativeJobId == null) {
            throw new IncorrectState("Can not listen to job in 'New' state", this);
        }
        m_monitorService.startListening(m_nativeJobId, this);
        return true;    // a job task is always listening (either with notification, or with polling)
    }

    public void stopListening(Metric metric) throws NotImplemented, Timeout, NoSuccess {
        if (m_nativeJobId == null) {
            throw new RuntimeException("INTERNAL ERROR: JobID not initialized");
        }
        m_monitorService.stopListening(m_nativeJobId);
    }

    ////////////////////////////////////// implementation of Job //////////////////////////////////////

    public JobDescription getJobDescription() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_jobDescription;
    }

    public OutputStream getStdin() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        throw new NotImplemented("Interactive jobs are currently not supported by JSAGA", this);
    }

    public InputStream getStdout() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        throw new NotImplemented("Interactive jobs are currently not supported by JSAGA", this);
    }

    public InputStream getStderr() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        throw new NotImplemented("Interactive jobs are currently not supported by JSAGA", this);
    }

    public void suspend() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_nativeJobId == null) {
            throw new IncorrectState("Can not suspend job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof HoldableJobAdaptor && m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).hold(m_nativeJobId)) {
                if (! ((SuspendableJobAdaptor)m_controlAdaptor).suspend(m_nativeJobId)) {
                    throw new NoSuccess("Failed to hold/suspend job because it is neither queued nor active: "+m_nativeJobId);
                }
            }
        } else if (m_controlAdaptor instanceof HoldableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).hold(m_nativeJobId)) {
                throw new NoSuccess("Failed to hold job because it is not queued: "+m_nativeJobId);
            }
        } else if (m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((SuspendableJobAdaptor)m_controlAdaptor).suspend(m_nativeJobId)) {
                throw new NoSuccess("Failed to suspend job because if is not active: "+m_nativeJobId);
            }
        } else {
            throw new NotImplemented("Suspend is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    public void resume() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_nativeJobId == null) {
            throw new IncorrectState("Can not resume job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof HoldableJobAdaptor && m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).release(m_nativeJobId)) {
                if (! ((SuspendableJobAdaptor)m_controlAdaptor).resume(m_nativeJobId)) {
                    throw new NoSuccess("Failed to release/resume job because it is neither held nor suspended: "+m_nativeJobId);
                }
            }
        } else if (m_controlAdaptor instanceof HoldableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).release(m_nativeJobId)) {
                throw new NoSuccess("Failed to release job because it is not held: "+m_nativeJobId);
            }
        } else if (m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((SuspendableJobAdaptor)m_controlAdaptor).resume(m_nativeJobId)) {
                throw new NoSuccess("Failed to resume job because if is not suspended: "+m_nativeJobId);
            }
        } else {
            throw new NotImplemented("Resume is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    public void checkpoint() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_nativeJobId == null) {
            throw new IncorrectState("Can not checkpoint job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof CheckpointableJobAdaptor) {
            if (! ((CheckpointableJobAdaptor)m_controlAdaptor).checkpoint(m_nativeJobId)) {
                throw new NoSuccess("Failed to checkpoint job: "+m_nativeJobId);
            }
        } else {
            throw new NotImplemented("Checkpoint is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    public void migrate(JobDescription jd) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_nativeJobId == null) {
            throw new IncorrectState("Can not migrate job in 'New' state", this);
        }
        throw new NotImplemented("Not implemented yet..."); //todo: implement method migrate()
//        if (super.cancel(true)) {   //synchronous cancel (not the SAGA cancel)
    }

    public void signal(int signum) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_nativeJobId == null) {
            throw new IncorrectState("Can not send signal to job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof SignalableJobAdaptor) {
            if (! ((SignalableJobAdaptor)m_controlAdaptor).signal(m_nativeJobId, signum)) {
                throw new NoSuccess("Failed to signal job: "+m_nativeJobId);
            }
        } else {
            throw new NotImplemented("Signal is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    ////////////////////////////////////// implementation of JobMonitorCallback //////////////////////////////////////

    public void setState(State state, String stateDetail, SubState subState) {
        this.setJobState(state, stateDetail, subState);
        super.setState(state);
    }

    private void setJobState(State state, String stateDetail, SubState subState) {
        switch(m_metrics.m_State.getValue(State.RUNNING)) {
            case DONE:
            case CANCELED:
            case FAILED:
                return;
            default:
                m_metrics.m_State.setValue(state, this);
                m_metrics.m_StateDetail.setValue(stateDetail, this);
                m_metrics.m_SubState.setValue(subState.toString(), this);
        }
    }
}
