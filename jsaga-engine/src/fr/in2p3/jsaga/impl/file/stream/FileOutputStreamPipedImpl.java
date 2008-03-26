package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.helpers.URLFactory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileOutputStreamPipedImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileOutputStreamPipedImpl extends AbstractAsyncFileOutputStreamImpl {
    private DataAdaptor m_connection;
    private PipedOutputStreamImpl m_outStream;

    /** constructor */
    FileOutputStreamPipedImpl(Session session, URL url, FileWriterPutter adaptor, boolean disconnectable, boolean append, boolean exclusive) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session);

        // save connection
        m_connection = (disconnectable ? adaptor : null);

        // open stream
        URL fileUrl = URLFactory.toFileURL(url);
        URL parent = URLFactory.getParentURL(fileUrl);
        if (exclusive && append) {
            throw new BadParameter("Incompatible flags: EXCL and APPEND");
        } else if (adaptor instanceof DataReaderAdaptor) {
            DataReaderAdaptor a = (DataReaderAdaptor) adaptor;
            if (exclusive && a.exists(fileUrl.getPath(), fileUrl.getQuery())) {
                // need to check existence explicitely, else exception is never thrown
                throw new AlreadyExists("File already exists: "+fileUrl);
            } else if (!a.exists(parent.getPath(), parent.getQuery())) {
                // need to check existence explicitely, else exception is thrown to late (when writing bytes)
                throw new DoesNotExist("Parent directory does not exist: "+parent);
            }
        }
        m_outStream = new PipedOutputStreamImpl(
                adaptor,
                fileUrl.getPath(),
                fileUrl.getQuery(),
                append);
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        FileOutputStreamPipedImpl clone = (FileOutputStreamPipedImpl) super.clone();
        clone.m_connection = m_connection;
        clone.m_outStream = m_outStream;
        return clone;
    }


    public void close() throws IOException {
        // close stream
        m_outStream.close();

        // close connection
        if (m_connection != null) {
            try {
                m_connection.disconnect();
            } catch (NoSuccess e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    /////////////////////////////////// interface OutputStream ///////////////////////////////////

    public void write(int b) throws IOException {m_outStream.write(b);}
    public void write(byte[] b) throws IOException {m_outStream.write(b);}
    public void write(byte[] b, int off, int len) throws IOException {m_outStream.write(b, off, len);}
    public void flush() throws IOException {m_outStream.flush();}
}
