package fr.in2p3.jsaga.impl.permissions;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.SagaObject;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractDataPermissionsImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractDataPermissionsImpl extends AbstractSagaObjectImpl implements Permissions {
    protected URL m_url;
    protected DataAdaptor m_adaptor;

    /** constructor */
    public AbstractDataPermissionsImpl(Session session, URL url, DataAdaptor adaptor) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session);

        // set URL
        m_url = url;
        String scheme = adaptor.getSchemeAliases()[0];
        if (! url.getScheme().equals(scheme)) {
            m_url.setScheme(scheme);
        }

        // set adaptor
        m_adaptor = adaptor;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractDataPermissionsImpl clone = (AbstractDataPermissionsImpl) super.clone();
        clone.m_url = m_url;
        clone.m_adaptor = m_adaptor;
        return clone;
    }

    //////////////////////////////////////////// Synchronous ////////////////////////////////////////////

    public void permissionsAllow(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            ((PermissionAdaptor)m_adaptor).permissionsAllow(
                    JSagaURL.decode(m_url.getPath()),
                    id,
                    effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void permissionsDeny(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            ((PermissionAdaptor)m_adaptor).permissionsDeny(
                    JSagaURL.decode(m_url.getPath()),
                    id,
                    effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public boolean permissionsCheck(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_url instanceof JSagaURL) {
            PermissionBytes perm = ((JSagaURL)m_url).getAttributes().getPermission();
            if (perm != null) {
                String owner = ((JSagaURL)m_url).getAttributes().getOwner();
                boolean checkOwnerPerm = (owner!=null && owner.equals(id));
                boolean checkAllPerm = (owner==null && (id==null || id.equals("*")));
                if (checkOwnerPerm || checkAllPerm) {
                    return perm.containsAll(permissions);
                }
            }
        }
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            return ((PermissionAdaptor)m_adaptor).permissionsCheck(
                    JSagaURL.decode(m_url.getPath()),
                    id,
                    effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String getOwner() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_url instanceof JSagaURL) {
            String owner = ((JSagaURL)m_url).getAttributes().getOwner();
            if (owner != null) {
                return owner;
            }
        }
        if (m_adaptor instanceof PermissionAdaptor) {
            return ((PermissionAdaptor)m_adaptor).getOwner(
                    JSagaURL.decode(m_url.getPath()));
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String getGroup() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_url instanceof JSagaURL) {
            String group = ((JSagaURL)m_url).getAttributes().getGroup();
            if (group != null) {
                return group;
            }
        }
        if (m_adaptor instanceof PermissionAdaptor) {
            return ((PermissionAdaptor)m_adaptor).getGroup(
                    JSagaURL.decode(m_url.getPath()));
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    //////////////////////////////////////////// Asynchronous ////////////////////////////////////////////

    public Task permissionsAllow(TaskMode mode, String id, int permissions) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractDataPermissionsImpl.class.getMethod("permissionsAllow", new Class[]{String.class, int.class}),
                    new Object[]{id, permissions}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task permissionsDeny(TaskMode mode, String id, int permissions) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractDataPermissionsImpl.class.getMethod("permissionsDeny", new Class[]{String.class, int.class}),
                    new Object[]{id, permissions}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> permissionsCheck(TaskMode mode, String id, int permissions) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractDataPermissionsImpl.class.getMethod("permissionsCheck", new Class[]{String.class, int.class}),
                    new Object[]{id, permissions}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String> getOwner(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractDataPermissionsImpl.class.getMethod("getOwner", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String> getGroup(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractDataPermissionsImpl.class.getMethod("getGroup", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }
}
