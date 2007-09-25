package fr.in2p3.jsaga.adaptor.data;

import org.globus.ftp.DataSourceStream;
import org.globus.ftp.GridFTPClient;
import org.ogf.saga.error.NoSuccess;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpOutputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpOutputStream extends PipedOutputStream implements Runnable {
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private GridFTPClient m_client;
    private String m_absolutePath;
    private boolean m_append;
    private IOException m_exception;
    private boolean m_closed;
    private InputStream m_in;

    public GsiftpOutputStream(GridFTPClient client, String absolutePath, boolean append) throws NoSuccess {
        m_client = client;
        m_absolutePath = absolutePath;
        m_append = append;
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
            m_client.put(
                    m_absolutePath,
                    new DataSourceStream(m_in, DEFAULT_BUFFER_SIZE),
                    null,
                    m_append);
        } catch (IOException e) {
            m_exception = e;
        } catch (Exception e) {
            try {
                throw GsiftpDataAdaptorAbstract.rethrowException(e);
            } catch (Exception rethrown) {
                m_exception = new IOException(rethrown.getClass()+": "+rethrown.getMessage());
            }
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
