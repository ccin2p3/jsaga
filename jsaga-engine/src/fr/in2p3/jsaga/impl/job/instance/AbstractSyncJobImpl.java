package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.*;
import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.attributes.ScalarAttributeImpl;
import fr.in2p3.jsaga.impl.attributes.VectorAttributeImpl;
import fr.in2p3.jsaga.impl.job.instance.stream.*;
import fr.in2p3.jsaga.impl.job.service.AbstractSyncJobServiceImpl;
import fr.in2p3.jsaga.impl.job.staging.mgr.*;
import fr.in2p3.jsaga.impl.job.streaming.GenericStreamableJobAdaptor;
import fr.in2p3.jsaga.impl.job.streaming.mgr.StreamingManagerThroughSandbox;
import fr.in2p3.jsaga.impl.job.streaming.mgr.StreamingManagerThroughSandboxTwoPhase;
import fr.in2p3.jsaga.impl.permissions.AbstractJobPermissionsImpl;
import fr.in2p3.jsaga.sync.job.SyncJob;
import org.apache.log4j.Logger;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.file.Directory;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.namespace.Flags;

import java.io.*;
import java.util.Date;
import org.ogf.saga.task.TaskMode;

/*
 * ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) *** *** http://cc.in2p3.fr/
 * *** *************************************************** File:
 * AbstractSyncJobImpl Author: Sylvain Reynaud (sreynaud@in2p3.fr) Date: 26 oct.
 * 2007 *************************************************** Description:
 */
/**
 *
 */
public abstract class AbstractSyncJobImpl extends AbstractJobPermissionsImpl implements SyncJob, JobMonitorCallback {

    /**
     * Job state detail engine model
     */
    private static final String MODEL = "JSAGA";
    /**
     * Job attribute (deviation from SAGA specification)
     */
    public static final String NATIVEJOBDESCRIPTION = "NativeJobDescription";
    /**
     * Job vector attribute (deviation from SAGA specification)
     */
    public static final String OUTPUTURL = "OutputURL";
    /**
     * logger
     */
    private static Logger s_logger = Logger.getLogger(AbstractSyncJobImpl.class);
    private JobControlAdaptor m_controlAdaptor;
    private GenericStreamableJobAdaptor m_genericStreamableJobAdaptor;
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
    private boolean m_willStartListening;
    private String m_currentModelState;

