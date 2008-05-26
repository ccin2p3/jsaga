package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.impl.job.instance.JobImpl;
import org.ogf.saga.error.*;

import java.io.*;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStderrInputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobStderrInputStream extends Stdout {
    protected JobImpl m_job;
    private JobIOHandler m_ioHandler;

    public JobStderrInputStream(JobImpl job, JobIOHandler ioHandler) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        m_job = job;
        m_ioHandler = ioHandler;
        switch(m_job.getJobState()) {
            case DONE:
            case CANCELED:
            case FAILED:
            case RUNNING:
                // OK
                break;
            default:
                throw new DoesNotExist("Stderr is not available because job is neither finished nor running: "+m_job.getState());
        }
    }

    /** constructor for InteractiveJobStreamSet */
    protected JobStderrInputStream(JobImpl job) {
        m_job = job;
    }

    public void closeJobIOHandler() throws PermissionDenied, Timeout, NoSuccess {
        // get stream
        if (m_stream == null) {
            if (m_ioHandler instanceof JobIOGetterPseudo) {
                m_stream = ((JobIOGetterPseudo)m_ioHandler).getStderr();
            } else if (m_ioHandler instanceof JobIOSetterPseudo) {
                m_stream = new PipedStderr((JobIOSetterPseudo) m_ioHandler);
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
            throw new NoSuccess(e);
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
                        throw new NoSuccess("INTERNAL ERROR: JobIOHandler has not been closed");
                    }
                    return m_buffer;
                case RUNNING:
                    if (m_stream == null) {
                        if (m_ioHandler instanceof JobIOGetterPseudo) {
                            m_stream = ((JobIOGetterPseudo)m_ioHandler).getStderr();
                        } else if (m_ioHandler instanceof JobIOSetterPseudo) {
                            m_stream = new PipedStderr((JobIOSetterPseudo) m_ioHandler);
                        } else {
                            throw new NoSuccess("Can not read from stderr because job is running and adaptor does not support job interactivity");
                        }
                    }
                    return m_stream;
                default:
                    throw new DoesNotExist("Stderr is not available because job is neither finished nor running");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
