package fr.in2p3.jsaga.impl.file.copy;

import fr.in2p3.jsaga.impl.file.AbstractSyncFileImpl;
import org.apache.log4j.Logger;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileCopyTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileCopyTask<T extends SagaObject,E> extends AbstractCopyTask<T,E> {
    private static Logger s_logger = Logger.getLogger(FileCopyTask.class);
    
    private AbstractSyncFileImpl m_sourceFile;

    /** constructor */
    public FileCopyTask(TaskMode mode, Session session, AbstractSyncFileImpl sourceFile, URL target, int flags) throws NotImplementedException {
        super(mode, session, target, flags);
        m_sourceFile = sourceFile;
    }

    public void doCopy(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        try {
            m_sourceFile._copyAndMonitor(target, flags, this);
        } catch (NullPointerException e) {
            // workaround to a bug that is still not understood
            s_logger.info(FileCopyTask.class.getName()+".m_sourceFile is null");
        }
    }
}