    /**
     * constructor for submission
     */
    protected AbstractSyncJobImpl(Session session, String nativeJobDesc, JobDescription jobDesc, DataStagingManager stagingMgr, String uniqId, AbstractSyncJobServiceImpl service) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        this(session, service, true);
        m_attributes.m_NativeJobDescription.setObject(nativeJobDesc);
        m_jobDescription = jobDesc;
        m_stagingMgr = stagingMgr;
        m_uniqId = uniqId;
        m_nativeJobId = null;
        m_currentModelState = null;
        m_genericStreamableJobAdaptor = null;
    }

    /**
     * constructor for control and monitoring only
     */
    protected AbstractSyncJobImpl(Session session, String nativeJobId, DataStagingManager stagingMgr, AbstractSyncJobServiceImpl service) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        this(session, service, false);
        m_attributes.m_NativeJobDescription.setObject(null);
        m_jobDescription = null;
        m_stagingMgr = stagingMgr;
        m_uniqId = null;
        m_nativeJobId = nativeJobId;
        m_currentModelState = null;
        m_genericStreamableJobAdaptor = null;
    }

    /**
     * common to all contructors
     */
    private AbstractSyncJobImpl(Session session, AbstractSyncJobServiceImpl service, boolean create) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, create);
        m_attributes = new JobAttributes(this);
        m_attributes.m_ServiceUrl.setObject(service.m_resourceManager.getString());
        m_metrics = new JobMetrics(this);
        m_controlAdaptor = service.m_controlAdaptor;
        m_monitorService = service.m_monitorService;
        m_IOHandler = null;
        m_stdin = null;
        m_stdout = null;
        m_stderr = null;
        m_willStartListening = false;
        m_currentModelState = null;
        m_genericStreamableJobAdaptor = null;
    }

    /**
     * clone
     */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractSyncJobImpl clone = (AbstractSyncJobImpl) super.clone();
        clone.m_attributes = m_attributes.clone();
        clone.m_metrics = m_metrics.clone();
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
        clone.m_genericStreamableJobAdaptor = m_genericStreamableJobAdaptor;
        return clone;
    }
    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////
    private static boolean s_checkMatch = EngineProperties.getBoolean(EngineProperties.JOB_CONTROL_CHECK_MATCH);

    protected void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        try {
            // get native job description
            String nativeJobDesc = m_attributes.m_NativeJobDescription.getObject();

            // pre-staging (before job submit)
            m_metrics.m_StateDetail.setValue(MODEL + ":" + SubState.RUNNING_PRE_STAGING.toString());
            if (m_stagingMgr instanceof DataStagingManagerThroughStream) {
                ((DataStagingManagerThroughStream) m_stagingMgr).preStaging(this);
            } else if (m_stagingMgr instanceof DataStagingManagerThroughSandboxOnePhase) {
                ((DataStagingManagerThroughSandboxOnePhase) m_stagingMgr).preStaging(this, nativeJobDesc, m_uniqId);
            }

            // submit
            if (this.isInteractive()) {
                if (m_controlAdaptor instanceof StreamableJobInteractiveGet) {
                    // submit
                    JobIOGetterInteractive ioHandler = ((StreamableJobInteractiveGet) m_controlAdaptor).submitInteractive(nativeJobDesc, s_checkMatch);
                    if (ioHandler == null) {
                        throw new NotImplementedException("ADAPTOR ERROR: Method submitInteractive() must not return null: " + m_controlAdaptor.getClass().getName());
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
                        stdin = ((PostconnectedStdinOutputStream) m_stdin).getInputStreamContainer();
                    }

                    // set stdout and stderr
                    if (m_stdout == null) {
                        m_stdout = new PreconnectedStdoutInputStream(this);
                    }
                    OutputStream stdout = ((PreconnectedStdoutInputStream) m_stdout).getOutputStreamContainer();
                    if (m_stderr == null) {
                        m_stderr = new PreconnectedStderrInputStream(this);
                    }
                    OutputStream stderr = ((PreconnectedStderrInputStream) m_stderr).getOutputStreamContainer();

                    // submit
                    m_nativeJobId = ((StreamableJobInteractiveSet) m_controlAdaptor).submitInteractive(
                            nativeJobDesc, s_checkMatch,
                            stdin, stdout, stderr);
                } else if (m_controlAdaptor instanceof StreamableJobBatch) {
                    // set stdin
                    InputStream stdin;
                    if (m_stdin != null && m_stdin.getBuffer().length > 0) {
                        stdin = new ByteArrayInputStream(m_stdin.getBuffer());
                    } else {
                        stdin = null;
                    }

                    // submit
                    m_IOHandler = ((StreamableJobBatch) m_controlAdaptor).submit(nativeJobDesc, s_checkMatch, m_uniqId, stdin);
                    if (m_IOHandler == null) {
                        throw new NotImplementedException("ADAPTOR ERROR: Method submit() must not return null: " + m_controlAdaptor.getClass().getName());
                    }
                    m_nativeJobId = m_IOHandler.getJobId();
                } else {
                    throw new NotImplementedException("Interactive jobs are not supported by this adaptor: " + m_controlAdaptor.getClass().getName());
                }
            } else {
                if (m_stagingMgr instanceof StreamingManagerThroughSandboxTwoPhase) {
                    m_genericStreamableJobAdaptor = new GenericStreamableJobAdaptor((StagingJobAdaptor) m_controlAdaptor);
                    // set stdin
                    InputStream stdin;
                    if (m_stdin != null && m_stdin.getBuffer().length > 0) {
                        stdin = new ByteArrayInputStream(m_stdin.getBuffer());
                    } else {
                        stdin = null;
                    }

                    // submit
                    m_IOHandler = m_genericStreamableJobAdaptor.submit(nativeJobDesc, s_checkMatch, m_uniqId, stdin);
                    if (m_IOHandler == null) {
                        throw new NotImplementedException("ADAPTOR ERROR: Method submit() must not return null: " + m_genericStreamableJobAdaptor.getClass().getName());
                    }
                    m_nativeJobId = m_IOHandler.getJobId();
                } else {
                    m_nativeJobId = m_controlAdaptor.submit(nativeJobDesc, s_checkMatch, m_uniqId);
                }
            }
            String monitorUrl = m_monitorService.getURL().getString();
            String sagaJobId = "[" + monitorUrl + "]-[" + m_nativeJobId + "]";
            m_attributes.m_JobId.setObject(sagaJobId);

            // start listening if a callback was registered
            if (m_willStartListening) {
                m_willStartListening = false;
                this.startListening();
            }

            // pre-staging (after job register)
            if (m_stagingMgr instanceof DataStagingManagerThroughSandboxTwoPhase) {
                ((DataStagingManagerThroughSandboxTwoPhase) m_stagingMgr).preStaging(this, m_nativeJobId);
            }

            // start job
            if (m_controlAdaptor instanceof StagingJobAdaptorTwoPhase) {
                ((StagingJobAdaptorTwoPhase) m_controlAdaptor).start(m_nativeJobId);
            }
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
        try {
            m_monitorService.checkState();
        } catch (SagaException e) {
            throw new RuntimeException(e);
        }
        if (m_nativeJobId == null) {
            throw new RuntimeException("INTERNAL ERROR: JobID not initialized");
        }
        try {
            m_controlAdaptor.cancel(m_nativeJobId);
            this.setState(State.CANCELED, "USER:Canceled", SubState.CANCEL_REQUESTED, new IncorrectStateException("Canceled by user"));
        } catch (SagaException e) {
            // do nothing (failed to cancel task)
            s_logger.warn("Could not cancel job " + m_nativeJobId + ": " + e.getMessage());
        }
    }

    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        JobStatus status = m_monitorService.getState(m_nativeJobId);
        // set job state
        this.setJobState(status.getSagaState(), status.getStateDetail(), status.getSubState(), status.getCause());

        // close job output and error streams
        this.closeStreamsIfDoneAndInteractive();

        // return task state (may trigger finish task)
        return status.getSagaState();
    }

    public boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            m_willStartListening = true;
        } else {
            m_monitorService.startListening(m_nativeJobId, this);
        }
        return true;    // a job task is always listening (either with notification, or with polling)
    }

    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            return;
        }
        m_monitorService.stopListening(m_nativeJobId);

        if (!(m_stagingMgr instanceof StreamingManagerThroughSandbox)) {
            // close job output and error streams
            this.closeStreamsIfDoneAndInteractive();
        }

        if (this.isFinalState()) {
            // post-staging
            try {
                this.postStaging();
                if (m_stagingMgr instanceof StreamingManagerThroughSandbox) {
                    // close job output and error streams after data staging
                    this.closeStreamsIfDoneAndInteractive();
                }
            } catch (PermissionDeniedException e) {
                throw new NoSuccessException(e);
            } catch (IncorrectStateException e) {
                throw new NoSuccessException(e);
            }
            // cleanup
            try {
                this.cleanUp();
            } catch (SagaException e) {
                s_logger.warn("Failed to cleanup job: " + m_nativeJobId, e);
            }
        }
    }

    private void closeStreamsIfDoneAndInteractive() {
        if (this.isFinalState() && m_IOHandler != null) {  //if job is done and interactive
            if (m_controlAdaptor instanceof StreamableJobInteractiveGet || m_controlAdaptor instanceof StreamableJobBatch || m_stagingMgr instanceof StreamingManagerThroughSandbox) {
                try {
                    if (m_stdout == null) {
                        m_stdout = new JobStdoutInputStream(this, m_IOHandler);
                    }
                    m_stdout.closeJobIOHandler();
                } catch (Exception e) {
                    s_logger.warn("Failed to get job output stream: " + m_nativeJobId, e);
                }
                try {
                    if (m_stderr == null) {
                        m_stderr = new JobStderrInputStream(this, m_IOHandler);
                    }
                    m_stderr.closeJobIOHandler();
                } catch (Exception e) {
                    s_logger.warn("Failed to get job error stream: " + m_nativeJobId, e);
                }
            }
        }
    }

    public void postStagingAndCleanup() throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        State state = this.getState();  // force state refresh
        if (this.isFinalState()) {
            // post-staging
            this.postStaging();
            // cleanup
            this.cleanUp();
        } else {
            throw new IncorrectStateException("Can not cleanup unfinished job: " + state, this);
        }
    }

    private void postStaging() throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (this.isFinalState()) {
            // post-staging
            try {
                m_stagingMgr.postStaging(this, m_nativeJobId);
            } catch (AuthenticationFailedException e) {
                throw new NoSuccessException(e);
            } catch (AuthorizationFailedException e) {
                throw new NoSuccessException(e);
            } catch (BadParameterException e) {
                throw new NoSuccessException(e);
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    private void cleanUp() throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        // cleanup staged files
        Directory dir = null;

        // remove staging files
        try {
            dir = m_stagingMgr.cleanup(this, m_nativeJobId);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (NotImplementedException e){
        	// In case of read-only data adaptor for example.
        	// continue to be able to clean job
        	s_logger.info("Could not clean staged files:" + e.getMessage());
        }
        try {
            // adaptor's cleanup
            if (m_controlAdaptor instanceof CleanableJobAdaptor) {
                try {
                    JobInfoAdaptor jia = getJobInfoAdaptor();
                    try {
                        setStaticValue(m_attributes.m_Created, jia.getCreated(m_nativeJobId));
                    } catch (Exception e) {
                        s_logger.warn(e.getMessage());
                    }
                    try {
                        setStaticValue(m_attributes.m_Started, jia.getStarted(m_nativeJobId));
                    } catch (Exception e) {
                        s_logger.warn(e.getMessage());
                    }
                    try {
                        setStaticValue(m_attributes.m_Finished, jia.getFinished(m_nativeJobId));
                    } catch (Exception e) {
                        s_logger.warn(e.getMessage());
                    }
                    try {
                        setStaticValue(m_attributes.m_ExitCode, jia.getExitCode(m_nativeJobId));
                    } catch (Exception e) {
                        s_logger.warn(e.getMessage());
                    }
                    try {
                        setStaticValues(m_attributes.m_ExecutionHosts, jia.getExecutionHosts(m_nativeJobId));
                    } catch (Exception e) {
                        s_logger.warn(e.getMessage());
                    }
                } catch (NotImplementedException nie) {
                    // Do not cache
                }

                ((CleanableJobAdaptor) m_controlAdaptor).clean(m_nativeJobId);
            }

            // remove staging directory
            if (dir != null) {
                try {
                    dir.remove();
                } catch (AuthenticationFailedException e) {
                    throw new NoSuccessException(e);
                } catch (AuthorizationFailedException e) {
                    throw new NoSuccessException(e);
                } catch (BadParameterException e) {
                    throw new NoSuccessException(e);
                }
            }

        } finally {
            if (dir != null) {
                dir.close();
            }
        }
        // throw NotImplementedException if adaptor not instance of CleanableJobAdaptor
        if (!(m_controlAdaptor instanceof CleanableJobAdaptor)) {
            throw new NotImplementedException("Cleanup is not supported by this adaptor: " + m_controlAdaptor.getClass().getName());
        }
    }

    ////////////////////////////////////// implementation of Job //////////////////////////////////////
    public JobDescription getJobDescriptionSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_jobDescription;
    }

    public OutputStream getStdinSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        m_monitorService.checkState();
        if (this.isInteractive() || m_stagingMgr instanceof StreamingManagerThroughSandbox) {
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
        if (this.isInteractive() || m_stagingMgr instanceof StreamingManagerThroughSandbox) {
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
        if (this.isInteractive() || m_stagingMgr instanceof StreamingManagerThroughSandbox) {
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
            if (!((HoldableJobAdaptor) m_controlAdaptor).hold(m_nativeJobId)) {
                if (!((SuspendableJobAdaptor) m_controlAdaptor).suspend(m_nativeJobId)) {
                    if (getJobState().equals(State.NEW) || getJobState().equals(State.RUNNING)) {
                        throw new NoSuccessException("Failed to hold/suspend job, the plugin returned False: " + m_nativeJobId);
                    } else {
                        throw new IncorrectStateException("Failed to hold/suspend job because it is neither queued nor active: " + m_nativeJobId);
                    }
                }
            }
        } else if (m_controlAdaptor instanceof HoldableJobAdaptor) {
            if (!((HoldableJobAdaptor) m_controlAdaptor).hold(m_nativeJobId)) {
                if (!getJobState().equals(State.NEW)) {
                    throw new IncorrectStateException("Failed to hold job because it is not queued: " + m_nativeJobId);
                } else {
                    throw new NoSuccessException("Failed to hold job; the plugin returned False");
                }
            }
        } else if (m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (!((SuspendableJobAdaptor) m_controlAdaptor).suspend(m_nativeJobId)) {
                if (!getJobState().equals(State.RUNNING)) {
                    throw new IncorrectStateException("Failed to suspend job because if is not active: " + m_nativeJobId);
                } else {
                    throw new NoSuccessException("Failed to suspend job; the plugin returned False");
                }
            }
        } else {
            throw new NotImplementedException("Suspend is not supported by this adaptor: " + m_controlAdaptor.getClass().getName());
        }
    }

    public void resumeSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not resume job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof HoldableJobAdaptor && m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (!((HoldableJobAdaptor) m_controlAdaptor).release(m_nativeJobId)) {
                if (!((SuspendableJobAdaptor) m_controlAdaptor).resume(m_nativeJobId)) {
                    if (!getState().equals(State.SUSPENDED)) {
                        throw new IncorrectStateException("Failed to release/resume job because it is neither held nor suspended: " + m_nativeJobId);
                    } else {
                        throw new NoSuccessException("Failed to release/resume job; the plugin returned False");
                    }
                }
            }
        } else if (m_controlAdaptor instanceof HoldableJobAdaptor) {
            if (!((HoldableJobAdaptor) m_controlAdaptor).release(m_nativeJobId)) {
                if (!getState().equals(State.SUSPENDED)) {
                    throw new IncorrectStateException("Failed to release job because it is not held: " + m_nativeJobId);
                } else {
                    throw new NoSuccessException("Failed to release job; the plugin returned False");
                }
            }
        } else if (m_controlAdaptor instanceof SuspendableJobAdaptor) {
            if (!((SuspendableJobAdaptor) m_controlAdaptor).resume(m_nativeJobId)) {
                if (!getState().equals(State.SUSPENDED)) {
                    throw new IncorrectStateException("Failed to resume job because if is not suspended: " + m_nativeJobId);
                } else {
                    throw new NoSuccessException("Failed to resume job; the plugin returned False");
                }
            }
        } else {
            throw new NotImplementedException("Resume is not supported by this adaptor: " + m_controlAdaptor.getClass().getName());
        }
    }

    public void checkpointSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_monitorService.checkState();
        if (m_nativeJobId == null) {
            throw new IncorrectStateException("Can not checkpoint job in 'New' state", this);
        }
        if (m_controlAdaptor instanceof CheckpointableJobAdaptor) {
            if (!((CheckpointableJobAdaptor) m_controlAdaptor).checkpoint(m_nativeJobId)) {
                throw new NoSuccessException("Failed to checkpoint job: " + m_nativeJobId);
            }
        } else {
            throw new NotImplementedException("Checkpoint is not supported by this adaptor: " + m_controlAdaptor.getClass().getName());
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
            if (!((SignalableJobAdaptor) m_controlAdaptor).signal(m_nativeJobId, signum)) {
                throw new NoSuccessException("Failed to signal job: " + m_nativeJobId);
            }
        } else {
            throw new NotImplementedException("Signal is not supported by this adaptor: " + m_controlAdaptor.getClass().getName());
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
        if (m_currentModelState == null || !m_currentModelState.equals(stateDetail)) {
            m_currentModelState = stateDetail;
            m_monitorService.getStateLogger().debug("State changed to " + stateDetail + " for job " + m_attributes.m_JobId.getObject());
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
        if (!this.isFinalState()) {
            // update cause
            if (cause != null) {
                super.setException(cause);
            }
            // update metrics
            m_metrics.m_State.setValue(state);
            m_metrics.m_StateDetail.setValue(stateDetail);
            m_metrics.m_StateDetail.setValue(MODEL + ":" + subState.toString());
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
            throw new NotImplementedException("Job attribute not supported by this adaptor: " + monitorAdaptor.getClass());
        }
    }

    StagingTransfer[] getOutputStagingTransfer() throws PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_stagingMgr instanceof DataStagingManagerThroughSandbox) {
            return ((DataStagingManagerThroughSandbox) m_stagingMgr).getOutputStagingTransfer(m_nativeJobId);
        }
        return null;
    }

    ////////////////////////////////////// private methods //////////////////////////////////////
    private boolean isFinalState() {
        // do not use getState_fromCache() because it may lead to infinite recursion
        State state = m_metrics.m_State.getValue();
        if (state == null) {
            state = State.RUNNING;
        }

        // if state is terminal
        switch (state) {
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

    private void setStaticValue(ScalarAttributeImpl<Date> attr, final Date value) {
        attr = _addAttribute(new ScalarAttributeImpl<Date>(attr.getKey(), null, attr.getMode(), attr.getType(), new Date()) {

            public String getValue() {
                return value.toString();
            }
        });
    }

    private void setStaticValue(ScalarAttributeImpl<Integer> attr, final Integer value) {
        attr = _addAttribute(new ScalarAttributeImpl<Integer>(attr.getKey(), null, attr.getMode(), attr.getType(), null) {

            public String getValue() {
                return value.toString();
            }
        });
    }

    private void setStaticValues(VectorAttributeImpl<String> attr, final String[] values) {
        attr = _addVectorAttribute(new VectorAttributeImpl<String>(attr.getKey(), null, attr.getMode(), attr.getType(), null) {

            public String[] getValues() {
                return values;
            }
        });
    }
}
