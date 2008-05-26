package fr.in2p3.jsaga.impl.job.instance.stream;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   InputStreamContainer
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class InputStreamContainer extends InputStream {
    private ByteArrayOutputStream m_out;

    public InputStreamContainer() {
        m_out = new ByteArrayOutputStream();
    }

    public OutputStream getOutputStream() {
        return m_out;
    }

    public void finishWriting() throws IOException {
        m_out.close();
        m_buffer = new ByteArrayInputStream(m_out.toByteArray());
    }

    /////////////////////////////////// interface InputStream ///////////////////////////////////

    public int read() throws IOException {return this.stream().read();}
    public int read(byte[] b) throws IOException {return this.stream().read(b);}
    public int read(byte[] b, int off, int len) throws IOException {return this.stream().read(b, off, len);}
    public long skip(long n) throws IOException {return this.stream().skip(n);}
    public void close() throws IOException {this.stream().close();}

    /////////////////////////////////////// private method ///////////////////////////////////////

    private ByteArrayInputStream m_buffer;
    private InputStream stream() throws IOException {
        if (m_buffer != null) {
            return m_buffer;
        } else {
            return new ByteArrayInputStream(new byte[]{});
        }
    }
}
