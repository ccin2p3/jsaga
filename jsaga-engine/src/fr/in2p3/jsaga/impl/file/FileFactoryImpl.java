package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;

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

    protected IOVec doCreateIOVec(byte[] data, int lenIn) throws BadParameter {
        throw new BadParameter("Not implemented by the SAGA engine");
    }

    protected IOVec doCreateIOVec(int size, int lenIn) throws BadParameter, NoSuccess {
        throw new BadParameter("Not implemented by the SAGA engine");
    }

    protected IOVec doCreateIOVec(byte[] data) throws BadParameter, NoSuccess {
        throw new BadParameter("Not implemented by the SAGA engine");
    }

    protected IOVec doCreateIOVec(int size) throws BadParameter, NoSuccess {
        throw new BadParameter("Not implemented by the SAGA engine");
    }

    protected File doCreateFile(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            throw new BadParameter("Not a physical file URL: "+name);
        } else {
            return new FileImpl(session, name, adaptor, flags);
        }
    }

    protected FileInputStream doCreateFileInputStream(Session session, URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        throw new BadParameter("Not implemented yet...");   //todo: implement method doCreateFileInputStream()
    }

    protected FileOutputStream doCreateFileOutputStream(Session session, URL name, boolean append) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        throw new BadParameter("Not implemented yet...");   //todo: implement method doCreateFileOutputStream()
    }

    protected Directory doCreateDirectory(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            throw new BadParameter("Not a physical directory URL: "+name);
        } else {
            return new DirectoryImpl(session, name, adaptor, flags);
        }
    }

    protected Task<File> doCreateFile(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    FileFactoryImpl.class.getMethod("doCreateFile", new Class[]{Session.class, URL.class, int.class}),
                    new Object[]{session, name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    protected Task<FileInputStream> doCreateFileInputStream(TaskMode mode, Session session, URL name) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    FileFactoryImpl.class.getMethod("doCreateFileInputStream", new Class[]{Session.class, URL.class}),
                    new Object[]{session, name}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    protected Task<FileOutputStream> doCreateFileOutputStream(TaskMode mode, Session session, URL name, boolean append) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    FileFactoryImpl.class.getMethod("doCreateFileOutputStream", new Class[]{Session.class, URL.class, boolean.class}),
                    new Object[]{session, name, append}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    protected Task<Directory> doCreateDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    FileFactoryImpl.class.getMethod("doCreateDirectory", new Class[]{Session.class, URL.class, int.class}),
                    new Object[]{session, name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
