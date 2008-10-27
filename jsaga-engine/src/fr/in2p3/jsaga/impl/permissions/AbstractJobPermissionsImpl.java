package fr.in2p3.jsaga.impl.permissions;

import fr.in2p3.jsaga.impl.task.AbstractTaskImplWithAsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.job.Job;
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
public abstract class AbstractJobPermissionsImpl extends AbstractTaskImplWithAsyncAttributes<Void,Void,Job> implements Permissions<Job>, Job {
    /** constructor */
    public AbstractJobPermissionsImpl(Session session, boolean create) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(session, create);
    }

    public void permissionsAllow(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public void permissionsDeny(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public boolean permissionsCheck(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public String getOwner() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public String getGroup() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public Task<Job, Void> permissionsAllow(TaskMode mode, String id, int permissions) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public Task<Job, Void> permissionsDeny(TaskMode mode, String id, int permissions) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public Task<Job, Boolean> permissionsCheck(TaskMode mode, String id, int permissions) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public Task<Job, String> getOwner(TaskMode mode) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }

    public Task<Job, String> getGroup(TaskMode mode) throws NotImplementedException {
        throw new NotImplementedException("Not implemented by the SAGA engine", this);
    }
}
