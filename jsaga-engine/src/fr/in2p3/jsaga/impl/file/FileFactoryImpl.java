package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.RVTask;
import org.ogf.saga.task.TaskMode;

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

    protected File doCreateFile(Session session, URI name, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            throw new BadParameter("Not a physical file URI: "+name);
        } else {
            return new FileImpl(session, name, adaptor, flags);
        }
    }

    protected Directory doCreateDirectory(Session session, URI name, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        DataAdaptor adaptor = m_adaptorFactory.getDataAdaptor(name, session);
        if (adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter) {
            throw new BadParameter("Not a physical directory URI: "+name);
        } else {
            return new DirectoryImpl(session, name, adaptor, flags);
        }
    }

    protected RVTask<File> doCreateFile(TaskMode mode, Session session, URI name, Flags... flags) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected RVTask<Directory> doCreateDirectory(TaskMode mode, Session session, URI name, Flags... flags) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }
}
