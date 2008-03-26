package fr.in2p3.jsaga.impl.file.stream;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileInputStream;
import org.ogf.saga.file.FileOutputStream;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileStreamFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileStreamFactoryImpl {
    public static FileInputStream newFileInputStream(Session session, URL name, DataAdaptor adaptor, boolean disconnectable) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (adaptor instanceof FileReaderStreamFactory) {
            return new FileInputStreamImpl(session, name, (FileReaderStreamFactory) adaptor, disconnectable);
        } else if (adaptor instanceof FileReaderGetter) {
            return new FileInputStreamPipedImpl(session, name, (FileReaderGetter) adaptor, disconnectable);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ name.getScheme());
        }
    }

    public static FileOutputStream newFileOutputStream(Session session, URL name, DataAdaptor adaptor, boolean disconnectable, boolean append, boolean exclusive) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (adaptor instanceof FileWriterStreamFactory) {
            return new FileOutputStreamImpl(session, name, (FileWriterStreamFactory) adaptor, disconnectable, append, exclusive);
        } else if (adaptor instanceof FileWriterPutter) {
            return new FileOutputStreamPipedImpl(session, name, (FileWriterPutter) adaptor, disconnectable, append, exclusive);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ name.getScheme());
        }
    }
}
