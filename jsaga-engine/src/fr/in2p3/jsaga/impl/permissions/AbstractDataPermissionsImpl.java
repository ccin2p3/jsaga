package fr.in2p3.jsaga.impl.permissions;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.URL;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

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
public abstract class AbstractDataPermissionsImpl extends AbstractAttributesImpl implements Permissions, Attributes {
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

    public void permissionsAllow(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            ((PermissionAdaptor)m_adaptor).permissionsAllow(m_url.getPath(), id, effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void permissionsDeny(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            ((PermissionAdaptor)m_adaptor).permissionsDeny(m_url.getPath(), id, effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public boolean permissionsCheck(String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            return ((PermissionAdaptor)m_adaptor).permissionsCheck(m_url.getPath(), id, effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String getOwner() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            return ((PermissionAdaptor)m_adaptor).getOwner(m_url.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String getGroup() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            return ((PermissionAdaptor)m_adaptor).getGroup(m_url.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
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
