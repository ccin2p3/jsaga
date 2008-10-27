package fr.in2p3.jsaga.impl.job.instance.stream;

import org.ogf.saga.error.*;

import java.io.IOException;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GetterInputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GetterInputStream extends Stdout {
    private GetterBufferedInputStream m_buffer;
    private InputStream m_stream;

    public GetterInputStream(InputStream stdout) throws NotImplementedException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_buffer = new GetterBufferedInputStream(stdout);
        m_stream = stdout;
    }

    public void closeJobIOHandler() throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // do nothing
    }

    /////////////////////////////////// interface InputStream ///////////////////////////////////

    public int read() throws IOException {return this.stream().read();}
    public int read(byte[] b) throws IOException {return this.stream().read(b);}
    public int read(byte[] b, int off, int len) throws IOException {return this.stream().read(b, off, len);}
    public long skip(long n) throws IOException {return this.stream().skip(n);}
    public void close() throws IOException {this.stream().close();}

    /////////////////////////////////////// private method ///////////////////////////////////////

    private InputStream stream() throws IOException {
        //todo: use stream when buffer is empty
        return m_buffer;
    }
}
