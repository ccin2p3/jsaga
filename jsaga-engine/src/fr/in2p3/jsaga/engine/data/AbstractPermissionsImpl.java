package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptor;
import fr.in2p3.jsaga.engine.base.AbstractSagaBaseImpl;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.*;

import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractPermissionsImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractPermissionsImpl extends AbstractSagaBaseImpl implements Permissions {
    protected URI m_uri;
    protected DataAdaptor m_adaptor;

    /** constructor */
    public AbstractPermissionsImpl(Session session, URI uri, DataAdaptor adaptor) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        super(session);

        // set URI
        String scheme = adaptor.getScheme();
        if (uri.getScheme().equals(scheme)) {
            m_uri = uri;
        } else {
            try {
                m_uri = new URI(new java.net.URI(
                        scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()
                ));
            } catch (URISyntaxException e) {
                throw new NoSuccess(e);
            }
        }

        // set adaptor
        m_adaptor = adaptor;
    }

    /** constructor for deepCopy */
    protected AbstractPermissionsImpl(AbstractPermissionsImpl source) {
        super(source);
        m_uri = source.m_uri;
        m_adaptor = source.m_adaptor;
    }

    public void enablePermissions(String id, Permission permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            Permission effectivePermissions = _getEffectivePermissions(permissions, Permission.UNKNOWN);
            ((PermissionAdaptor)m_adaptor).enablePermissions(m_uri.getPath(), id, effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void disablePermissions(String id, Permission permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            Permission effectivePermissions = _getEffectivePermissions(permissions, Permission.UNKNOWN);
            ((PermissionAdaptor)m_adaptor).disablePermissions(m_uri.getPath(), id, effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public boolean areEnabled(String id, Permission permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            Permission effectivePermissions = _getEffectivePermissions(permissions, Permission.UNKNOWN);
            return ((PermissionAdaptor)m_adaptor).areEnabled(m_uri.getPath(), id, effectivePermissions);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public String getOwner() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            return ((PermissionAdaptor)m_adaptor).getOwner(m_uri.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public String getGroup() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof PermissionAdaptor) {
            return ((PermissionAdaptor)m_adaptor).getGroup(m_uri.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public Task enablePermissions(TaskMode mode, String id, Permission permissions) {
        throw new RuntimeException(new NotImplemented("Not implemented by the SAGA engine", this));
    }

    public Task disablePermissions(TaskMode mode, String id, Permission permissions) {
        throw new RuntimeException(new NotImplemented("Not implemented by the SAGA engine", this));
    }

    public TaskReturnValueBoolean areEnabled(TaskMode mode, String id, Permission permissions) {
        throw new RuntimeException(new NotImplemented("Not implemented by the SAGA engine", this));
    }

    public TaskReturnValueString getOwner(TaskMode mode) {
        throw new RuntimeException(new NotImplemented("Not implemented by the SAGA engine", this));
    }

    public TaskReturnValueString getGroup(TaskMode mode) {
        throw new RuntimeException(new NotImplemented("Not implemented by the SAGA engine", this));
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    protected Permission _getEffectivePermissions(Permission permissions, Permission defaultPermissions) {
        return (permissions!=null ? permissions : defaultPermissions);
    }
}
