package fr.in2p3.jsaga.impl.unimplemented;

import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.rpc.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.RVTask;
import org.ogf.saga.task.TaskMode;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   RPCFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class RPCFactoryImpl extends RPCFactory {
    protected Parameter doCreateParameter(byte[] data, IOMode mode) throws BadParameter {
        throw new BadParameter("Not implemented by the SAGA engine");
    }

    protected RPC doCreateRPC(Session session, URI funcname) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected RVTask<RPC> doCreateRPC(TaskMode mode, Session session, URI funcname) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }
}
