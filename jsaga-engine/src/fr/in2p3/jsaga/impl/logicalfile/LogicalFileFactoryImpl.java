package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalFileFactoryImpl extends LogicalFileFactory {
    private DataAdaptorFactory m_adaptorFactory;

    public LogicalFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
    }

    protected LogicalFile doCreateLogicalFile(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        if (isLogical || !isPhysical) {
            return new LogicalFileImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameter("Not a logical file URL: "+name);
        }
    }

    protected LogicalDirectory doCreateLogicalDirectory(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        if (isLogical || !isPhysical) {
            return new LogicalDirectoryImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameter("Not a logical directory URL: "+name);
        }
    }

    protected Task<LogicalFile> doCreateLogicalFile(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    LogicalFileFactoryImpl.class.getMethod("doCreateLogicalFile", new Class[]{Session.class, URL.class, int.class}),
                    new Object[]{session, name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    protected Task<LogicalDirectory> doCreateLogicalDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    LogicalFileFactoryImpl.class.getMethod("doCreateLogicalDirectory", new Class[]{Session.class, URL.class, int.class}),
                    new Object[]{session, name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
