package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.impl.file.AbstractSyncFileImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileCopyFromTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileCopyFromTask<T,E> extends AbstractCopyFromTask<T,E> {
    private AbstractSyncFileImpl m_targetFile;

    /** constructor */
    public FileCopyFromTask(TaskMode mode, Session session, AbstractSyncFileImpl targetFile, URL source, int flags) throws NotImplementedException {
        super(mode, session, source, flags);
        m_targetFile = targetFile;
    }

    public void doCopyFrom(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        m_targetFile._copyFromAndMonitor(source, flags, this);
    }
}
