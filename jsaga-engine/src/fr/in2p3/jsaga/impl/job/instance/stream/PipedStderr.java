package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOSetter;
import org.ogf.saga.error.NoSuccess;

import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PipedStderr
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PipedStderr extends PipedStdout {
    public PipedStderr(JobIOSetter ioHandler) throws NoSuccess {
        super(ioHandler);
    }

    public void run() {
        try {
            m_ioHandler.setStderr(m_out);
        } catch (Exception e) {
            m_exception = new IOException(e.getClass()+": "+e.getMessage());
        } finally {
            try {
                // pipe must be closed to unlock read attempt
                m_out.close();
            } catch (IOException e) {
                m_exception = e;
            }
            m_closed = true;
        }
    }
}
