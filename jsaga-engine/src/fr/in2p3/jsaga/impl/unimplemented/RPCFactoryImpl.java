package fr.in2p3.jsaga.impl.unimplemented;

import org.ogf.saga.url.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.rpc.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
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

    protected Parameter doCreateParameter(IOMode mode) throws NotImplemented, BadParameter, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected Parameter doCreateParameter(int sz, IOMode mode) throws NotImplemented, BadParameter, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected RPC doCreateRPC(Session session, URL funcname) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }

    protected Task<RPC> doCreateRPC(TaskMode mode, Session session, URL funcname) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine");
    }
}
