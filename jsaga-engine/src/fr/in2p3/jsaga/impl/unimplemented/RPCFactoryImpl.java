package fr.in2p3.jsaga.impl.unimplemented;

import org.ogf.saga.error.*;
import org.ogf.saga.rpc.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
    protected Parameter doCreateParameter(Object data, IOMode mode) throws BadParameterException, NoSuccessException, NotImplementedException {
        throw new BadParameterException("Not implemented by the SAGA engine");
    }

    protected Parameter doCreateParameter(IOMode mode) throws NotImplementedException, BadParameterException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    protected RPC doCreateRPC(Session session, URL funcname) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }

    protected Task<RPCFactory, RPC> doCreateRPC(TaskMode mode, Session session, URL funcname) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine");
    }
}
