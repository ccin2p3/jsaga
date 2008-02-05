package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

import java.lang.Exception;
import java.lang.reflect.Method;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GenericThreadedTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GenericThreadedTask extends AbstractTaskImpl implements Task {
    private Method m_method;
    private Object[] m_arguments;
    private Thread m_thread;

    /** constructor */
    public GenericThreadedTask(Session session, Object object, Method method, Object[] arguments) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, object, true);
        this.init(method, arguments);
    }

    private void init(Method method, Object[] arguments) {
        m_method = method;
        m_arguments = arguments;
        m_thread = new Thread(new Runnable(){
            public void run() {
                GenericThreadedTask.super.setState(State.RUNNING);
                try {
                    Object object = GenericThreadedTask.super.getObject();
                    Object result = m_method.invoke(object, m_arguments);
                    GenericThreadedTask.super.setResult(result);
                    GenericThreadedTask.super.setState(State.DONE);
                } catch (Exception e) {
                    GenericThreadedTask.super.setException(new NoSuccess(e));
                    GenericThreadedTask.super.setState(State.FAILED);
                }
            }
        });
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    public void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        m_thread.start();
    }

    protected boolean doCancel() {
        try {
            Thread.currentThread().interrupt();
            return true;
        } catch(SecurityException e) {
            return false;
        }
    }

    protected void refreshState() {
        // do nothing
    }

    public void startListening(Metric metric) {
        // do nothing
    }

    public void stopListening(Metric metric) {
        // do nothing
    }
}
