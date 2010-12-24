package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import org.ogf.saga.error.NoSuccessException;

import java.io.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   PipedOutputStreamImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   21 mars 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class PipedOutputStreamImpl extends PipedOutputStream implements Runnable {

    private FileWriterPutter m_adaptor;
    private String m_absolutePath;
    private String m_additionalArgs;
    private boolean m_append;
    private IOException m_exception;
    private InputStream m_in;
    private Thread thread = new Thread(this);

    public PipedOutputStreamImpl(FileWriterPutter adaptor, String absolutePath, String additionalArgs, boolean append) throws NoSuccessException {
        m_adaptor = adaptor;
        m_absolutePath = absolutePath;
        m_additionalArgs = additionalArgs;
        m_append = append;
        m_exception = null;

        try {
            // pipe must be connected before writing (else will hang on 2nd test case)
            m_in = new PipedInputStream(this);
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
        thread.start();
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
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new IOException("InterruptedException: " + e.getMessage());
        }
    }

    public void run() {
        try {
            m_adaptor.putFromStream(
                    m_absolutePath,
                    m_append,
                    m_additionalArgs,
                    m_in);
        } catch (Throwable e) {
            m_exception = new IOException(e.getClass() + ": " + e.getMessage(), e);
        } finally {
            try {
                m_in.close();
            } catch (IOException e) {
                m_exception = e;
            }
        }
    }
}
