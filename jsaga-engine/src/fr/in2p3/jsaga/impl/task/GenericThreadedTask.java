package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

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
public class GenericThreadedTask<T,E> extends AbstractTaskImpl<T,E> implements Task<T,E> {
    private Method m_method;
    private Object[] m_arguments;
    private Thread m_thread;

    /** constructor */
    public GenericThreadedTask(Session session, T object, Method method, Object[] arguments) throws NotImplementedException {
        super(session, object, true);

        // set thread
        m_method = method;
        m_arguments = arguments;
        m_thread = new Thread(new Runnable(){
            public void run() {
                GenericThreadedTask.super.setState(State.RUNNING);
                try {
                    T object = GenericThreadedTask.super.getObject();
                    E result = (E) m_method.invoke(object, m_arguments);
                    GenericThreadedTask.super.setResult(result);
                    GenericThreadedTask.super.setState(State.DONE);
                } catch (Exception e) {
                    GenericThreadedTask.super.setException(new NoSuccessException(e));
                    GenericThreadedTask.super.setState(State.FAILED);
                }
            }
        });
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    public void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        // start thread
        m_thread.start();
    }

    protected void doCancel() {
        // cancel thread
        try {
            m_thread.interrupt();
            this.setState(State.CANCELED);
        } catch(SecurityException e) {
            // do nothing (failed to cancel task)
        }
    }

    protected State queryState() {
        return null;    // GenericThreadedTask does not support queryState
    }

    public boolean startListening() {
        return true;    // GenericThreadedTask is always listening anyway...
    }

    public void stopListening() {
        // do nothing
    }
}
