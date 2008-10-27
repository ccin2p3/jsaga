package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.file.stream.FileStreamFactoryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileFactoryImpl extends FileFactory {
    private DataAdaptorFactory m_adaptorFactory;

    public FileFactoryImpl(DataAdaptorFactory adaptorFactory) {
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

    protected File doCreateFile(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new FileImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameterException("Not a physical file URL: "+name);
        }
    }

    protected FileInputStream doCreateFileInputStream(Session session, URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean disconnectable = true;
        return FileStreamFactoryImpl.newFileInputStream(session, name, adaptor, disconnectable);
    }
    public static FileInputStream openFileInputStream(Session session, URL name, DataAdaptor adaptor) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        boolean disconnectable = false;
        return FileStreamFactoryImpl.newFileInputStream(session, name, adaptor, disconnectable);
    }

    protected FileOutputStream doCreateFileOutputStream(Session session, URL name, boolean append) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean disconnectable = true;
        boolean exclusive = false;
        return FileStreamFactoryImpl.newFileOutputStream(session, name, adaptor, disconnectable, append, exclusive);
    }
    public static FileOutputStream openFileOutputStream(Session session, URL name, DataAdaptor adaptor, boolean append, boolean exclusive) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        boolean disconnectable = false;
        return FileStreamFactoryImpl.newFileOutputStream(session, name, adaptor, disconnectable, append, exclusive);
    }

    protected Directory doCreateDirectory(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new DirectoryImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameterException("Not a physical directory URL: "+name);
        }
    }

    protected Task<FileFactory, File> doCreateFile(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,File>().create(
                mode, null, this,
                "doCreateFile",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }

    protected Task<FileFactory, FileInputStream> doCreateFileInputStream(TaskMode mode, Session session, URL name) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,FileInputStream>().create(
                mode, null, this,
                "doCreateFileInputStream",
                new Class[]{Session.class, URL.class},
                new Object[]{session, name});
    }

    protected Task<FileFactory, FileOutputStream> doCreateFileOutputStream(TaskMode mode, Session session, URL name, boolean append) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,FileOutputStream>().create(
                mode, null, this,
                "doCreateFileOutputStream",
                new Class[]{Session.class, URL.class, boolean.class},
                new Object[]{session, name, append});
    }

    protected Task<FileFactory, Directory> doCreateDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplementedException {
        return new GenericThreadedTaskFactory<FileFactory,Directory>().create(
                mode, null, this,
                "doCreateDirectory",
                new Class[]{Session.class, URL.class, int.class},
                new Object[]{session, name, flags});
    }
}
