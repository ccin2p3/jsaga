package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNamespaceEntryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNamespaceEntryImpl extends AbstractPermissionsImpl implements NamespaceEntry {
    protected final DataConnection m_connection;

    /** constructor */
    public AbstractNamespaceEntryImpl(Session session, URI uri, Flags flags, DataConnection connection) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, connection.getDataAdaptor());
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.CREATE.or(Flags.EXCL).or(Flags.CREATEPARENTS));
        m_connection = connection;
        m_connection.registerEntry(this);
    }

    /** constructor for deepCopy */
    protected AbstractNamespaceEntryImpl(AbstractNamespaceEntryImpl source) {
        super(source);
        m_connection = source.m_connection;
    }

    public URI getURI() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        return m_uri;
    }

    public URI getCWD() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        String path = m_uri.getPath();
        while(path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        int pos = path.lastIndexOf('/');
        String parentPath = (pos>-1 ? path.substring(0,pos+1) : "/");
        try {
            return new URI(new java.net.URI(
                    m_uri.getScheme(), m_uri.getUserInfo(), m_uri.getHost(), m_uri.getPort(), parentPath, m_uri.getQuery(), m_uri.getFragment()));
        } catch (URISyntaxException e) {
            throw new IncorrectState(e.getMessage(), this);
        }
    }

    public String getName() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        String[] names = m_uri.getPath().split("/");
        String name;
        if (names.length > 0) {
            name = names[names.length-1];
            if (name.equals("")) {
                name = null;
            }
        } else {
            name = null;
        }
        return name;
    }

    public boolean exists() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof DataReaderAdaptor) {
            return ((DataReaderAdaptor)m_adaptor).exists(m_uri.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public boolean isDirectory(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this._dereferenceEntry().isDirectory(effectiveFlags.remove(Flags.DEREFERENCE));
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof DataReaderAdaptor) {
            try {
                return ((DataReaderAdaptor)m_adaptor).isDirectory(m_uri.getPath());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Entry does not exist: "+m_uri, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public boolean isEntry(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this._dereferenceEntry().isEntry(effectiveFlags.remove(Flags.DEREFERENCE));
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof DataReaderAdaptor) {
            try {
                return ((DataReaderAdaptor)m_adaptor).isEntry(m_uri.getPath());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Entry does not exist: "+m_uri, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public boolean isLink(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this._dereferenceEntry().isLink(effectiveFlags.remove(Flags.DEREFERENCE));
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof LinkAdaptor) {
            try {
                return ((LinkAdaptor)m_adaptor).isLink(m_uri.getPath());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Link does not exist: "+m_uri, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public URI readLink() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LinkAdaptor) {
            String absolutePath;
            try {
                try {
                    absolutePath = ((LinkAdaptor)m_adaptor).readLink(m_uri.getPath());
                } catch (DoesNotExist doesNotExist) {
                    throw new IncorrectState("Link does not exist: "+m_uri, doesNotExist);
                }
            } catch (NotLink notLink) {
                throw new BadParameter("Not a link: "+m_uri, this);
            }
            return m_uri.resolve(absolutePath);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    /**
     * Create a link to the physical entry <code>m_uri</code>.<br>
     * Note: RECURSIVE flag support is not implemented.
     * @param link the path to the link to create (may be absolute or relative to <code>m_uri</code>).
     */
    public void link(URI link, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().link(link, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE).or(Flags.CREATEPARENTS).or(Flags.OVERWRITE));
        if (effectiveFlags.contains(Flags.RECURSIVE)) {
            throw new NotImplemented("Support of RECURSIVE flags with method link() is not implemented by the SAGA engine", this);
        }
        URI effectiveLink = this._getEffectiveURI(link);
        if (m_adaptor instanceof LinkAdaptor) {
            boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
            try {
                try {
                    ((LinkAdaptor)m_adaptor).link(m_uri.getPath(), effectiveLink.getPath(), overwrite);
                } catch (DoesNotExist doesNotExist) {
                    throw new IncorrectState("Entry does not exist: "+m_uri, doesNotExist);
                } catch (AlreadyExists alreadyExists) {
                    throw new AlreadyExists("Target entry already exists: "+effectiveLink, alreadyExists.getCause());
                }
            } catch(IncorrectState e) {
                if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                    // make parent directories
                    this._makeParentDirs();
                    // create link
                    try {
                        ((LinkAdaptor)m_adaptor).link(m_uri.getPath(), effectiveLink.getPath(), overwrite);
                    } catch (DoesNotExist doesNotExist) {
                        throw new IncorrectState("Entry does not exist: "+m_uri, doesNotExist);
                    } catch (AlreadyExists alreadyExists) {
                        throw new AlreadyExists("Target entry already exists: "+effectiveLink, alreadyExists.getCause());
                    }
                } else {
                    throw e;
                }
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme());
        }
    }

    public abstract void copyFrom(URI source, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess;

    public void move(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().move(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.CREATEPARENTS).or(Flags.OVERWRITE));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URI effectiveTarget = this._getEffectiveURI(target);
        if (m_adaptor instanceof DataRename
                && m_uri.getScheme().equals(effectiveTarget.getScheme())
                && (m_uri.getUserInfo()==null || m_uri.getUserInfo().equals(effectiveTarget.getUserInfo()))
                && (m_uri.getHost()==null || m_uri.getHost().equals(effectiveTarget.getHost()))
                && (m_uri.getPort()==effectiveTarget.getPort()))
        {
            try {
                ((DataRename)m_adaptor).rename(m_uri.getPath(), effectiveTarget.getPath(), overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("File does not exist: "+m_uri, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else {
            this.copy(effectiveTarget, flags);
            this.remove(flags);
        }
    }

    public void remove(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().remove(flags);
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof DataWriterAdaptor) {
            URI parent = this._getParentDirURI();
            String fileName = this.getName();
            try {
                ((DataWriterAdaptor)m_adaptor).removeFile(parent.getPath(), fileName);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("File does not exist: "+m_uri, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme());
        }
    }

    private boolean m_disconnected = false;
    public synchronized void close(float timeoutInSeconds) throws NotImplemented, IncorrectState, NoSuccess {
        synchronized(m_connection) {
            m_connection.unregisterEntry(this);
            if (! m_disconnected && ! m_connection.hasRegisteredEntries()) {
                m_adaptor.disconnect();
                m_disconnected = true;
            }
        }
    }

    public void finalize() throws Throwable {
        super.finalize();
        if (! m_disconnected) {
            m_adaptor.disconnect();
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    public abstract NamespaceDirectory openDir(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    public abstract NamespaceEntry openEntry(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    protected AbstractNamespaceDirectoryImpl _dereferenceDir()  throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return (AbstractNamespaceDirectoryImpl) this.openDir(this.readLink(), Flags.NONE);
        } catch (IncorrectURL e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (IncorrectSession e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        }
    }

    protected AbstractNamespaceEntryImpl _dereferenceEntry()  throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return (AbstractNamespaceEntryImpl) this.openEntry(this.readLink(), Flags.NONE);
        } catch (IncorrectURL e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (IncorrectSession e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        }
    }

    protected NamespaceDirectory _getParent(FlagsContainer flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return this.openDir(this._getParentDirURI(), flags.remove(Flags.NONE));
        } catch (IncorrectURL e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (IncorrectSession e) {
            throw new NoSuccess("Unexpected exception", e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        }
    }

    protected URI _resolveAbsoluteURI(URI absolutePath) throws IncorrectURL {
        if (absolutePath==null) {
            throw new IncorrectURL("URI must not be null");
        } else if (absolutePath.getScheme()!=null && !absolutePath.getScheme().equals(m_uri.getScheme())) {
            throw new IncorrectURL("You must not modify the scheme of the URI: "+m_uri.getScheme());
        } else if (absolutePath.getUserInfo()!=null && !absolutePath.getUserInfo().equals(m_uri.getUserInfo())) {
            throw new IncorrectURL("You must not modify the user part of the URI: "+m_uri.getUserInfo());
        } else if (absolutePath.getHost()!=null && !absolutePath.getHost().equals(m_uri.getHost())) {
            throw new IncorrectURL("You must not modify the host of the URI: "+m_uri.getHost());
        } else if (! absolutePath.getPath().startsWith("/")) {
            throw new IncorrectURL("URI must contain an absolute path: "+m_uri.getPath());
        }
        return m_uri.resolve(absolutePath);
    }

    protected URI _getEffectiveURI(URI target) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        if (target.getPath().endsWith("/")) {
            return target.resolve(this.getName());
        } else {
            return target;
        }
    }

    protected URI _getParentDirURI() {
        return (m_uri.getPath().endsWith("/") ? m_uri.resolve("..") : m_uri.resolve("."));
    }

    protected void _makeParentDirs() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        try {
            URI parent = this._getParentDirURI();
            Flags flags = Flags.CREATE.or(Flags.CREATEPARENTS);
            this.openDir(parent, flags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        }
    }
}
