package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
public class NSFactoryImpl extends AbstractAsyncNSFactoryImpl {
    public NSFactoryImpl(DataAdaptorFactory adaptorFactory) {
        super(adaptorFactory);
    }

    ////////////////////////////////////////// interface NSFactory //////////////////////////////////////////

    protected NSEntry doCreateNSEntry(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createNSEntry", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateNSEntrySync(session, name, flags);
        } else {
            try {
                return (NSEntry) this.getResult(super.doCreateNSEntry(TaskMode.ASYNC, session, name, flags), timeout);
            } catch (IncorrectStateException | SagaIOException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    protected NSDirectory doCreateNSDirectory(Session session, URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("createNSDirectory", name);
        if (timeout == SagaObject.WAIT_FOREVER) {
            return super.doCreateNSDirectorySync(session, name, flags);
        } else {
            try {
                return (NSDirectory) this.getResult(super.doCreateNSDirectory(TaskMode.ASYNC, session, name, flags), timeout);
            } catch (IncorrectStateException | SagaIOException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName, URL url) throws NoSuccessException {
        return AbstractSagaObjectImpl.getTimeout(NSFactory.class, methodName, url.getScheme());
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
