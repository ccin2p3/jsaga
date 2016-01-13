package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
public class LogicalFileFactoryImpl extends AbstractAsyncLogicalFileFactoryImpl {
    public LogicalFileFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    ///////////////////////////////////// interface LogicalFileFactory /////////////////////////////////////

    protected LogicalFile doCreateLogicalFile(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createLogicalFile", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateLogicalFileSync(session, name, flags);
        } else {
            try {
                return (LogicalFile) this.getResult(super.doCreateLogicalFile(TaskMode.ASYNC, session, name, flags), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    protected LogicalDirectory doCreateLogicalDirectory(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createLogicalDirectory", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateLogicalDirectorySync(session, name, flags);
        } else {
            try {
                return (LogicalDirectory) this.getResult(super.doCreateLogicalDirectory(TaskMode.ASYNC, session, name, flags), timeout);
            }
            catch (IncorrectStateException e) {throw new NoSuccessException(e);}
            catch (SagaIOException e) {throw new NoSuccessException(e);}
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName, URL url) throws NoSuccessException {
        return AbstractSagaObjectImpl.getTimeout(LogicalFileFactory.class, methodName, url.getScheme());
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
