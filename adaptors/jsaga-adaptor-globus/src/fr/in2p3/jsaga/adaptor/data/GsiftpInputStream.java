package fr.in2p3.jsaga.adaptor.data;

import org.globus.ftp.DataSinkStream;
import org.globus.ftp.GridFTPClient;
import org.ogf.saga.error.NoSuccess;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpInputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpInputStream extends PipedInputStream implements Runnable {
    private GridFTPClient m_client;
    private String m_absolutePath;
    private IOException m_exception;
    private boolean m_closed;
    private OutputStream m_out;

    public GsiftpInputStream(GridFTPClient client, String absolutePath) throws NoSuccess {
        m_client = client;
        m_absolutePath = absolutePath;
        m_exception = null;
        m_closed = false;
        try {
            // pipe must be connected before reading (else will throw exception)
            m_out = new PipedOutputStream(this);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
        new Thread(this).start();
    }

    public synchronized int read() throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        int c = super.read();
        if (m_exception != null) {
            throw m_exception;
        }
        return c;
    }

    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        int readlen = super.read(bytes, off, len);
        if (m_exception != null) {
            throw m_exception;
        }
        return readlen;
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
        final boolean autoFlush = false;
        final boolean ignoreOffset = true;
        try {
            m_client.get(
                    m_absolutePath,
                    new DataSinkStream(m_out, autoFlush, ignoreOffset),
                    null);
        } catch (IOException e) {
            m_exception = e;
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
