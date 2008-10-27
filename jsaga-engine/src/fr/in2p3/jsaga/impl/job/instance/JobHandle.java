package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.impl.monitoring.MetricImpl;
import fr.in2p3.jsaga.impl.task.TaskCallback;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobHandle
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobHandle extends AbstractAsyncJobImpl implements Job {
    private JobImpl m_job;
    private File m_inputFile;
    private TaskCallback m_jobRunTask;

    /** constructor for submission */
    public JobHandle(Session session) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, true);
        m_job = null;
        m_inputFile = null;
        m_jobRunTask = null;
    }

    /** constructor for control and monitoring only */
    public JobHandle(Session session, URL rm, String nativeJobId) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, false);
        m_job = (JobImpl) JobFactory.createJobService(m_session, rm).getJob(nativeJobId);
        m_inputFile = null;
        m_jobRunTask = null;
    }

    ////////////////////////////////////////// JobHandler specific method //////////////////////////////////////////

    public void setJob(JobImpl job) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_job = job;
        if (m_isRunning) {
            this.doSubmit();
        }
    }

    public void setInputFile(File inputFile) throws NoSuccessException {
        if (m_job==null || !m_isRunning) {
            m_inputFile = inputFile;
        } else {
            throw new NoSuccessException("Can not set stdin on a running job");
        }
    }

    public void setJobRunTask(TaskCallback jobRunTask) {
        m_jobRunTask = jobRunTask;
    }

    //////////////////////////////////////////// implementation of Task ////////////////////////////////////////////

    /** override super.rethrow() */
    public void rethrow() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            m_job.rethrow();
        }
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    private boolean m_isRunning = false;
    protected void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            // send input stream to job
            if (m_inputFile != null) {
                try {
                    m_job.getJobDescription().setAttribute(JobDescription.INTERACTIVE, "true");
                    FileInputStream in = new FileInputStream(m_inputFile);
                    OutputStream out = m_job.getStdin();
                    int len;
                    byte[] buffer = new byte[1024];
                    while ( (len=in.read(buffer)) > -1 ) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    in.close();
                } catch (Exception e) {
                    throw new NoSuccessException(e);
                }
            }

            // submit
            m_job.doSubmit();

            // start listening
            m_job.startListening();
        } else {
            // set as running
            m_isRunning = true;
        }
    }

    protected void doCancel() {
        if (m_job != null) {
            m_job.doCancel();
        }
        super.setState(State.CANCELED);
    }

    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            return m_job.queryState();
        } else {
            return State.NEW;
        }
    }

    private int m_cookie;
    public boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            try {
                m_cookie = m_job.addCallback(Task.TASK_STATE, new Callback(){
                    public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                        State state = ((MetricImpl<State>) metric).getValue();
                        m_jobRunTask.setState(state);
                        JobHandle.this.setState(state);
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
            return m_job.startListening();
        } else {
            return true;    // a job task is always listening (either with notification, or with polling)
        }
    }

    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            try {
                m_job.removeCallback(Task.TASK_STATE, m_cookie);
            }
            catch (DoesNotExistException e) {throw new NoSuccessException(e);}
            catch (BadParameterException e) {throw new NoSuccessException(e);}
            catch (AuthenticationFailedException e) {throw new NoSuccessException(e);}
            catch (AuthorizationFailedException e) {throw new NoSuccessException(e);}
            catch (PermissionDeniedException e) {throw new NoSuccessException(e);}
            m_job.stopListening();
        }
    }

    ////////////////////////////////////// implementation of Job //////////////////////////////////////

    public JobDescription getJobDescription() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            return m_job.getJobDescription();
        } else {
            throw new NoSuccessException("No resource has been allocated yet", this);
        }
    }

    public OutputStream getStdin() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        if (m_job != null) {
            return m_job.getStdin();
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }

    public InputStream getStdout() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        if (m_job != null) {
            return m_job.getStdout();
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }

    public InputStream getStderr() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        if (m_job != null) {
            return m_job.getStderr();
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }

    public void suspend() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            m_job.suspend();
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }

    public void resume() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            m_job.resume();
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }

    public void checkpoint() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            m_job.checkpoint();
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }

    public void migrate(JobDescription jd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            m_job.migrate(jd);
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }

    public void signal(int signum) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_job != null) {
            m_job.signal(signum);
        } else {
            throw new IncorrectStateException("No resource has been allocated yet", this);
        }
    }
}
