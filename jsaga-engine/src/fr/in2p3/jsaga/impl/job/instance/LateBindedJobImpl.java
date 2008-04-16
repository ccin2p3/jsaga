package fr.in2p3.jsaga.impl.job.instance;

import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;

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
public class LateBindedJobImpl extends AbstractAsyncJobImpl implements Job {
    private JobDescription m_jobDesc;
    private JobImpl m_job;

    /** constructor for submission */
    public LateBindedJobImpl(Session session, JobDescription jobDesc) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, true);
        m_jobDesc = jobDesc;
        m_job = null;
    }

    /** constructor for control and monitoring only */
    public LateBindedJobImpl(Session session, URL rm, String nativeJobId) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, false);
        m_jobDesc = null;
        m_job = (JobImpl) JobFactory.createJobService(m_session, rm).getJob(nativeJobId);
    }

    public void allocate(URL rm) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_job = (JobImpl) JobFactory.createJobService(m_session, rm).createJob(m_jobDesc);
        if (m_isSubmitted && !m_isCancelled) {
            m_job.run();
        }
        if (m_listenedMetric != null) {
            m_job.startListening(m_listenedMetric);
        }
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////    

    private boolean m_isSubmitted = false;
    protected void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        m_isSubmitted = true;
    }

    private boolean m_isCancelled = false;
    protected void doCancel() {
        if (m_job != null) {
            m_job.doCancel();
        } else {
            m_isCancelled = true;
            this.setState(State.CANCELED);
        }
    }

    protected State queryState() throws NotImplemented, Timeout, NoSuccess {
        if (m_job != null) {
            return m_job.queryState();
        } else {
            return State.NEW;
        }
    }

    private Metric m_listenedMetric;
    public boolean startListening(Metric metric) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        if (m_job != null) {
            return m_job.startListening(metric);
        } else {
            m_listenedMetric = metric;
            return true;    // a job task is always listening (either with notification, or with polling)
        }
    }

    public void stopListening(Metric metric) throws NotImplemented, Timeout, NoSuccess {
        if (m_job != null) {
            m_job.stopListening(metric);
        } else {
            m_listenedMetric = null;
        }
    }

    ////////////////////////////////////// implementation of Job //////////////////////////////////////

    public JobDescription getJobDescription() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        if (m_job != null) {
            return m_job.getJobDescription();
        } else {
            return m_jobDesc;
        }
    }

    public OutputStream getStdin() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        if (m_job != null) {
            return m_job.getStdin();
        } else {
            throw new NotImplemented("Interactive jobs are currently not supported by JSAGA", this);
        }
    }

    public InputStream getStdout() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        if (m_job != null) {
            return m_job.getStdout();
        } else {
            throw new NotImplemented("Interactive jobs are currently not supported by JSAGA", this);
        }
    }

    public InputStream getStderr() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, IncorrectState, NoSuccess {
        if (m_job != null) {
            return m_job.getStderr();
        } else {
            throw new NotImplemented("Interactive jobs are currently not supported by JSAGA", this);
        }
    }

    public void suspend() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_job != null) {
            m_job.suspend();
        } else {
            throw new IncorrectState("Can not suspend job in 'New' state", this);            
        }
    }

    public void resume() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_job != null) {
            m_job.resume();
        } else {
            throw new IncorrectState("Can not resume job in 'New' state", this);            
        }
    }

    public void checkpoint() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_job != null) {
            m_job.checkpoint();
        } else {
            throw new IncorrectState("Can not checkpoint job in 'New' state", this);
        }
    }

    public void migrate(JobDescription jd) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_job != null) {
            m_job.migrate(jd);
        } else {
            throw new IncorrectState("Can not migrate job in 'New' state", this);
        }
    }

    public void signal(int signum) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_job != null) {
            m_job.signal(signum);
        } else {
            throw new IncorrectState("Can not send signal to job in 'New' state", this);
        }
    }
}
