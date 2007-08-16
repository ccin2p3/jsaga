package fr.in2p3.jsaga.engine.task;

import fr.in2p3.jsaga.engine.base.AbstractMonitorableImpl;
import org.ogf.saga.SagaBase;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskImpl extends AbstractMonitorableImpl implements Task {
    private TaskMode m_mode;
    private State m_state;
    private org.ogf.saga.error.Exception m_exception;

    /** constructor */
    public TaskImpl(Session session, TaskMode mode) {
        super(session);
        m_mode = mode;
        switch(m_mode.getValue()) {
            case TaskMode.ASYNC_TYPE:
            case TaskMode.SYNC_TYPE:
                m_state = State.RUNNING;
                break;
            case TaskMode.TASK_TYPE:
                m_state = State.NEW;
                break;
        }
        m_exception = null;
    }

    /** constructor for deepCopy */
    protected TaskImpl(TaskImpl source) {
        super(source);
        m_mode = source.m_mode;
        m_state = source.m_state;
        m_exception = source.m_exception;
    }
    public SagaBase deepCopy() {
        return new TaskImpl(this);
    }

    public void run() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        switch(m_mode.getValue()) {
            case TaskMode.ASYNC_TYPE:
            case TaskMode.SYNC_TYPE:
                // ignore
                break;
            case TaskMode.TASK_TYPE:
                switch(m_state.getValue()) {
                    case State.NEW_TYPE:
                        //todo: Implement method run()
                        break;
                    default:
                        throw new IncorrectState("Can not run task in state: "+m_state.getValue(), this);
                }
                break;
        }
    }

    public boolean doWait(float timeout) throws NotImplemented, IncorrectState, NoSuccess {
        try {
            long endTime = (timeout>=0.0f ? System.currentTimeMillis()+(long)timeout : -1);
            while(!isFinished() && (endTime>0 && System.currentTimeMillis()<endTime)) {
                Thread.currentThread().sleep(100);
            }
        } catch (InterruptedException e) {
        }
        return isFinished();
    }

    public void cancel(float timeout) throws NotImplemented, IncorrectState, NoSuccess {
        switch(m_state.getValue()) {
            case State.NEW_TYPE:
                throw new IncorrectState("Can not cancel task in state: NEW", this);
            case State.DONE_TYPE:
            case State.CANCELED_TYPE:
            case State.FAILED_TYPE:
                // do nothing
                break;
            case State.RUNNING_TYPE:
                //todo: Implement method cancel()
                m_state = State.CANCELED;
                break;
        }
    }

    public State getState() throws NotImplemented, Timeout, NoSuccess {
        return m_state;
    }

    public void rethrow() throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, ReadOnly, Timeout, NoSuccess {
        switch(m_state.getValue()) {
            case State.FAILED_TYPE:
                if (m_exception != null) {
                    throw new NoSuccess(m_exception);
                } else {
                    throw new NoSuccess("task failed with unknown reason", this);
                }
            case State.CANCELED_TYPE:
                throw new NoSuccess("task canceled", this);
        }
    }

    private boolean isFinished() {
        switch(m_state.getValue()) {
            case State.DONE_TYPE:
            case State.CANCELED_TYPE:
            case State.FAILED_TYPE:
                return true;
            default:
                return false;
        }
    }
}
