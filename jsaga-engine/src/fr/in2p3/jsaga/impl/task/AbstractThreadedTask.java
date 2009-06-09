package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.error.*;
import org.ogf.saga.task.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractThreadedTask
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractThreadedTask<T,E> extends AbstractTaskImpl<T,E> implements Task<T,E> {
    private Thread m_thread;

    /** constructor */
    public AbstractThreadedTask(TaskMode mode) throws NotImplementedException {
        super(null, null, true);

        // set thread
        m_thread = new Thread(new Runnable(){
            public void run() {
                AbstractThreadedTask.super.setState(State.RUNNING);
                try {
                    E result = AbstractThreadedTask.this.invoke();
                    AbstractThreadedTask.super.setResult(result);
                    AbstractThreadedTask.super.setState(State.DONE);
                } catch (Exception e) {
                    AbstractThreadedTask.super.setException(new NoSuccessException(e));
                    AbstractThreadedTask.super.setState(State.FAILED);
                }
            }
        });
        try {
            switch(mode) {
                case TASK:
                    break;
                case ASYNC:
                    this.run();
                    break;
                case SYNC:
                    this.run();
                    this.waitFor();
                    break;
                default:
                    throw new NotImplementedException("INTERNAL ERROR: unexpected exception");
            }
        } catch (NotImplementedException e) {
            throw e;
        } catch (SagaException e) {
            throw new NotImplementedException(e);
        }
    }

    public abstract E invoke() throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException,
            TimeoutException, NoSuccessException;

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
