package fr.in2p3.jsaga.impl.unimplemented;

import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.stream.*;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StreamFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class StreamFactoryImpl extends StreamFactory {
    protected Stream doCreateStream(Session session, URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    protected StreamService doCreateStreamService(Session session, URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    protected StreamService doCreateStreamService(Session session) throws NotImplementedException, IncorrectURLException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    protected Task<StreamFactory, Stream> doCreateStream(TaskMode mode, Session session, URL name) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    protected Task<StreamFactory, StreamService> doCreateStreamService(TaskMode mode, Session session, URL name) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    protected Task<StreamFactory, StreamService> doCreateStreamService(TaskMode mode, Session session) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }
}
