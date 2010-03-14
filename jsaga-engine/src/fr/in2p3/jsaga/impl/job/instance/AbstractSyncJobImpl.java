package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.*;
import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.job.instance.stream.*;
import fr.in2p3.jsaga.impl.job.service.AbstractSyncJobServiceImpl;
import fr.in2p3.jsaga.impl.job.staging.mgr.DataStagingManager;
import fr.in2p3.jsaga.impl.permissions.AbstractJobPermissionsImpl;
import fr.in2p3.jsaga.sync.job.SyncJob;
import org.apache.log4j.Logger;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URL;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractSyncJobImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractSyncJobImpl extends AbstractJobPermissionsImpl implements SyncJob, JobMonitorCallback {
    /** Job attribute (deviation from SAGA specification) */
    public static final String NATIVEJOBDESCRIPTION = "NativeJobDescription";
    /** Job metric (deviation from SAGA specification) */
    public static final String JOB_SUBSTATE = "job.sub_state";
    /** logger */
    private static Logger s_logger = Logger.getLogger(AbstractSyncJobImpl.class);

    protected URL m_resourceManager;
    private JobControlAdaptor m_controlAdaptor;
    private JobMonitorService m_monitorService;
    private JobAttributes m_attributes;
    private JobMetrics m_metrics;
    private JobDescription m_jobDescription;
    private DataStagingManager m_stagingMgr;
    private String m_uniqId;
    private String m_nativeJobId;
    private JobIOHandler m_IOHandler;
    private Stdin m_stdin;
    private Stdout m_stdout;
    private Stdout m_stderr;

    /** constructor for submission */
    protected AbstractSyncJobImpl(Session session, String nativeJobDesc, JobDescription jobDesc, DataStagingManager stagingMgr, String uniqId, AbstractSyncJobServiceImpl service) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        this(session, service, true);
        m_attributes.m_NativeJobDescription.setObject(nativeJobDesc);
        m_jobDescription = jobDesc;
        m_stagingMgr = stagingMgr;
        m_uniqId = uniqId;
        m_nativeJobId = null;
    }

    /** constructor for control and monitoring only */
    protected AbstractSyncJobImpl(Session session, String nativeJobId, AbstractSyncJobServiceImpl service) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        this(session, service, false);
        m_attributes.m_NativeJobDescription.setObject(null);
        m_jobDescription = null;
        m_stagingMgr = null;
        m_uniqId = null;
        m_nativeJobId = nativeJobId;
    }

    /** common to all contructors */
    private AbstractSyncJobImpl(Session session, AbstractSyncJobServiceImpl service, boolean create) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, create);
        m_attributes = new JobAttributes(this);
        m_metrics = new JobMetrics(this);
        m_resourceManager = service.m_resourceManager;
        m_controlAdaptor = service.m_controlAdaptor;
        m_monitorService = service.m_monitorService;
        m_IOHandler = null;
        m_stdin = null;
        m_stdout = null;
        m_stderr = null;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractSyncJobImpl clone = (AbstractSyncJobImpl) super.clone();
        clone.m_attributes = m_attributes.clone();
        clone.m_metrics = m_metrics.clone();
        clone.m_resourceManager = m_resourceManager;
        clone.m_controlAdaptor = m_controlAdaptor;
        clone.m_monitorService = m_monitorService;
        clone.m_jobDescription = m_jobDescription;
        clone.m_stagingMgr = m_stagingMgr;
        clone.m_uniqId = m_uniqId;
        clone.m_nativeJobId = m_nativeJobId;
        clone.m_IOHandler = m_IOHandler;
        clone.m_stdin = m_stdin;
        clone.m_stdout = m_stdout;
        clone.m_stderr = m_stderr;
        return clone;
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    private static boolean s_checkMatch = EngineProperties.getBoolean(EngineProperties.JOB_CONTROL_CHECK_MATCH);
    protected void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        try {
            // pre-staging
            m_metrics.m_SubState.setValue(SubState.RUNNING_PRE_STAGING.toString());
            m_stagingMgr.preStaging(this);

            // submit
            String nativeJobDesc = m_attributes.m_NativeJobDescription.getObject();
            if (this.isInteractive()) {
                if (m_controlAdaptor instanceof StreamableJobInteractiveGet) {
                    // submit
                    JobIOGetterInteractive ioHandler = ((StreamableJobInteractiveGet)m_controlAdaptor).submitInteractive(nativeJobDesc, s_checkMatch);
                    if (ioHandler == null) {
                        throw new NotImplementedException("ADAPTOR ERROR: Method submitInteractive() must not return null: "+m_controlAdaptor.getClass().getName());
                    }

                    // set stdin
                    if (m_stdin == null) {
                        m_stdin = new JobStdinOutputStream(this);
                    }
                    m_stdin.openJobIOHandler(ioHandler);

                    // set stdout and stderr
                    if (m_stdout == null) {
                        m_stdout = new GetterInputStream(ioHandler.getStdout());
                    }
                    if (m_stderr == null) {
                        m_stderr = new GetterInputStream(ioHandler.getStderr());
                    }

                    m_IOHandler = ioHandler;
                    m_nativeJobId = m_IOHandler.getJobId();
                } else if (m_controlAdaptor instanceof StreamableJobInteractiveSet) {
                    // set stdin
                    InputStream stdin = null;
                    if (m_stdin != null) {
                        stdin = ((PostconnectedStdinOutputStream)m_stdin).getInputStreamContainer();
                    }

                    // set stdout and stderr
                    if (m_stdout == null) {
                        m_stdout = new PreconnectedStdoutInputStream(this);
                    }
                    OutputStream stdout = ((PreconnectedStdoutInputStream)m_stdout).getOutputStreamContainer();
                    if (m_stderr == null) {
                        m_stderr = new PreconnectedStderrInputStream(this);
                    }
                    OutputStream stderr = ((PreconnectedStderrInputStream)m_stderr).getOutputStreamContainer();

                    // submit
                    m_nativeJobId = ((StreamableJobInteractiveSet)m_controlAdaptor).submitInteractive(
                            nativeJobDesc, s_checkMatch,
                            stdin, stdout, stderr);
                } else if (m_controlAdaptor instanceof StreamableJobBatch) {
                    // set stdin
                    InputStream stdin;
                    if (m_stdin!=null && m_stdin.getBuffer().length>0) {
                        stdin = new ByteArrayInputStream(m_stdin.getBuffer());
                    } else {
                        stdin = null;
                    }

                    // submit
                    m_IOHandler = ((StreamableJobBatch)m_controlAdaptor).submit(nativeJobDesc, s_checkMatch, m_uniqId, stdin);
                    if (m_IOHandler == null) {
                        throw new NotImplementedException("ADAPTOR ERROR: Method submit() must not return null: "+m_controlAdaptor.getClass().getName());
                    }
                    m_nativeJobId = m_IOHandler.getJobId();
                } else {
                    throw new NotImplementedException("Interactive jobs are not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
                }
            } else {
                m_nativeJobId = m_controlAdaptor.submit(nativeJobDesc, s_checkMatch, m_uniqId);
            }
            String monitorUrl = m_monitorService.getURL().getString();
            String sagaJobId = "["+monitorUrl+"]-["+m_nativeJobId+"]";
            m_attributes.m_JobId.setObject(sagaJobId);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        } catch (PermissionDeniedException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }

    protected void doCancel() {
        try{m_monitorService.checkState();} catch(TimeoutException e){throw new RuntimeException(e);}
        if (m_nativeJobId == null) {
            throw new RuntimeException("INTERNAL ERROR: JobID not initialized");
        }
        try {
            m_controlAdaptor.cancel(m_nativeJobId);
            this.setState(State.CANCELED, "Canceled by user", SubState.CANCELED, new IncorrectStateException("Canceled by user"));
        } catch (SagaException e) {
            // do nothing (failed to cancel task)
        }
    }

    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        JobStatus status = m_monitorService.getState(m_nativeJobId);
        // set job state
        this.setJobState(status.getSagaState(), status.getStateDetail(), status.getSubState(), status.getCause());
        // return task state (may trigger finish task)
        return status.getSagaState();
    }

    public boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not listen to job in 'New' state", this);
        }
        m_monitorService.startListening(m_nativeJobId, this);
        return true;    // a job task is always listening (either with notification, or with polling)
    }

    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new RuntimeException("INTERNAL ERROR: JobID not initialized");
        }
        m_monitorService.stopListening(m_nativeJobId);

        // close job output and error streams
        boolean isDone = (State.DONE.compareTo(m_metrics.m_State.getValue()) == 0);
        if (isDone && m_IOHandler!=null) {  //if job is done and interactive
            if (m_controlAdaptor instanceof StreamableJobInteractiveGet || m_controlAdaptor instanceof StreamableJobBatch) {
                try {
                    if (m_stdout == null) {
                        m_stdout = new JobStdoutInputStream(this, m_IOHandler);
                    }
                    m_stdout.closeJobIOHandler();
                    if (m_stderr == null) {
                        m_stderr = new JobStderrInputStream(this, m_IOHandler);
                    }
                    m_stderr.closeJobIOHandler();
                } catch (Exception e) {
                    s_logger.warn("Failed to get job output/error streams: "+m_nativeJobId, e);
                }
            }
        }

        // staging
        try {
            // post-staging
            if (isDone) {
                m_stagingMgr.postStaging(this);
            }

            // cleanup staged files
            if (this.isFinalState()) {
                m_stagingMgr.cleanup(this);
            }
        }
        catch (NotImplementedException e) {throw e;}
        catch (TimeoutException e) {throw e;}
        catch (NoSuccessException e) {throw e;}
        catch (SagaException e) {
            throw new NoSuccessException(e);
        }

        // cleanup job
        if (this.isFinalState() && m_controlAdaptor instanceof CleanableJobAdaptor) {
            try {
                ((CleanableJobAdaptor)m_controlAdaptor).clean(m_nativeJobId);
            } catch (SagaException e) {
                s_logger.warn("Failed to cleanup job: "+m_nativeJobId, e);
            }
        }
    }

    public void cleanup() throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        State state = this.getState();
        if (! this.isFinalState()) {
            throw new IncorrectStateException("Can not cleanup unfinished job: "+state, this);
        }

        if (m_controlAdaptor instanceof CleanableJobAdaptor) {
            ((CleanableJobAdaptor)m_controlAdaptor).clean(m_nativeJobId);
        } else {
            throw new NotImplementedException("Cleanup is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    ////////////////////////////////////// implementation of Job //////////////////////////////////////

    public JobDescription getJobDescriptionSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_jobDescription;
    }

    public OutputStream getStdinSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_monitorService.checkState();
        if (this.isInteractive()) {
            if (m_stdin == null) {
                if (m_controlAdaptor instanceof StreamableJobInteractiveSet) {
                    m_stdin = new PostconnectedStdinOutputStream(this);
                } else {
                    m_stdin = new JobStdinOutputStream(this);
                }
            }
            return m_stdin;
        } else {
            throw new IncorrectStateException("Method getStdin() is allowed on interactive jobs only", this);
        }
    }

    public InputStream getStdoutSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_monitorService.checkState();
        if (this.isInteractive()) {
            if (m_stdout == null) {
                m_stdout = new JobStdoutInputStream(this, m_IOHandler);
            }
            return m_stdout;
        } else {
            throw new IncorrectStateException("Method getStdout() is allowed on interactive jobs only", this);
        }
    }

    public InputStream getStderrSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_monitorService.checkState();
        if (this.isInteractive()) {
            if (m_stderr == null) {
                m_stderr = new JobStderrInputStream(this, m_IOHandler);
            }
            return m_stderr;
        } else {
            throw new IncorrectStateException("Method getStderr() is allowed on interactive jobs only", this);
        }
    }

    public void suspendSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not suspend job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof HoldableJobAdaptor && m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).hold(m_nativeJobId)) {
                if (! ((SuspendableJobAdaptor)m_controlAdaptor).suspend(m_nativeJobId)) {
                    throw new NoSuccessException("Failed to hold/suspend job because it is neither queued nor active: "+m_nativeJobId);
                }
            }
        } else if (m_controlAdaptor instanceof HoldableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).hold(m_nativeJobId)) {
                throw new NoSuccessException("Failed to hold job because it is not queued: "+m_nativeJobId);
            }
        } else if (m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((SuspendableJobAdaptor)m_controlAdaptor).suspend(m_nativeJobId)) {
                throw new NoSuccessException("Failed to suspend job because if is not active: "+m_nativeJobId);
            }
        } else {
            throw new NotImplementedException("Suspend is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    public void resumeSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not resume job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof HoldableJobAdaptor && m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).release(m_nativeJobId)) {
                if (! ((SuspendableJobAdaptor)m_controlAdaptor).resume(m_nativeJobId)) {
                    throw new NoSuccessException("Failed to release/resume job because it is neither held nor suspended: "+m_nativeJobId);
                }
            }
        } else if (m_controlAdaptor instanceof HoldableJobAdaptor) {
            if (! ((HoldableJobAdaptor)m_controlAdaptor).release(m_nativeJobId)) {
                throw new NoSuccessException("Failed to release job because it is not held: "+m_nativeJobId);
            }
        } else if (m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (! ((SuspendableJobAdaptor)m_controlAdaptor).resume(m_nativeJobId)) {
                throw new NoSuccessException("Failed to resume job because if is not suspended: "+m_nativeJobId);
            }
        } else {
            throw new NotImplementedException("Resume is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    public void checkpointSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not checkpoint job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof CheckpointableJobAdaptor) {
            if (! ((CheckpointableJobAdaptor)m_controlAdaptor).checkpoint(m_nativeJobId)) {
                throw new NoSuccessException("Failed to checkpoint job: "+m_nativeJobId);
            }
        } else {
            throw new NotImplementedException("Checkpoint is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    public void migrateSync(JobDescription jd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not migrate job in 'New' state", this);
        }
        throw new NotImplementedException("Not implemented yet..."); //todo: implement method migrate()
//        if (super.cancel(true)) {   //synchronous cancel (not the SAGA cancel)
    }

    public void signalSync(int signum) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not send signal to job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof SignalableJobAdaptor) {
            if (! ((SignalableJobAdaptor)m_controlAdaptor).signal(m_nativeJobId, signum)) {
                throw new NoSuccessException("Failed to signal job: "+m_nativeJobId);
            }
        } else {
            throw new NotImplementedException("Signal is not supported by this adaptor: "+m_controlAdaptor.getClass().getName());
        }
    }

    /////////////////////////////////////////// implementation of AbstractSyncJobImpl ////////////////////////////////////////////

    public State getJobState() {
        return m_metrics.m_State.getValue();
    }

    ////////////////////////////////////// implementation of JobMonitorCallback //////////////////////////////////////

    /**
     * Set job and task state (may finish task)
     */
    public void setState(State state, String stateDetail, SubState subState, SagaException cause) {
        // log
        if (! stateDetail.equals(m_metrics.m_StateDetail.getValue())) {
            m_monitorService.getStateLogger().debug("State changed to "+stateDetail+" for job "+m_attributes.m_JobId.getObject());
        }
        // set job state
        this.setJobState(state, stateDetail, subState, cause);
        // set task state (may finish task)
        super.setState(state);
    }

    /**
     * Set job state only
     */
    private synchronized void setJobState(State state, String stateDetail, SubState subState, SagaException cause) {
        // if not already in a final state
        if (! this.isFinalState()) {
            // update cause
            if (cause != null) {
                super.setException(cause);
            }
            // update metrics
            m_metrics.m_State.setValue(state);
            m_metrics.m_StateDetail.setValue(stateDetail);
            m_metrics.m_SubState.setValue(subState.toString());
        }
    }

    /////////////////////////////////////// friend methods //////////////////////////////////////

    String getNativeJobId() {
        return m_nativeJobId;
    }

    JobInfoAdaptor getJobInfoAdaptor() throws NotImplementedException {
        JobMonitorAdaptor monitorAdaptor = m_monitorService.getAdaptor();
        if (monitorAdaptor instanceof JobInfoAdaptor) {
            return (JobInfoAdaptor) monitorAdaptor;
        } else {
            throw new NotImplementedException("Job attribute not supported by this adaptor: "+m_resourceManager.getScheme());
        }
    }

    ////////////////////////////////////// private methods //////////////////////////////////////

    private boolean isFinalState() {
        // do not use getState_fromCache() because it may lead to infinite recursion
        State state = m_metrics.m_State.getValue();
        if (state == null)
            state = State.RUNNING;

        // if state is terminal
        switch(state) {
            case DONE:
            case CANCELED:
            case FAILED:
                return true;
            default:
                return false;
        }
    }

    private boolean isInteractive() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            return "true".equalsIgnoreCase(m_jobDescription.getAttribute(JobDescription.INTERACTIVE));
        } catch (DoesNotExistException e) {
            return false;
        }
    }
}
