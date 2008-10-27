package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import org.ogf.saga.error.NoSuccessException;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PipedInputStreamImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PipedInputStreamImpl extends PipedInputStream implements Runnable {
    private FileReaderGetter m_adaptor;
    private String m_absolutePath;
    private String m_additionalArgs;
    private IOException m_exception;
    private boolean m_closed;
    private OutputStream m_out;

    public PipedInputStreamImpl(FileReaderGetter adaptor, String absolutePath, String additionalArgs) throws NoSuccessException {
        m_adaptor = adaptor;
        m_absolutePath = absolutePath;
        m_additionalArgs = additionalArgs;
        m_exception = null;
        m_closed = false;
        try {
            // pipe must be connected before reading (else will throw exception)
            m_out = new PipedOutputStream(this);
        } catch (IOException e) {
            throw new NoSuccessException(e);
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
        try {
            m_adaptor.getToStream(
                    m_absolutePath,
                    m_additionalArgs,
                    m_out);
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
