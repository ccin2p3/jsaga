package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricImpl;
import fr.in2p3.jsaga.impl.permissions.AbstractJobPermissionsImpl;
import fr.in2p3.jsaga.jobcollection.LateBindedJob;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.*;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LateBindedJobImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LateBindedJobImpl extends AbstractJobPermissionsImpl implements LateBindedJob {
    protected JobHandle m_jobHandle;
    private XJSDLJobDescriptionImpl m_jobDesc;
    private URL m_resourceManager;

    /** constructor for JobWithStagingImpl */
    protected LateBindedJobImpl(Session session, XJSDLJobDescriptionImpl jobDesc, JobHandle jobHandle) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, true);
        m_jobHandle = jobHandle;
        m_jobDesc = jobDesc;
        m_resourceManager = null;
    }

    /** constructor for submission */
    public LateBindedJobImpl(Session session, XJSDLJobDescriptionImpl jobDesc) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, true);
        m_jobHandle = new JobHandle(session);
        m_jobDesc = jobDesc;
        m_resourceManager = null;
    }

    /** constructor for control and monitoring only */
    public LateBindedJobImpl(Session session, URL rm, String nativeJobId) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, false);
        m_jobHandle = new JobHandle(session, rm, nativeJobId);
        m_jobDesc = (XJSDLJobDescriptionImpl) m_jobHandle.getJobDescription();
        m_resourceManager = rm;
    }

    /////////////////////////////////////// implementation of LateBindedJob ////////////////////////////////////////

    public void allocate(Resource rm) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        // find grid name and allocate resource
        m_resourceManager = URLFactory.createURL(rm.getId());
        if (rm.getGrid() == null) {
            rm.setGrid(Configuration.getInstance().getConfigurations().getJobserviceCfg().findJobService(m_resourceManager).getName());
        }
        m_resourceManager.setFragment(rm.getGrid());

        // transform job description
        m_jobDesc = this.transformJobDescription(m_jobHandle, m_jobDesc, rm);

        // create job
        JobService jobService = JobFactory.createJobService(m_session, m_resourceManager);
        AbstractSyncJobImpl job = (AbstractSyncJobImpl) jobService.createJob(m_jobDesc);
        m_jobHandle.setJob(job);
    }
    protected boolean isAllocated() {
        return (m_resourceManager != null);
    }
    protected XJSDLJobDescriptionImpl transformJobDescription(JobHandle jobHandle, XJSDLJobDescriptionImpl jobDesc, Resource rm) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        return jobDesc;     // default implementation does nothing
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    protected void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_jobHandle.doSubmit();
    }
    protected void doCancel() {
        m_jobHandle.doCancel();
        this.setState(State.CANCELED);
    }
    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        return m_jobHandle.queryState();
    }

    private int m_cookie;    
    public boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            m_cookie = m_jobHandle.addCallback(Task.TASK_STATE, new Callback(){
                public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                    State state = ((MetricImpl<State>) metric).getValue();
                    LateBindedJobImpl.this.setState(state);
                    switch(state) {
                        case DONE:
                        case CANCELED:
                        case FAILED:
                            return false;   // unregister
                        default:
                            return true;    // stay registered
                    }
                }
            });
        }
        catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
        catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
        catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
        catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        return true;
    }

    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        try {
            m_jobHandle.removeCallback(Task.TASK_STATE, m_cookie);
        }
        catch (DoesNotExistException e) {throw new NoSuccessException(e);}
        catch (BadParameterException e) {throw new NoSuccessException(e);}
        catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
        catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
        catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
    }

    ////////////////////////////////////// implementation of Job //////////////////////////////////////

    public JobDescription getJobDescription() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_jobDesc;
    }
    public OutputStream getStdin() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        return m_jobHandle.getStdin();
    }
    public InputStream getStdout() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        return m_jobHandle.getStdout();
    }
    public InputStream getStderr() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        return m_jobHandle.getStderr();
    }
    public void suspend() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_jobHandle.suspend();
    }
    public void resume() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_jobHandle.resume();
    }
    public void checkpoint() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_jobHandle.checkpoint();
    }
    public void migrate(JobDescription jd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_jobHandle.migrate(jd);
    }
    public void signal(int signum) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_jobHandle.signal(signum);
    }

    public Task<Job, JobDescription> getJobDescription(TaskMode mode) throws NotImplementedException {
        return m_jobHandle.getJobDescription(mode);
    }
    public Task<Job, OutputStream> getStdin(TaskMode mode) throws NotImplementedException {
        return m_jobHandle.getStdin(mode);
    }
    public Task<Job, InputStream> getStdout(TaskMode mode) throws NotImplementedException {
        return m_jobHandle.getStdout(mode);
    }
    public Task<Job, InputStream> getStderr(TaskMode mode) throws NotImplementedException {
        return m_jobHandle.getStderr(mode);
    }
    public Task<Job, Void> suspend(TaskMode mode) throws NotImplementedException {
        return m_jobHandle.suspend(mode);
    }
    public Task<Job, Void> resume(TaskMode mode) throws NotImplementedException {
        return m_jobHandle.resume(mode);
    }
    public Task<Job, Void> checkpoint(TaskMode mode) throws NotImplementedException {
        return m_jobHandle.checkpoint(mode);
    }
    public Task<Job, Void> migrate(TaskMode mode, JobDescription jd) throws NotImplementedException {
        return m_jobHandle.migrate(mode, jd);
    }
    public Task<Job, Void> signal(TaskMode mode, int signum) throws NotImplementedException {
        return m_jobHandle.signal(mode, signum);
    }
}
