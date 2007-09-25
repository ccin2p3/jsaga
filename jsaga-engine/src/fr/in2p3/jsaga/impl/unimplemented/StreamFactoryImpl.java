package fr.in2p3.jsaga.impl.unimplemented;

import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.stream.*;
import org.ogf.saga.task.RVTask;
import org.ogf.saga.task.TaskMode;

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
    protected Stream doCreateStream(Session session, URI name) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected StreamService doCreateStreamService(Session session, URI name) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected RVTask<Stream> doCreateStream(TaskMode mode, Session session, URI name) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected RVTask<StreamService> doCreateStreamService(TaskMode mode, Session session, URI name) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }
}
