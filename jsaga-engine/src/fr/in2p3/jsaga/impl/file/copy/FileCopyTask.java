package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.monitoring.*;
import fr.in2p3.jsaga.impl.task.AbstractTaskImplWithAsyncAttributes;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileCopyTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileCopyTask<T,E> extends AbstractTaskImplWithAsyncAttributes<T,E,T> implements Task<T,E>, AsyncAttributes<T> {
    public static final String FILE_COPY_PROGRESS = "file.copy.progress";
    // internal
    private FileImpl m_sourceFile;
    private URL m_target;
    private int m_flags;
    // metrics
    private long m_totalWrittenBytes;
    private MetricImpl<Long> m_metric_Progress;

    /** constructor */
    public FileCopyTask(Session session, FileImpl sourceFile, URL target, int flags) throws NotImplementedException {
        super(session, true);
        // internal
        m_sourceFile = sourceFile;
        m_target = target;
        m_flags = flags;
        // metrics
        m_totalWrittenBytes = 0L;
        m_metric_Progress = new MetricFactoryImpl<Long>(this).createAndRegister(
                FILE_COPY_PROGRESS,
                "this metric gives the state of ongoing file transfer as number of bytes transfered.",
                MetricMode.ReadOnly,
                "bytes",
                MetricType.Int,
                0L);
    }

    void increment(long writtenBytes) {
        m_totalWrittenBytes += writtenBytes;
        m_metric_Progress.setValue(m_totalWrittenBytes);
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    private Thread m_thread;
    public void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_thread = new Thread(new Runnable(){
            public void run() {
                FileCopyTask.super.setState(State.RUNNING);
                try {
                    m_sourceFile._copyAndMonitor(m_target, m_flags, FileCopyTask.this);
                    FileCopyTask.super.setState(State.DONE);
                } catch (Exception e) {
                    FileCopyTask.super.setException(new NoSuccessException(e));
                    FileCopyTask.super.setState(State.FAILED);
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
        return null;    // FileCopyTask does not support queryState
    }

    public boolean startListening() {
        return true;    // FileCopyTask is always listening anyway...
    }

    public void stopListening() {
        // do nothing
    }
}
