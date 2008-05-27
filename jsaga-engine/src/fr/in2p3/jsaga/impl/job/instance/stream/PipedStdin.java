package fr.in2p3.jsaga.impl.job.instance.stream;

import org.ogf.saga.error.NoSuccess;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PipedStdin
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 avr. 2008
* ***************************************************
* Description:                                      */
/**
 * TODO: remove this class (not used) ???
 */
public class PipedStdin extends PipedOutputStream implements Runnable {
    protected IOException m_exception;
    protected boolean m_closed;
    protected InputStream m_in;

    public PipedStdin() throws NoSuccess {
        m_exception = null;
        m_closed = false;
        try {
            // pipe must be connected before writing (else will hang on 2nd test case)
            m_in = new PipedInputStream(this);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
        new Thread(this).start();        
    }

    public void write(int b) throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        super.write(b);
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        super.write(bytes, off, len);
    }

    public void close() throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        super.close();
        while (!m_closed) {
            if (m_exception != null) {
                throw m_exception;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new IOException("InterruptedException: "+e.getMessage());
            }
        }
    }

    public void run() {
        try {
//            m_ioHandler.setStdin(m_in);
        } catch (Exception e) {
            m_exception = new IOException(e.getClass()+": "+e.getMessage());
        } finally {
            try {
                m_in.close();
            } catch (IOException e) {
                m_exception = e;
            }
            m_closed = true;
        }
    }
}
