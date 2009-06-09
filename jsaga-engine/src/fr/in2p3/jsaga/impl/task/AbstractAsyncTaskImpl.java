package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractAsyncTaskImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   4 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncTaskImpl<A extends Attributes> extends AbstractTaskImplWithAsyncAttributes<Void,Void,A> {
    /** constructor */
    protected AbstractAsyncTaskImpl(Session session, boolean create) throws NotImplementedException {
        super(session, create);
    }

    public Task<Task,Void> run(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Task,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncTaskImpl.super.run();
                return null;
            }
        };
    }

    public Task<Task,Void> cancel(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Task,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractAsyncTaskImpl.super.cancel();
                return null;
            }
        };
    }

    public Task<Task,State> getState(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<Task,State>(mode) {
            public State invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractAsyncTaskImpl.super.getState();
            }
        };
    }
}
