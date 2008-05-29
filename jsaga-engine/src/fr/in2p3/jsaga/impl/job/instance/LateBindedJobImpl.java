package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricImpl;
import fr.in2p3.jsaga.jobcollection.LateBindedJob;
import org.ogf.saga.URL;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

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
public class LateBindedJobImpl extends AbstractAsyncJobImpl implements LateBindedJob {
    private JobHandle m_jobHandle;
    private XJSDLJobDescriptionImpl m_jobDesc;
    private URL m_resourceManager;

    /** constructor for JobWithStagingImpl */
    protected LateBindedJobImpl(Session session, XJSDLJobDescriptionImpl jobDesc, JobHandle jobHandle) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, true);
        m_jobHandle = jobHandle;
        m_jobDesc = jobDesc;
        m_resourceManager = null;
    }

    /** constructor for submission */
    public LateBindedJobImpl(Session session, XJSDLJobDescriptionImpl jobDesc) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, true);
        m_jobHandle = new JobHandle(session);
        m_jobDesc = jobDesc;
        m_resourceManager = null;
    }

    /** constructor for control and monitoring only */
    public LateBindedJobImpl(Session session, URL rm, String nativeJobId) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, false);
        m_jobHandle = new JobHandle(session, rm, nativeJobId);
        m_jobDesc = (XJSDLJobDescriptionImpl) m_jobHandle.getJobDescription();
        m_resourceManager = rm;
    }

    /////////////////////////////////////// implementation of LateBindedJob ////////////////////////////////////////

    public void allocate(Resource rm) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // find grid name and allocate resource
        m_resourceManager = new URL(rm.getId());
        if (rm.getGrid() == null) {
            rm.setGrid(Configuration.getInstance().getConfigurations().getJobserviceCfg().findJobService(m_resourceManager).getName());
        }
        m_resourceManager.setFragment(rm.getGrid());

        // transform job description
        m_jobDesc = this.transformJobDescription(m_jobHandle, m_jobDesc, rm);

        // create job
        JobService jobService = JobFactory.createJobService(m_session, m_resourceManager);
        JobImpl job = (JobImpl) jobService.createJob(m_jobDesc);
        m_jobHandle.setJob(job);
    }
    protected boolean isAllocated() {
        return (m_resourceManager != null);
    }
    protected XJSDLJobDescriptionImpl transformJobDescription(JobHandle jobHandle, XJSDLJobDescriptionImpl jobDesc, Resource rm) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        return jobDesc;     // default implementation does nothing
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    protected void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        m_jobHandle.doSubmit();
    }
    protected void doCancel() {
        m_jobHandle.doCancel();
        this.setState(State.CANCELED);
    }
    protected State queryState() throws NotImplemented, Timeout, NoSuccess {
        return m_jobHandle.queryState();
    }

    private int m_cookie;    
    public boolean startListening() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        try {
            m_cookie = m_jobHandle.addCallback(Task.TASK_STATE, new Callback(){
                public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplemented, AuthorizationFailed {
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
        catch (AuthenticationFailed e) {throw new NoSuccess(e);}
        catch (AuthorizationFailed e) {throw new NoSuccess(e);}
        catch (PermissionDenied e) {throw new NoSuccess(e);}
        catch (DoesNotExist e) {throw new NoSuccess(e);}
        return true;
    }

    public void stopListening() throws NotImplemented, Timeout, NoSuccess {
        try {
            m_jobHandle.removeCallback(Task.TASK_STATE, m_cookie);
        }
        catch (DoesNotExist e) {throw new NoSuccess(e);}
        catch (BadParameter e) {throw new NoSuccess(e);}
        catch (AuthenticationFailed e) {throw new NoSuccess(e);}
        catch (AuthorizationFailed e) {throw new NoSuccess(e);}
        catch (PermissionDenied e) {throw new NoSuccess(e);}
    }

    ////////////////////////////////////// implementation of Job //////////////////////////////////////

    public JobDescription getJobDescription() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return m_jobDesc;
    }
    public OutputStream getStdin() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        return m_jobHandle.getStdin();
    }
    public InputStream getStdout() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        return m_jobHandle.getStdout();
    }
    public InputStream getStderr() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        return m_jobHandle.getStderr();
    }
    public void suspend() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        m_jobHandle.suspend();
    }
    public void resume() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        m_jobHandle.resume();
    }
    public void checkpoint() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        m_jobHandle.checkpoint();
    }
    public void migrate(JobDescription jd) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_jobHandle.migrate(jd);
    }
    public void signal(int signum) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_jobHandle.signal(signum);
    }
}
