package fr.in2p3.jsaga.impl.job.instance.stream;

import org.ogf.saga.error.NoSuccess;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GetterBufferedInputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GetterBufferedInputStream extends InputStream implements Runnable {
    private InputStream m_stream;
    private ByteArrayOutputStream m_buffer;
    private ByteArrayInputStream m_bufferedStream;
    private IOException m_exception;
    private boolean m_closed;

    public GetterBufferedInputStream(InputStream stdout) throws NoSuccess {
        m_stream = stdout;
        m_buffer = new ByteArrayOutputStream();
        m_bufferedStream = null;
        m_exception = null;
        m_closed = false;
        new Thread(this).start();
    }

    public synchronized int read() throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        int c = this.stream().read();
        if (m_exception != null) {
            throw m_exception;
        }
        return c;
    }

    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        int readlen = this.stream().read(bytes, off, len);
        if (m_exception != null) {
            throw m_exception;
        }
        return readlen;
    }

    public void close() throws IOException {
        if (m_exception != null) {
            throw m_exception;
        }
        this.stream().close();
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

    public boolean isClosed() {
        return m_closed;
    }

    public void run() {
        try {
            byte[] bytes = new byte[1024];
            for (int len; (len=m_stream.read(bytes))>-1; ) {
                m_buffer.write(bytes, 0, len);
            }
        } catch (Exception e) {
            m_exception = new IOException(e.getClass()+": "+e.getMessage());
        } finally {
            try {
                // pipe must be closed to unlock read attempt
                m_stream.close();
            } catch (IOException e) {
                m_exception = e;
            }
            m_closed = true;
        }
    }

    private InputStream stream() throws IOException {
        if (m_bufferedStream == null) {
            m_buffer.close();
            m_bufferedStream = new ByteArrayInputStream(m_buffer.toByteArray());
        }
        return m_bufferedStream;
    }
}
