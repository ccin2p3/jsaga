package fr.in2p3.jsaga.impl.permissions;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import fr.in2p3.jsaga.impl.url.URLImpl;
import fr.in2p3.jsaga.sync.namespace.SyncNSEntry;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.permissions.Permission;
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
            PermissionAdaptor adaptor = (PermissionAdaptor) m_adaptor;
            if (isSupportedScope(adaptor, id)) {
                int _scope = getPermissionsScope(id);
                String _identifier = getPermissionsIdentifier(id);
                PermissionBytes _permissions = new PermissionBytes(permissions);

                // set OWNER permission
                if (_permissions.contains(Permission.OWNER)) {
                    switch (_scope) {
                        case PermissionAdaptor.SCOPE_USER:
                            adaptor.setOwner(_identifier);
                            break;
                        case PermissionAdaptor.SCOPE_GROUP:
                            adaptor.setGroup(_identifier);
                            break;
                        case PermissionAdaptor.SCOPE_ANY:
                            throw new BadParameterException("Setting * as OWNER is not allowed");
                    }
                    // remove flag OWNER
                    _permissions = new PermissionBytes(permissions - Permission.OWNER.getValue());
                }

                // set other permissions
                adaptor.permissionsAllow(m_url.getPath(), _scope, _identifier, _permissions);
            } else {
                throw new BadParameterException("Not supported for this protocol: "+ m_url.getScheme(), this);
            }
            // reset file attributes from cache
            ((URLImpl)m_url).setCache(null);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void permissionsDeny(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof PermissionAdaptor) {
            PermissionAdaptor adaptor = (PermissionAdaptor) m_adaptor;
            if (isSupportedScope(adaptor, id)) {
                int _scope = getPermissionsScope(id);
                String _identifier = getPermissionsIdentifier(id);
                PermissionBytes _permissions = new PermissionBytes(permissions);

                // set OWNER permission
                if (_permissions.contains(Permission.OWNER)) {
                    throw new BadParameterException("Unsetting OWNER permission is not allowed");
                }

                // set other permissions
                adaptor.permissionsDeny(m_url.getPath(), _scope, _identifier, _permissions);
            } else {
                throw new BadParameterException("Not supported for this protocol: "+ m_url.getScheme(), this);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        // reset file attributes from cache
        ((URLImpl)m_url).setCache(null);
    }

    public boolean permissionsCheck(String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        boolean checked = true;

        final String ctxUserID = null;  //todo: get UserID from context
        int _scope = getPermissionsScope(id);
        String _identifier = getPermissionsIdentifier(id);
        PermissionBytes _permissions = new PermissionBytes(permissions);

        // get cache
        FileAttributes attrs;
        try{attrs=this._getFileAttributes();} catch(IncorrectStateException e){throw new NoSuccessException(e);}

        // check OWNER permission
        if (_permissions.contains(Permission.OWNER)) {
            switch (_scope) {
                case PermissionAdaptor.SCOPE_USER:
                    if (attrs.getOwner() != null) {
                        checked = attrs.getOwner().equals(_identifier);
                    } else {
                        throw new BadParameterException("Not supported for this protocol: "+m_url.getScheme(), this);
                    }
                    break;
                case PermissionAdaptor.SCOPE_GROUP:
                    if (attrs.getGroup() != null) {
                        checked = attrs.getGroup().equals(_identifier);
                    } else {
                        throw new BadParameterException("Not supported for this protocol: "+m_url.getScheme(), this);
                    }
                    break;
                case PermissionAdaptor.SCOPE_ANY:
                    throw new BadParameterException("* can not be OWNER of an entry");
            }
            // remove flag OWNER
            _permissions = new PermissionBytes(permissions - Permission.OWNER.getValue());
        }

        //fixme: get other permissions from cache
        PermissionBytes perms = FileAttributes.PERMISSION_UNKNOWN;
        switch (_scope) {
            case PermissionAdaptor.SCOPE_USER:
                if (id==null ||
                    id==ctxUserID ||
                    _identifier.equals(attrs.getOwner()))
                {
                    perms = attrs.getUserPermission();
                }
                break;
            case PermissionAdaptor.SCOPE_GROUP:
                if (_identifier.equals(attrs.getGroup())) {
                    perms = attrs.getGroupPermission();
                }
                break;
            case PermissionAdaptor.SCOPE_ANY:
                perms = attrs.getAnyPermission();
                break;
        }

        // check other permissions
        if (perms != FileAttributes.PERMISSION_UNKNOWN) {
            checked &= perms.containsAll(_permissions.getValue());
        } else if (m_adaptor instanceof PermissionAdaptor) {
            PermissionAdaptor adaptor = (PermissionAdaptor) m_adaptor;
            if (isSupportedScope(adaptor, id)) {
                checked &= adaptor.permissionsCheck(m_url.getPath(), _scope, _identifier, _permissions);
            } else {
                throw new BadParameterException("Not supported for this protocol: "+ m_url.getScheme(), this);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        return checked;
    }

    private static boolean isSupportedScope(PermissionAdaptor adaptor, String id) {
        int scope = getPermissionsScope(id);
        for (int s : adaptor.getSupportedScopes()) {
            if (scope == s) {
                return true;
            }
        }
        return false;
    }
    private static int getPermissionsScope(String id) {
        if (id != null) {
            if (id.equals("*")) {
                return PermissionAdaptor.SCOPE_ANY;
            } else if (id.startsWith("group-")) {
                return PermissionAdaptor.SCOPE_GROUP;
            }
        }
        return PermissionAdaptor.SCOPE_USER;
    }
    private static String getPermissionsIdentifier(String id) {
        if (id != null) {
            if (id.startsWith("group-") || id.startsWith("user-")) {
                String realIdentifier = id.substring(id.indexOf('-')+1);
                if (realIdentifier.equals("null")) {
                    return null;
                }
            }
        }
        return id;
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

    public Task<NSEntry, Void> permissionsAllow(TaskMode mode, final String id, final int permissions) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractDataPermissionsImpl.this.permissionsAllow(id, permissions);
                return null;
            }
        };
    }

    public Task<NSEntry, Void> permissionsDeny(TaskMode mode, final String id, final int permissions) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                AbstractDataPermissionsImpl.this.permissionsDeny(id, permissions);
                return null;
            }
        };
    }

    public Task<NSEntry, Boolean> permissionsCheck(TaskMode mode, final String id, final int permissions) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,Boolean>(mode) {
            public Boolean invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractDataPermissionsImpl.this.permissionsCheck(id, permissions);
            }
        };
    }

    public Task<NSEntry, String> getOwner(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,String>(mode) {
            public String invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractDataPermissionsImpl.this.getOwner();
            }
        };
    }

    public Task<NSEntry, String> getGroup(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<NSEntry,String>(mode) {
            public String invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return AbstractDataPermissionsImpl.this.getGroup();
            }
        };
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
