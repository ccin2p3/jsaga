package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.impl.url.URLHelper;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.IOException;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileOutputStreamImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileOutputStreamImpl extends AbstractAsyncFileOutputStreamImpl {
    private DataAdaptor m_connection;
    private OutputStream m_outStream;

    /** constructor */
    FileOutputStreamImpl(Session session, URL url, FileWriterStreamFactory adaptor, boolean disconnectable, boolean append, boolean exclusive) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session);

        // save connection
        m_connection = (disconnectable ? adaptor : null);

        // open stream
        URL fileUrl = URLHelper.toFileURL(url);
        URL parent = URLHelper.getParentURL(fileUrl);
        String fileName = URLHelper.getName(fileUrl);
        if (exclusive && append) {
            throw new BadParameterException("Incompatible flags: EXCL and APPEND");
        }
        try {
            m_outStream = adaptor.getOutputStream(
                    parent.getPath(),
                    fileName,
                    exclusive,
                    append,
                    fileUrl.getQuery());
        } catch(ParentDoesNotExist e) {
            throw new DoesNotExistException("Parent directory does not exist: "+parent, e.getCause());
        } catch(AlreadyExistsException e) {
            throw new AlreadyExistsException("File already exists: "+ fileUrl, e.getCause());
        }
        if (m_outStream == null) {
            throw new NoSuccessException("[ADAPTOR ERROR] Method getOutputStream() must not return 'null'", this);
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        FileOutputStreamImpl clone = (FileOutputStreamImpl) super.clone();
        clone.m_connection = m_connection;
        clone.m_outStream = m_outStream;
        return clone;
    }


    public void close() throws IOException {
        // close stream
        try { 
           m_outStream.close();
        } finally {
           // close connection
           if (m_connection != null) {
               try {
                   m_connection.disconnect();
               } catch (NoSuccessException e) {
                   throw new IOException(e.getMessage());
               }
           }
        }
    }

    /////////////////////////////////// interface OutputStream ///////////////////////////////////

    public void write(int b) throws IOException {m_outStream.write(b);}
    public void write(byte[] b) throws IOException {m_outStream.write(b);}
    public void write(byte[] b, int off, int len) throws IOException {m_outStream.write(b, off, len);}
    public void flush() throws IOException {m_outStream.flush();}
}
