package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

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

    protected LogicalFile doCreateLogicalFile(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            return new LogicalFileImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameter("Not a logical file URL: "+name);
        }
    }

    protected LogicalDirectory doCreateLogicalDirectory(Session session, URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            return new LogicalDirectoryImpl(session, name, adaptor, flags);
        } else {
            throw new BadParameter("Not a logical directory URL: "+name);
        }
    }

    protected Task<LogicalFile> doCreateLogicalFile(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected Task<LogicalDirectory> doCreateLogicalDirectory(TaskMode mode, Session session, URL name, int flags) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }
}
