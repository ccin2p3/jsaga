package fr.in2p3.jsaga.impl.permissions;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import fr.in2p3.jsaga.impl.url.URLImpl;
import fr.in2p3.jsaga.sync.namespace.SyncNSEntry;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.permissions.Permissions;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
public abstract class AbstractDataPermissionsImpl extends AbstractSagaObjectImpl implements Permissions<NSEntry>, SyncNSEntry {
    protected URL m_url;
    protected DataAdaptor m_adaptor;

    /** constructor */
    public AbstractDataPermissionsImpl(Session session, URL url, DataAdaptor adaptor) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session);

        // set URL
        m_url = url;
        String scheme = adaptor.getType();
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

    public void permissionsAllow(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            ((PermissionAdaptor)m_adaptor).permissionsAllow(
                    m_url.getPath(),
                    id,
                    effectivePermissions);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void permissionsDeny(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionBytes effectivePermissions = new PermissionBytes(permissions);
            ((PermissionAdaptor)m_adaptor).permissionsDeny(
                    m_url.getPath(),
                    id,
                    effectivePermissions);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public boolean permissionsCheck(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            FileAttributes attrs = this._getFileAttributes();
            PermissionBytes perm = attrs.getPermission();
            if (perm != null) {
                String owner = attrs.getOwner();
                boolean checkOwnerPerm = (owner!=null && owner.equals(id));
                boolean checkAllPerm = (owner==null && (id==null || id.equals("*")));
                if (checkOwnerPerm || checkAllPerm) {
                    return perm.containsAll(permissions);
                }
            }
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
        throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
    }

    public String getOwner() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            FileAttributes attrs = this._getFileAttributes();
            String owner = attrs.getOwner();
            if (owner != null) {
                return owner;
            }
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
        throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
    }

    public String getGroup() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            FileAttributes attrs = this._getFileAttributes();
            String group = attrs.getGroup();
            if (group != null) {
                return group;
            }
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
        throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
    }

    //////////////////////////////////////////// Asynchronous ////////////////////////////////////////////

    public Task<NSEntry, Void> permissionsAllow(TaskMode mode, String id, int permissions) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, (NSEntry) this,
                "permissionsAllow",
                new Class[]{String.class, int.class},
                new Object[]{id, permissions});
    }

    public Task<NSEntry, Void> permissionsDeny(TaskMode mode, String id, int permissions) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Void>().create(
                mode, m_session, (NSEntry) this,
                "permissionsDeny",
                new Class[]{String.class, int.class},
                new Object[]{id, permissions});
    }

    public Task<NSEntry, Boolean> permissionsCheck(TaskMode mode, String id, int permissions) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,Boolean>().create(
                mode, m_session, (NSEntry) this,
                "permissionsCheck",
                new Class[]{String.class, int.class},
                new Object[]{id, permissions});
    }

    public Task<NSEntry, String> getOwner(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,String>().create(
                mode, m_session, (NSEntry) this,
                "getOwner",
                new Class[]{},
                new Object[]{});
    }

    public Task<NSEntry, String> getGroup(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<NSEntry,String>().create(
                mode, m_session, (NSEntry) this,
                "getGroup",
                new Class[]{},
                new Object[]{});
    }

    protected FileAttributes _getFileAttributes() throws NotImplementedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        FileAttributes attrs;
        if ( ((URLImpl)m_url).hasCache() ) {
            // get file attributes from cache
            attrs = ((URLImpl)m_url).getCache();
        } else if (m_adaptor instanceof DataReaderAdaptor) {
            // query file attributes
            try {
                attrs = ((DataReaderAdaptor)m_adaptor).getAttributes(
                        m_url.getPath(),
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Entry does not exist: "+m_url, doesNotExist);
            }

            // set file attributes to cache
            ((URLImpl)m_url).setCache(attrs);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+m_url.getScheme(), this);
        }
        return attrs;
    }
}
