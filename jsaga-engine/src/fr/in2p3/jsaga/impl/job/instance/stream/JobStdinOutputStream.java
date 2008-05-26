package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;

import java.io.*;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStdinOutputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobStdinOutputStream extends Stdin {
    protected Job m_job;
    private JobIOHandler m_ioHandler;

    public JobStdinOutputStream(Job job) throws NotImplemented, DoesNotExist, Timeout, NoSuccess {
        m_job = job;
        switch(m_job.getState()) {
            case NEW:
            case RUNNING:
                // OK
                break;
            default:
                throw new DoesNotExist("Stdin is not available because job is ended or suspended");
        }
    }

    public byte[] getBuffer() {
        if (m_buffer != null) {
            return m_buffer.toByteArray();
        } else {
            return new byte[0];
        }
    }
    
    public void openJobIOHandler(JobIOHandler ioHandler) throws NotImplemented, PermissionDenied, Timeout, NoSuccess {
        m_ioHandler = ioHandler;

        // get stream
        if (m_stream == null) {
            if (m_ioHandler instanceof JobIOGetter) {
                m_stream = ((JobIOGetter)m_ioHandler).getStdin();
            } else if (m_ioHandler instanceof JobIOSetter) {
                m_stream = new PipedStdin((JobIOSetter) m_ioHandler);
            } else if (m_ioHandler instanceof JobIOGetterPseudo || m_ioHandler instanceof JobIOSetterPseudo) {
                throw new NotImplemented("ADAPTOR ERROR: Inconsistent implementation (InteractiveJobAdaptor with JobIOHandlerPseudo)");
            }
        }

        // dump buffer to stream
        if (m_buffer!=null && m_buffer.size()>0) {
            try {
                m_stream.write(m_buffer.toByteArray());
                m_stream.close();
            } catch (IOException e) {
                throw new NoSuccess(e);
            }
        }
    }

    /////////////////////////////////// interface OutputStream ///////////////////////////////////

    public void write(int b) throws IOException {this.getStream().write(b);}
    public void write(byte[] b) throws IOException {this.getStream().write(b);}
    public void write(byte[] b, int off, int len) throws IOException {this.getStream().write(b, off, len);}
    public void flush() throws IOException {this.getStream().flush();}
    public void close() throws IOException {this.getStream().close();}

    /////////////////////////////////////// private method ///////////////////////////////////////

    private ByteArrayOutputStream m_buffer;
    private OutputStream m_stream;
    private OutputStream getStream() throws IOException {
        try {
            switch(m_job.getState()) {
                case NEW:
                    if (m_buffer == null) {
                        m_buffer = new ByteArrayOutputStream();
                    }
                    return m_buffer;
                case RUNNING:
                    if (m_stream == null) {
                        if (m_ioHandler instanceof JobIOGetter) {
                            m_stream = ((JobIOGetter)m_ioHandler).getStdin();
                        } else if (m_ioHandler instanceof JobIOSetter) {
                            m_stream = new PipedStdin((JobIOSetter) m_ioHandler);
                        } else if (m_ioHandler instanceof JobIOGetterPseudo || m_ioHandler instanceof JobIOSetterPseudo) {
                            throw new NotImplemented("Can not write to stdin because job is running and adaptor does not support job interactivity");
                        }
                    }
                    return m_stream;
                default:
                    throw new DoesNotExist("Stdin is not available because job neither unsubmitted nor running");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
