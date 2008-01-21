package fr.in2p3.jsaga.impl.permissions;

import fr.in2p3.jsaga.impl.task.AbstractTaskImplWithAsyncAttributes;
import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobPermissionsImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractJobPermissionsImpl extends AbstractTaskImplWithAsyncAttributes<Object> implements Permissions, AsyncAttributes {
    /** constructor */
    public AbstractJobPermissionsImpl(Session session, SagaObject object) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(session, object);
    }

    public void permissionsAllow(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public void permissionsDeny(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public boolean permissionsCheck(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public String getOwner() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public String getGroup() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public Task permissionsAllow(TaskMode mode, String id, int permissions) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public Task permissionsDeny(TaskMode mode, String id, int permissions) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public Task<Boolean> permissionsCheck(TaskMode mode, String id, int permissions) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public Task<String> getOwner(TaskMode mode) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public Task<String> getGroup(TaskMode mode) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }
}
