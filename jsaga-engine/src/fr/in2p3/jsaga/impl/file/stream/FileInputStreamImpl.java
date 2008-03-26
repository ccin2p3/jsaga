package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.helpers.URLFactory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.io.IOException;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileInputStreamImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileInputStreamImpl extends AbstractAsyncFileInputStreamImpl {
    private DataAdaptor m_connection;
    private InputStream m_inStream;

    /** constructor */
    FileInputStreamImpl(Session session, URL url, FileReaderStreamFactory adaptor, boolean disconnectable) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session);

        // save connection
        m_connection = (disconnectable ? adaptor : null);

        // open stream
        URL fileUrl = URLFactory.toFileURL(url);
        try {
            m_inStream = adaptor.getInputStream(
                    fileUrl.getPath(),
                    fileUrl.getQuery());
        } catch(DoesNotExist e) {
            throw new DoesNotExist("File does not exist: "+fileUrl, e.getCause());
        }
        if (m_inStream == null) {
            throw new NoSuccess("[ADAPTOR ERROR] Method getInputStream() must not return 'null'", this);
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        FileInputStreamImpl clone = (FileInputStreamImpl) super.clone();
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
            } catch (NoSuccess e) {
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
