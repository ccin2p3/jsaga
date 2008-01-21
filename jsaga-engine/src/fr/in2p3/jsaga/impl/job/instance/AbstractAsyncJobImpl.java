package fr.in2p3.jsaga.impl.job.instance;

import fr.in2p3.jsaga.impl.permissions.AbstractJobPermissionsImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncJobImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncJobImpl extends AbstractJobPermissionsImpl implements Job {
    /** constructor */
    public AbstractAsyncJobImpl(Session session, SagaObject object) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, object);
    }

    public Task<JobDescription> getJobDescription(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("getJobDescription", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<OutputStream> getStdin(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("getStdin", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<InputStream> getStdout(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("getStdout", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<InputStream> getStderr(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("getStderr", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task suspend(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("suspend", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task resume(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("resume", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task checkpoint(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("checkpoint", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task migrate(TaskMode mode, JobDescription jd) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("migrate", new Class[]{JobDescription.class}),
                    new Object[]{jd}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task signal(TaskMode mode, int signum) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    JobImpl.class.getMethod("signal", new Class[]{int.class}),
                    new Object[]{signum}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
