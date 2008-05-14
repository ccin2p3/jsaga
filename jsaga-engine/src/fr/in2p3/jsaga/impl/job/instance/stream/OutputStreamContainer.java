package fr.in2p3.jsaga.impl.job.instance.stream;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   OutputStreamContainer
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class OutputStreamContainer extends OutputStream {
    private boolean m_isClosed;

    public OutputStreamContainer() {
        m_isClosed = false;
    }

    public boolean isClosed() {
        return m_isClosed;
    }

    public InputStream getInputStream() throws InterruptedException {
        while(!m_isClosed) {
            Thread.currentThread().sleep(100);
        }
        if (m_buffer == null) {
            m_buffer = new ByteArrayOutputStream();
        }
        return new ByteArrayInputStream(m_buffer.toByteArray());
    }

    /////////////////////////////////// interface OutputStream ///////////////////////////////////

    public void write(int b) throws IOException {this.stream().write(b);}
    public void write(byte[] b) throws IOException {this.stream().write(b);}
    public void write(byte[] b, int off, int len) throws IOException {this.stream().write(b, off, len);}
    public void flush() throws IOException {this.stream().flush();}
    public void close() throws IOException {this.stream().close(); m_isClosed=true;}

    /////////////////////////////////////// private method ///////////////////////////////////////

    private ByteArrayOutputStream m_buffer;
    private OutputStream stream() throws IOException {
        if (m_buffer == null) {
            m_buffer = new ByteArrayOutputStream();
        }
        return m_buffer;
    }
}
