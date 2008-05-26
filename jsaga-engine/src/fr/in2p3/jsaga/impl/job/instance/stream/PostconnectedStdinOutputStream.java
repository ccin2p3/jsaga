package fr.in2p3.jsaga.impl.job.instance.stream;

import fr.in2p3.jsaga.impl.job.instance.JobImpl;
import org.ogf.saga.error.*;

import java.io.*;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PostconnectedStdinOutputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PostconnectedStdinOutputStream extends JobStdinOutputStream {
    private InputStreamContainer m_in;

    /** constructor for InteractiveJobStreamSet */
    public PostconnectedStdinOutputStream(JobImpl job) throws Timeout, DoesNotExist, NoSuccess, NotImplemented {
        super(job);
        m_in = new InputStreamContainer();
    }

    public InputStream getInputStreamContainer() {
        return m_in;
    }

    /////////////////////////////////// interface OutputStream ///////////////////////////////////

    public void write(int b) throws IOException {this.stream().write(b);}
    public void write(byte[] b) throws IOException {this.stream().write(b);}
    public void write(byte[] b, int off, int len) throws IOException {this.stream().write(b, off, len);}
    public void flush() throws IOException {this.stream().flush();}
    public void close() throws IOException {this.stream().close(); m_in.finishWriting();}

    /////////////////////////////////////// private method ///////////////////////////////////////

    private OutputStream stream() throws IOException {
        try {
            switch(m_job.getState()) {
                case NEW:
                    return m_in.getOutputStream();
                case RUNNING:
                    throw new NoSuccess("Not supported yet...");
                default:
                    throw new DoesNotExist("Stdin is not available because job is finished or suspended");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
