package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.file.stream.FileStreamFactoryImpl;
import fr.in2p3.jsaga.sync.file.SyncFileFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncFileFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncFileFactoryImpl extends FileFactory implements SyncFileFactory {
    private static final boolean PLUGIN_TYPE = DataAdaptorFactory.PHYSICAL;
    private DataAdaptorFactory m_adaptorFactory;

    public AbstractSyncFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
    }

    protected IOVec doCreateIOVec(byte[] data, int lenIn) throws BadParameterException {
        throw new BadParameterException("Not implemented by the SAGA engine");
    }

    protected IOVec doCreateIOVec(int size, int lenIn) throws BadParameterException, NoSuccessException {
        throw new BadParameterException("Not implemented by the SAGA engine");
    }

    protected IOVec doCreateIOVec(byte[] data) throws BadParameterException, NoSuccessException {
        throw new BadParameterException("Not implemented by the SAGA engine");
    }

    protected IOVec doCreateIOVec(int size) throws BadParameterException, NoSuccessException {
        throw new BadParameterException("Not implemented by the SAGA engine");
    }

    public File doCreateFileSync(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session, PLUGIN_TYPE);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new FileImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameterException("Not a physical file URL: "+name);
        }
    }

    public FileInputStream doCreateFileInputStreamSync(Session session, URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session, PLUGIN_TYPE);
        boolean disconnectable = true;
        return FileStreamFactoryImpl.newFileInputStream(session, name, adaptor, disconnectable);
    }
    public static FileInputStream openFileInputStream(Session session, URL name, DataAdaptor adaptor) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        boolean disconnectable = false;
        return FileStreamFactoryImpl.newFileInputStream(session, name, adaptor, disconnectable);
    }

    public FileOutputStream doCreateFileOutputStreamSync(Session session, URL name, boolean append) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session, PLUGIN_TYPE);
        boolean disconnectable = true;
        boolean exclusive = false;
        return FileStreamFactoryImpl.newFileOutputStream(session, name, adaptor, disconnectable, append, exclusive);
    }
    public static FileOutputStream openFileOutputStream(Session session, URL name, DataAdaptor adaptor, boolean append, boolean exclusive) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        boolean disconnectable = false;
        return FileStreamFactoryImpl.newFileOutputStream(session, name, adaptor, disconnectable, append, exclusive);
    }

    public Directory doCreateDirectorySync(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session, PLUGIN_TYPE);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new DirectoryImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameterException("Not a physical directory URL: "+name);
        }
    }
}
