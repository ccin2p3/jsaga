package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.impl.monitoring.*;
import fr.in2p3.jsaga.impl.task.AbstractTaskImplWithAsyncAttributes;
import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.*;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractCopyFromTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 févr. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractCopyFromTask<T extends SagaObject,E> extends AbstractTaskImplWithAsyncAttributes<T,E,T> implements Task<T,E>, AsyncAttributes<T> {
    // internal
    private URL m_source;
    private int m_flags;
    // metrics
    private long m_totalWrittenBytes;
    private MetricImpl<Long> m_metric_Progress;

    /** constructor */
    public AbstractCopyFromTask(TaskMode mode, Session session, URL source, int flags) throws NotImplementedException {
        super(session, true);
        // internal
        m_source = source;
        m_flags = flags;
        // metrics
        m_totalWrittenBytes = 0L;
        m_metric_Progress = new MetricFactoryImpl<Long>(this).createAndRegister(
                FileCopyTask.FILE_COPY_PROGRESS,
                "this metric gives the state of ongoing file transfer as number of bytes transfered.",
                MetricMode.ReadOnly,
                "bytes",
                MetricType.Int,
                0L);
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

    void increment(long writtenBytes) {
        m_totalWrittenBytes += writtenBytes;
        m_metric_Progress.setValue(m_totalWrittenBytes);
    }

    public abstract void doCopyFrom(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException;

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    private Thread m_thread;
    public void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_thread = new Thread(new Runnable(){
            public void run() {
                AbstractCopyFromTask.super.setState(State.RUNNING);
                try {
                    AbstractCopyFromTask.this.doCopyFrom(m_source, m_flags);
                    AbstractCopyFromTask.super.setState(State.DONE);
                } catch (Exception e) {
                    AbstractCopyFromTask.super.setException(new NoSuccessException(e));
                    AbstractCopyFromTask.super.setState(State.FAILED);
                }
            }
        });
        m_thread.start();
    }

    protected void doCancel() {
        try {
            if (m_thread != null) {
                m_thread.interrupt();
            }
            this.setState(State.CANCELED);
        } catch(SecurityException e) {
            // do nothing (failed to cancel task)
        }
    }

    protected State queryState() {
        return null;    // FileCopyFromTask does not support queryState
    }

    public boolean startListening() {
        return true;    // FileCopyFromTask is always listening anyway...
    }

    public void stopListening() {
        // do nothing
    }
}
