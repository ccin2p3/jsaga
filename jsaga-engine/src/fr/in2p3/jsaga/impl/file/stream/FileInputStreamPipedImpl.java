package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.impl.url.URLHelper;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileInputStreamPipedImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileInputStreamPipedImpl extends AbstractAsyncFileInputStreamImpl {
    private DataAdaptor m_connection;
    private PipedInputStreamImpl m_inStream;

    /** constructor */
    FileInputStreamPipedImpl(Session session, URL url, FileReaderGetter adaptor, boolean disconnectable) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session);

        // save connection
        m_connection = (disconnectable ? adaptor : null);

        // open stream
        URL fileUrl = URLHelper.toFileURL(url);
        m_inStream = new PipedInputStreamImpl(
                adaptor,
                fileUrl.getPath(),
                fileUrl.getQuery());
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        FileInputStreamPipedImpl clone = (FileInputStreamPipedImpl) super.clone();
        clone.m_connection = m_connection;
        clone.m_inStream = m_inStream;
        return clone;
    }

    public void close() throws IOException {
        // close stream
        m_inStream.close();

        // close connection
        if (m_connection != null) {
            try {
                m_connection.disconnect();
            } catch (NoSuccessException e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    /////////////////////////////////// interface InputStream ///////////////////////////////////

    public int read() throws IOException {return m_inStream.read();}
    public int read(byte[] b) throws IOException {return m_inStream.read(b);}
    public int read(byte[] b, int off, int len) throws IOException {return m_inStream.read(b, off, len);}
    public long skip(long n) throws IOException {return m_inStream.skip(n);}
    public int available() throws IOException {return m_inStream.available();}
    public synchronized void mark(int readlimit) {m_inStream.mark(readlimit);}
    public synchronized void reset() throws IOException {m_inStream.reset();}
    public boolean markSupported() {return m_inStream.markSupported();}
}
