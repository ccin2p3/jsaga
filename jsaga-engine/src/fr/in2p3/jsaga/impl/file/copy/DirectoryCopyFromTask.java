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
* File:   DirectoryCopyFromTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 févr. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DirectoryCopyFromTask<T extends SagaObject,E> extends AbstractCopyFromTask<T,E> {
    private AbstractSyncDirectoryImpl m_targetDir;

    /** constructor */
    public DirectoryCopyFromTask(TaskMode mode, Session session, AbstractSyncDirectoryImpl targetDir, URL target, int flags) throws NotImplementedException {
        super(mode, session, target, flags);
        m_targetDir = targetDir;
    }

    public void doCopyFrom(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        m_targetDir.copyFromSync(source, flags);
    }
}
