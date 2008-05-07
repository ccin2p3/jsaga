package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.monitoring.*;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Callback;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskStateMetricImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   5 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskStateMetricImpl<E> extends MetricImpl<E> {
    private AbstractTaskImpl m_task;
    private boolean m_isListening;

    /** constructor */
    public TaskStateMetricImpl(AbstractTaskImpl task, String name, String desc, MetricMode mode, String unit, MetricType type, E initialValue) {
        super(task, name, desc, mode, unit, type, initialValue);
        m_task = task;
        m_isListening = false;
    }

    /** clone */
    public TaskStateMetricImpl<E> clone() throws CloneNotSupportedException {
        // clone attributes
        TaskStateMetricImpl<E> clone = (TaskStateMetricImpl<E>) super.clone();
        clone.m_task = m_task;
        clone.m_isListening = m_isListening;
        return clone;
    }

    public synchronized int addCallback(Callback cb) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        int nbBefore = super.getNumberOfCallbacks();
        int cookie = super.addCallback(cb);
        int nbAfter = super.getNumberOfCallbacks();
        if (nbBefore==0 && nbAfter==1) {
            m_isListening = m_task.startListening();
        }
        return cookie;
    }

    public synchronized void removeCallback(int cookie) throws NotImplemented, BadParameter, AuthenticationFailed, AuthorizationFailed,PermissionDenied, Timeout, NoSuccess {
        int nbBefore = super.getNumberOfCallbacks();
        super.removeCallback(cookie);
        int nbAfter = super.getNumberOfCallbacks();
        if (nbBefore==1 && nbAfter==0) {
            m_task.stopListening();
            m_isListening = false;
        }
    }

    public boolean isListening() {
        return m_isListening;
    }
}
