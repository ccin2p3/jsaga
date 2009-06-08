package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.impl.file.AbstractSyncDirectoryImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DirectoryCopyTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 févr. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DirectoryCopyTask<T extends SagaObject,E> extends AbstractCopyTask<T,E> {
    private AbstractSyncDirectoryImpl m_sourceDir;

    /** constructor */
    public DirectoryCopyTask(TaskMode mode, Session session, AbstractSyncDirectoryImpl sourceDir, URL target, int flags) throws NotImplementedException {
        super(mode, session, target, flags);
        m_sourceDir = sourceDir;
    }

    public void doCopy(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        m_sourceDir._copyAndMonitor(target, flags, this);
    }
}
