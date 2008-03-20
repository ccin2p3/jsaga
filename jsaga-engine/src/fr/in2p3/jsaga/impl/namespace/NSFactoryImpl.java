package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.file.DirectoryImpl;
import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalDirectoryImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSFactoryImpl extends NSFactory {
    private DataAdaptorFactory m_adaptorFactory;

    public NSFactoryImpl(DataAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - do not throw BadParameter exception if entry is a directory, create a NSDirectory object instead.
     */
    protected NSEntry doCreateNSEntry(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (name.getPath().endsWith("/")) {
            return this.doCreateNSDirectory(session, name, flags);
        } else {
            return this.doCreateNSFile(session, name, flags);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - support the CREATEPARENTS flag (from specification of method makeDir).
     */
    protected NSDirectory doCreateNSDirectory(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new DirectoryImpl(session, name, adaptor, flags);
        } else {
            return new LogicalDirectoryImpl(session, name, adaptor, flags);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - support the CREATEPARENTS flag (from specification of method makeDir).
     */
    private NSEntry doCreateNSFile(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        boolean isPhysical = adaptor instanceof FileReader || adaptor instanceof FileWriter;
        boolean isLogical = adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter;
        if (isPhysical || !isLogical) {
            return new FileImpl(session, name, adaptor, flags);
        } else {
            return new LogicalFileImpl(session, name, adaptor, flags);
        }
    }

    protected Task<NSEntry> doCreateNSEntry(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    NSFactoryImpl.class.getMethod("doCreateNSEntry", new Class[]{Session.class, URL.class, int.class}),
                    new Object[]{session, name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    protected Task<NSDirectory> doCreateNSDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        try {
            return AbstractSagaObjectImpl.prepareTask(mode, new GenericThreadedTask(
                    null,
                    this,
                    NSFactoryImpl.class.getMethod("doCreateNSDirectory", new Class[]{Session.class, URL.class, int.class}),
                    new Object[]{session, name, flags}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
