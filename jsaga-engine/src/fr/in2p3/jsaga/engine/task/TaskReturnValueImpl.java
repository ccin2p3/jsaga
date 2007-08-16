package fr.in2p3.jsaga.engine.task;

import org.ogf.saga.SagaBase;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.task.TaskReturnValue;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskReturnValueImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskReturnValueImpl extends TaskImpl implements TaskReturnValue {
    private Object m_result;

    /** constructor */
    public TaskReturnValueImpl(Session session, TaskMode mode) {
        super(session, mode);
        m_result = null;
    }

    /** constructor for deepCopy */
    protected TaskReturnValueImpl(TaskReturnValueImpl source) {
        super(source);
        m_result = source.m_result;
    }
    public SagaBase deepCopy() {
        return new TaskReturnValueImpl(this);
    }

    public Object getResult() throws NotImplemented, IncorrectState, NoSuccess {
        return m_result;
    }
}
