package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.*;
import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.job.instance.stream.*;
import org.apache.log4j.Logger;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;

import java.io.*;
import java.lang.Exception;

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
    /** logger */
    private static Logger s_logger = Logger.getLogger(JobImpl.class);

    private JobControlAdaptor m_controlAdaptor;
    private JobMonitorService m_monitorService;
    private JobAttributes m_attributes;
    private JobMetrics m_metrics;
    private JobDescription m_jobDescription;
    private String m_nativeJobId;
    private JobIOHandler m_IOHandler;
    private OutputStream m_stdin;
    private InputStream m_stdout;
    private InputStream m_stderr;

    /** constructor for submission */
    public JobImpl(Session session, JobDescription jobDesc, String nativeJobDesc, JobControlAdaptor controlAdaptor, JobMonitorService monitorService) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        this(session, controlAdaptor, monitorService, true);
        m_attributes.m_NativeJobDescription.setObject(nativeJobDesc);
        m_jobDescription = jobDesc;
        m_nativeJobId = null;
        boolean isInteractive;
        try {
            isInteractive = "true".equalsIgnoreCase(m_jobDescription.getAttribute(JobDescription.INTERACTIVE));
        } catch (DoesNotExist e) {
            isInteractive = false;
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
        if (isInteractive) {
            if (m_controlAdaptor instanceof InteractiveJobAdaptor) {
                m_IOHandler = ((InteractiveJobAdaptor)m_controlAdaptor).createJobIOHandler();
            } else {
                throw new NotImplemented("Interactive jobs are not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
            }
            if (m_IOHandler == null) {
                throw new NotImplemented("ADAPTOR ERROR: Method createJobIOHandler() must not return null: "+m_controlAdaptor.getClass().getName());
            }
        } else {
            m_IOHandler = null;
        }
        m_stdin = null;
        m_stdout = null;
        m_stderr = null;
    }

    /** constructor for control and monitoring only */
    public JobImpl(Session session, String nativeJobId, JobControlAdaptor controlAdaptor, JobMonitorService monitorService) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        this(session, controlAdaptor, monitorService, false);
        m_jobDescription = null;
        m_nativeJobId = nativeJobId;
        m_IOHandler = null;
        m_stdin = null;
        m_stdout = null;
        m_stderr = null;
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
        clone.m_IOHandler = m_IOHandler;
        clone.m_stdin = m_stdin;
        clone.m_stdout = m_stdout;
        clone.m_stderr = m_stderr;
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
            if (m_IOHandler!=null && m_controlAdaptor instanceof InteractiveJobAdaptor) {
                if (m_stdin!=null && m_stdin instanceof ByteArrayOutputStream) {
                    try{
                        m_stdin.close();
                        byte[] bytes = ((ByteArrayOutputStream)m_stdin).toByteArray();
                        if (m_IOHandler instanceof JobIOSetterPseudo) {
                            ((JobIOSetterPseudo)m_IOHandler).setStdin(new ByteArrayInputStream(bytes));
                        } else if (m_IOHandler instanceof JobIOGetterPseudo) {
                            OutputStream out = ((JobIOGetterPseudo)m_IOHandler).getStdin();
                            out.write(bytes);
                            out.close();
                        }
                    } catch (IOException e){
                        throw new NoSuccess(e);
                    }
                }
                boolean hasStdin = (m_stdin!=null);
                m_nativeJobId = ((InteractiveJobAdaptor)m_controlAdaptor).submitInteractive(nativeJobDesc, s_checkMatch, m_IOHandler, hasStdin);
            } else {
                m_nativeJobId = m_controlAdaptor.submit(nativeJobDesc, s_checkMatch);
            }
            String monitorUrl = m_monitorService.getURL().toString().replaceAll("%20", " ");
            String sagaJobId = "["+monitorUrl+"]-["+m_nativeJobId+"]";
            m_attributes.m_JobId.setObject(sagaJobId);
        } catch (PermissionDenied e) {
            throw new NoSuccess(e);
        }
    }

    protected void doCancel() {
        if (m_nativeJobId == null) {
            throw new RuntimeException("INTERNAL ERROR: JobID not initialized");
        }
        try {
            m_controlAdaptor.cancel(m_nativeJobId);
            this.setState(State.CANCELED, "Canceled by user", SubState.CANCELED);
        } catch (org.ogf.saga.error.Exception e) {
            // do nothing (failed to cancel task)
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
        if (m_IOHandler != null) {
            if (m_stdin != null) {
                // do nothing
            } else if (m_IOHandler instanceof JobIOGetterPseudo || m_IOHandler instanceof JobIOSetterPseudo) {
                m_stdin = new ByteArrayOutputStream();
            } else if (m_IOHandler instanceof JobIOGetter) {
                m_stdin = ((JobIOGetter)m_IOHandler).getStdin();
            } else if (m_IOHandler instanceof JobIOSetter) {
                m_stdin = new PipedStdin((JobIOSetter) m_IOHandler);
            } else {
                throw new NoSuccess("ADAPTOR ERROR: JobIOHandler must be either JobIOGetter or JobIOSetter", this);
            }
            return m_stdin;
        } else {
            throw new IncorrectState("Method getStdin() is allowed on interactive jobs only", this);
        }
    }

    public InputStream getStdout() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        if (m_IOHandler != null) {
            if (m_stdout != null) {
                // do nothing
            } else if (m_IOHandler instanceof JobIOGetterPseudo || m_IOHandler instanceof JobIOSetterPseudo) {
                // do nothing (already set by cleanup)
            } else if (m_IOHandler instanceof JobIOGetter) {
                m_stdout = ((JobIOGetter)m_IOHandler).getStdout();
            } else if (m_IOHandler instanceof JobIOSetter) {
                m_stdout = new PipedStdout((JobIOSetter) m_IOHandler);
            } else {
                throw new NoSuccess("ADAPTOR ERROR: JobIOHandler must be either JobIOGetter or JobIOSetter", this);
            }
            return m_stdout;
        } else {
            throw new IncorrectState("Method getStdout() is allowed on interactive jobs only", this);
        }
    }

    public InputStream getStderr() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        if (m_IOHandler != null) {
            if (m_stderr != null) {
                // do nothing
            } else if (m_IOHandler instanceof JobIOGetterPseudo || m_IOHandler instanceof JobIOSetterPseudo) {
                // do nothing (already set by cleanup)
            } else if (m_IOHandler instanceof JobIOGetter) {
                m_stderr = ((JobIOGetter)m_IOHandler).getStderr();
            } else if (m_IOHandler instanceof JobIOSetter) {
                m_stderr = new PipedStderr((JobIOSetter) m_IOHandler);
            } else {
                throw new NoSuccess("ADAPTOR ERROR: JobIOHandler must be either JobIOGetter or JobIOSetter", this);
            }
            return m_stderr;
        } else {
            throw new IncorrectState("Method getStderr() is allowed on interactive jobs only", this);
        }
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

    private void cleanup() throws PermissionDenied, Timeout, NoSuccess {
        // get job output and error streams
        if (m_IOHandler!=null && m_controlAdaptor instanceof InteractiveJobAdaptor) {
            if (m_IOHandler instanceof JobIOSetterPseudo || m_IOHandler instanceof JobIOGetterPseudo) {
                if (m_IOHandler instanceof JobIOSetterPseudo) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ((JobIOSetterPseudo)m_IOHandler).setStdout(out);
                    m_stdout = new ByteArrayInputStream(out.toByteArray());
                    ByteArrayOutputStream err = new ByteArrayOutputStream();
                    ((JobIOSetterPseudo)m_IOHandler).setStderr(err);
                    m_stderr = new ByteArrayInputStream(err.toByteArray());
                } else if (m_IOHandler instanceof JobIOGetterPseudo) {
                    m_stdout = ((JobIOGetterPseudo)m_IOHandler).getStdout();
                    m_stderr = ((JobIOGetterPseudo)m_IOHandler).getStderr();
                }
            }
        }
        // cleanup job
        if (m_controlAdaptor instanceof CleanableJobAdaptor) {
            ((CleanableJobAdaptor)m_controlAdaptor).clean(m_nativeJobId);
        }
    }

    ////////////////////////////////////// implementation of JobMonitorCallback //////////////////////////////////////

    public void setState(State state, String stateDetail, SubState subState) {
        this.setJobState(state, stateDetail, subState);
        super.setState(state);
    }

    private synchronized void setJobState(State state, String stateDetail, SubState subState) {
        // if not already in a final state
        if (!isFinal(m_metrics.m_State.getValue(State.RUNNING))) {
            // update metrics
            m_metrics.m_State.setValue(state, this);
            m_metrics.m_StateDetail.setValue(stateDetail, this);
            m_metrics.m_SubState.setValue(subState.toString(), this);

            // cleanup job
            if (isFinal(state)) {
                try {
                    this.cleanup();
                } catch (Exception e) {
                    s_logger.warn("Failed to cleanup job: "+m_nativeJobId, e);
                }
            }
        }
    }

    private static boolean isFinal(State state) {
        switch(state) {
            case DONE:
            case CANCELED:
            case FAILED:
                return true;
            default:
                return false;
        }
    }
}
