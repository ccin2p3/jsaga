package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.file.DirectoryImpl;
import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalDirectoryImpl;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

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

    protected Task<NSEntry> doCreateNSEntry(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected Task<NSDirectory> doCreateNSDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - do not throw BadParameter exception if entry is a directory, create a NSDirectory object instead.
     */
    protected NSEntry doCreateNSEntry(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (name.getPath().endsWith("/")) {
            return this.doCreateNSDirectory(session, name, flags);
        } else {
            return this.doCreateNamespaceFile(session, name, flags);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - support the CREATEPARENTS flag (from specification of method makeDir).
     */
    protected NSDirectory doCreateNSDirectory(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            return new LogicalDirectoryImpl(session, name, adaptor, flags);
        } else {
            return new DirectoryImpl(session, name, adaptor, flags);
        }
    }

    /**
     * Notes:
     * <br> - do not check existance of entry if flag <code>JSAGAFlags.BYPASSEXIST</code> is set.
     * <br> - support the CREATEPARENTS flag (from specification of method makeDir).
     */
    private NSEntry doCreateNamespaceFile(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            return new LogicalFileImpl(session, name, adaptor, flags);
        } else {
            return new FileImpl(session, name, adaptor, flags);
        }
    }
}
