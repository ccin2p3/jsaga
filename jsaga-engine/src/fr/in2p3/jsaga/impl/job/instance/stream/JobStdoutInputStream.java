package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import org.ogf.saga.error.*;
import org.ogf.saga.task.Task;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStdoutInputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobStdoutInputStream extends Stdout {
    protected Task m_job;
    private JobIOHandler m_ioHandler;

    public JobStdoutInputStream(Task job, JobIOHandler ioHandler) throws NotImplementedException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_job = job;
        m_ioHandler = ioHandler;
        switch(m_job.getState()) {
            case DONE:
            case CANCELED:
            case FAILED:
            case RUNNING:
                // OK
                break;
            default:
                throw new DoesNotExistException("Stdout is not available because job is neither finished nor running: "+m_job.getState());
        }
    }

    /** constructor for StreamableJobInteractiveSet */
    protected JobStdoutInputStream(Task job) {
        m_job = job;
    }

    public void closeJobIOHandler() throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // get stream
        if (m_stream == null) {
            if (m_ioHandler instanceof JobIOGetter) {
                m_stream = ((JobIOGetter)m_ioHandler).getStdout();
            } else if (m_ioHandler instanceof JobIOSetter) {
                m_stream = new PipedStdout((JobIOSetter) m_ioHandler);
            }
        }

        // dump stream to buffer
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int len;
            byte[] bytes = new byte[1024];
            while ( (len=m_stream.read(bytes)) > -1 ) {
                buffer.write(bytes, 0, len);
            }
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
        m_buffer = new ByteArrayInputStream(buffer.toByteArray());
    }

    /////////////////////////////////// interface InputStream ///////////////////////////////////

    public int read() throws IOException {return this.getStream().read();}
    public int read(byte[] b) throws IOException {return this.getStream().read(b);}
    public int read(byte[] b, int off, int len) throws IOException {return this.getStream().read(b, off, len);}
    public long skip(long n) throws IOException {return this.getStream().skip(n);}
    public void close() throws IOException {this.getStream().close();}

    /////////////////////////////////////// private method ///////////////////////////////////////

    private ByteArrayInputStream m_buffer;
    private InputStream m_stream;
    private InputStream getStream() throws IOException {
        try {
            switch(m_job.getState()) {
                case DONE:
                case CANCELED:
                case FAILED:
                    if (m_buffer == null) {
                        throw new NoSuccessException("INTERNAL ERROR: JobIOHandler has not been closed");
                    }
                    return m_buffer;
                case RUNNING:
                    if (m_stream == null) {
                        if (m_ioHandler instanceof JobIOGetter) {
                            m_stream = ((JobIOGetter)m_ioHandler).getStdout();
                        } else if (m_ioHandler instanceof JobIOSetter) {
                            m_stream = new PipedStdout((JobIOSetter) m_ioHandler);
                        } else {
                            throw new NoSuccessException("Can not read from stdout because job is running and adaptor does not support job interactivity");
                        }
                    }
                    return m_stream;
                default:
                    throw new DoesNotExistException("Stdout is not available because job is neither finished nor running");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
