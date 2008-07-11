package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.monitoring.*;
import fr.in2p3.jsaga.impl.task.AbstractTaskImplWithAsyncAttributes;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileCopyFromTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileCopyFromTask extends AbstractTaskImplWithAsyncAttributes implements Task {
    // internal
    private FileCopyFrom m_copyFrom;
    private URL m_effectiveSource;
    private FlagsBytes m_effectiveFlags;
    // metrics
    private long m_totalWrittenBytes;
    private MetricImpl<Long> m_metric_Progress;

    /** constructor */
    public FileCopyFromTask(Session session, FileImpl targetFile, DataAdaptor adaptor, URL effectiveSource, FlagsBytes effectiveFlags) throws NotImplemented {
        super(session, null, true);
        // check flags
        try {
            effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.CREATEPARENTS.or(Flags.OVERWRITE)));
        } catch (BadParameter e) {
            FileCopyFromTask.super.setException(e);
            FileCopyFromTask.super.setState(State.FAILED);
        }
        // internal
        m_copyFrom = new FileCopyFrom(session, targetFile, adaptor);
        m_effectiveSource = effectiveSource;
        m_effectiveFlags = effectiveFlags;
        // metrics
        m_totalWrittenBytes = 0;
        m_metric_Progress = (MetricImpl<Long>) this._addMetric(new MetricImpl<Long>(
                this,
                FileCopyTask.FILE_COPY_PROGRESS,
                "this metric gives the state of ongoing file transfer as number of bytes transfered.",
                MetricMode.ReadOnly,
                "bytes",
                MetricType.Int,
                new Long(0)));
    }

    void increment(long writtenBytes) {
        m_totalWrittenBytes += writtenBytes;
        m_metric_Progress.setValue(new Long(m_totalWrittenBytes));
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    private Thread m_thread;
    public void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        m_thread = new Thread(new Runnable(){
            public void run() {
                FileCopyFromTask.super.setState(State.RUNNING);
                try {
                    m_copyFrom.copyFrom(m_effectiveSource, m_effectiveFlags, FileCopyFromTask.this);
                    FileCopyFromTask.super.setState(State.DONE);
                } catch (Exception e) {
                    FileCopyFromTask.super.setException(new NoSuccess(e));
                    FileCopyFromTask.super.setState(State.FAILED);
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
