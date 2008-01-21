package fr.in2p3.jsaga.impl.unimplemented;

import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.stream.*;
import org.ogf.saga.task.Task;
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
    protected Stream doCreateStream(Session session, URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected StreamService doCreateStreamService(Session session, URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected StreamService doCreateStreamService(Session session) throws NotImplemented, IncorrectURL, BadParameter, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected Task<Stream> doCreateStream(TaskMode mode, Session session, URL name) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected Task<StreamService> doCreateStreamService(TaskMode mode, Session session, URL name) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected Task<StreamService> doCreateStreamService(TaskMode mode, Session session) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }
}
