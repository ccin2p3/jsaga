package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.task.AbstractTaskImplWithAsyncAttributes;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.task.Task;

import java.lang.Exception;

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
public class FileCopyTask extends AbstractTaskImplWithAsyncAttributes implements Task {
    private FileCopy m_copy;
    private URL m_effectiveTarget;
    private FlagsBytes m_effectiveFlags;

    /** constructor */
    public FileCopyTask(Session session, FileImpl sourceFile, DataAdaptor adaptor, URL effectiveTarget, FlagsBytes effectiveFlags) throws NotImplemented, BadParameter, IncorrectState, Timeout, NoSuccess {
        super(session, null, true);
        m_copy = new FileCopy(session, sourceFile, adaptor);
        m_effectiveTarget = effectiveTarget;
        m_effectiveFlags = effectiveFlags;
    }

    ////////////////////////////////////// implementation of AbstractTaskImpl //////////////////////////////////////

    private Thread m_thread;
    public void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        m_thread = new Thread(new Runnable(){
            public void run() {
                FileCopyTask.super.setState(State.RUNNING);
                try {
                    m_copy.copy(m_effectiveTarget, m_effectiveFlags);
                    FileCopyTask.super.setState(State.DONE);
                } catch (Exception e) {
                    FileCopyTask.super.setException(new NoSuccess(e));
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
