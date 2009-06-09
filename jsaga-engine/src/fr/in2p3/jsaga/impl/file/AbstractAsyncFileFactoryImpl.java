package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractAsyncFileFactoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncFileFactoryImpl extends AbstractSyncFileFactoryImpl {
    public AbstractAsyncFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    protected Task<FileFactory, File> doCreateFile(TaskMode mode, final Session session, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<FileFactory,File>(mode) {
            public File invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileFactoryImpl.super.doCreateFileSync(session, name, flags);
            }
        };
    }

    protected Task<FileFactory, FileInputStream> doCreateFileInputStream(TaskMode mode, final Session session, final URL name) throws NotImplementedException {
        return new AbstractThreadedTask<FileFactory,FileInputStream>(mode) {
            public FileInputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileFactoryImpl.super.doCreateFileInputStreamSync(session, name);
            }
        };
    }

    protected Task<FileFactory, FileOutputStream> doCreateFileOutputStream(TaskMode mode, final Session session, final URL name, final boolean append) throws NotImplementedException {
        return new AbstractThreadedTask<FileFactory,FileOutputStream>(mode) {
            public FileOutputStream invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileFactoryImpl.super.doCreateFileOutputStreamSync(session, name, append);
            }
        };
    }

    protected Task<FileFactory, Directory> doCreateDirectory(TaskMode mode, final Session session, final URL name, final int flags) throws NotImplementedException {
        return new AbstractThreadedTask<FileFactory,Directory>(mode) {
            public Directory invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncFileFactoryImpl.super.doCreateDirectorySync(session, name, flags);
            }
        };
    }
}
