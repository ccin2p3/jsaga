package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
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
* File:   FileFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileFactoryImpl extends AbstractAsyncFileFactoryImpl {
    public FileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    ///////////////////////////////////////// interface FileFactory /////////////////////////////////////////

    protected File doCreateFile(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createFile", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateFileSync(session, name, flags);
        } else {
            try {
                return (File) this.getResult(createFile(TaskMode.ASYNC, session, name, flags), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    protected FileInputStream doCreateFileInputStream(Session session, URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createFileInputStream", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateFileInputStreamSync(session, name);
        } else {
            try {
                return (FileInputStream) this.getResult(createFileInputStream(TaskMode.ASYNC, session, name), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    protected FileOutputStream doCreateFileOutputStream(Session session, URL name, boolean append) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createFileOutputStream", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateFileOutputStreamSync(session, name, append);
        } else {
            try {
                return (FileOutputStream) this.getResult(createFileOutputStream(TaskMode.ASYNC, session, name, append), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    protected Directory doCreateDirectory(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createDirectory", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateDirectorySync(session, name, flags);
        } else {
            try {
                return (Directory) this.getResult(createDirectory(TaskMode.ASYNC, session, name, flags), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName, URL url) throws NoSuccessException {
        return AbstractSagaObjectImpl.getTimeout(FileFactory.class, methodName, url.getScheme());
    }

    private Object getResult(Task task, float timeout)
            throws NotImplementedException, IncorrectURLException,
            AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException,
            TimeoutException, NoSuccessException, SagaIOException
    {
        return AbstractSagaObjectImpl.getResult(task, timeout);
    }
}
