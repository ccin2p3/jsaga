package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.ObjectType;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.task.State;

import java.util.Timer;
import java.util.TimerTask;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskForTesting
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskForTesting extends AbstractTaskImplWithAsyncAttributes<String> {
    private boolean m_notified;
    private long m_submitTime;
    private boolean m_isCancelled;
    private Timer m_timer;

    /** constructor */
    public TaskForTesting(boolean notified) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(null, null, true);
        m_notified = notified;
        m_submitTime = 0;
        m_isCancelled = false;
        m_timer = null;
    }

    public ObjectType getType() {
        return ObjectType.TASK;
    }

    protected void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        m_submitTime = System.currentTimeMillis();
    }

    protected void doCancel() {
        m_isCancelled = true;
        this.setState(State.CANCELED);
    }

    protected State queryState() throws NotImplemented, Timeout, NoSuccess {
        if (!m_notified) {
            if (m_isCancelled) {
                return State.CANCELED;
            } else if (System.currentTimeMillis() - m_submitTime > 100) {
                super.setResult("result");
                return State.DONE;
            } else {
                return State.NEW;
            }
        } else {
            return null;    // null <=> don't know
        }
    }

    public boolean startListening(Metric metric) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        if (m_notified) {
            final TaskForTesting current = this;
            m_timer = new Timer();
            m_timer.schedule(new TimerTask(){
                public void run() {
                    current.setResult("result");
                    current.setState(State.DONE);
                }
            }, 100);
        }
        return m_notified;
    }

    public void stopListening(Metric metric) throws NotImplemented, Timeout, NoSuccess {
        if (m_notified) {
            m_timer.cancel();
        }
    }
}
