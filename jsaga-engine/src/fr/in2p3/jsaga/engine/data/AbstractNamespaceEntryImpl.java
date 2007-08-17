package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.readwrite.*;
import fr.in2p3.jsaga.adaptor.data.write.*;
import fr.in2p3.jsaga.engine.base.BufferImpl;
import fr.in2p3.jsaga.engine.base.BufferImplApplicationManaged;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.lang.Exception;
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
    /** constructor */
    public AbstractNamespaceEntryImpl(Session session, URI uri, Flags flags, DataAdaptor adaptor) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, adaptor);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, PhysicalEntryFlags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.CREATE.or(Flags.EXCL).or(Flags.CREATEPARENTS).or(Flags.OVERWRITE));
        if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            this._makeParentDirs();
        }
        if (effectiveFlags.contains(Flags.CREATE)) {
            if (!(m_adaptor instanceof FileWriter) && !(m_adaptor instanceof LogicalWriter)) {
                throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme());
            }
            if (effectiveFlags.contains(Flags.EXCL) && this.exists()) {
                throw new AlreadyExists("Entry already exists: "+m_uri);
            }
        }
    }

    /** constructor for AbstractNamespaceDirectoryImpl */
    protected AbstractNamespaceEntryImpl(Session session, URI uri, DataAdaptor adaptor) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, adaptor);
    }

    /** constructor for deepCopy */
    protected AbstractNamespaceEntryImpl(AbstractNamespaceEntryImpl source) {
        super(source);
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
        String parentPath = (pos>-1 ? path.substring(0,pos) : "/");
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
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof DataReaderAdaptor) {
            URI effectiveSource = _getEffectiveURI(effectiveFlags);
            return ((DataReaderAdaptor)m_adaptor).isDirectory(effectiveSource.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public boolean isEntry(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof DataReaderAdaptor) {
            URI effectiveSource = _getEffectiveURI(effectiveFlags);
            return ((DataReaderAdaptor)m_adaptor).isEntry(effectiveSource.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public boolean isLink(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof LinkAdaptor) {
            URI effectiveSource = _getEffectiveURI(effectiveFlags);
            return ((LinkAdaptor)m_adaptor).isLink(effectiveSource.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public URI readLink() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LinkAdaptor) {
            String absolutePath;
            try {
                absolutePath = ((LinkAdaptor)m_adaptor).readLink(m_uri.getPath());
            } catch (NotLink notLink) {
                throw new BadParameter("Not a link: "+m_uri, this);
            }
            return m_uri.resolve(absolutePath);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void copy(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.CREATEPARENTS.or(Flags.OVERWRITE).or(Flags.DEREFERENCE));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URI effectiveTarget = this._getEffectiveTarget(target);
        Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(target.getScheme());
        boolean isLogical = descriptor.hasLogical() && descriptor.getLogical();
        if (m_adaptor instanceof DataCopyDelegated && m_uri.getScheme().equals(effectiveTarget.getScheme())) {
            ((DataCopyDelegated)m_adaptor).requestTransfer(
                    m_uri,
                    effectiveTarget,
                    overwrite);
        } else if (m_adaptor instanceof DataCopy && m_uri.getScheme().equals(effectiveTarget.getScheme())) {
            ((DataCopy)m_adaptor).copy(
                    m_uri.getPath(),
                    effectiveTarget.getHost(), effectiveTarget.getPort(), effectiveTarget.getPath(),
                    overwrite);
        } else if (m_adaptor instanceof FileReader) {
            FileReaderStream in = ((FileReader)m_adaptor).openFileReaderStream(m_uri.getPath());
            if (!isLogical) {
                Flags targetFlags = (overwrite
                        ? flags.or(PhysicalEntryFlags.WRITE).or(Flags.CREATE)
                        : flags.or(PhysicalEntryFlags.WRITE).or(Flags.CREATE).or(Flags.EXCL));
                File out = (File) this._createEntry(effectiveTarget, targetFlags);
                try {
                    byte[] data = new byte[1024];
                    BufferImpl buffer = new BufferImplApplicationManaged(data);
                    int readlen,writelen;
                    while ((readlen=in.read(data,data.length)) > 0) {
                        for (int total=0; total<readlen; total+=writelen) {
                            if (total > 0) {
                                int readlenRemaining = readlen-total;
                                byte[] dataBis = new byte[readlenRemaining];
                                System.arraycopy(data, total, dataBis, 0, readlenRemaining);
                                BufferImpl bufferBis = new BufferImplApplicationManaged(dataBis);
                                writelen = out.write(bufferBis, readlenRemaining);
                            } else {
                                writelen = out.write(buffer, readlen);
                            }
                        }
                    }
                } finally {
                    in.close();
                }
            } else {
                throw new BadParameter("Maybe what you want to do is to add to the target logical file this location: "+m_uri.toString());
            }
        } else if (m_adaptor instanceof LogicalReader) {
            String[] locations = ((LogicalReader)m_adaptor).listLocations(m_uri.getPath());
            if (isLogical) {
                Flags targetFlags = (overwrite
                        ? flags.or(PhysicalEntryFlags.WRITE).or(Flags.CREATE)
                        : flags.or(PhysicalEntryFlags.WRITE).or(Flags.CREATE).or(Flags.EXCL));
                LogicalFileImpl targetLogical = (LogicalFileImpl) this._createEntry(effectiveTarget, targetFlags);
                if (overwrite) {
                    targetLogical._removeAllLocations();
                }
                targetLogical._addAllLocations(locations);
            } else {
                if (locations!=null && locations.length>0) {
                    NamespaceEntry sourcePhysical;
                    try {
                        sourcePhysical = NamespaceFactory.createNamespaceEntry(m_session, new URI(locations[0]), flags);
                    } catch (Exception e) {
                        throw new NoSuccess(e);
                    }
                    sourcePhysical.copy(effectiveTarget, flags);
                }
            }
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
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.CREATEPARENTS).or(Flags.OVERWRITE).or(Flags.DEREFERENCE));
        if (effectiveFlags.contains(Flags.RECURSIVE)) {
            throw new NotImplemented("Support of RECURSIVE flags with method link() is not implemented by the SAGA engine", this);
        }
        URI effectiveLink = this._getEffectiveTarget(link);
        if (m_adaptor instanceof LinkAdaptor) {
            URI effectiveSource = _getEffectiveURI(effectiveFlags);
            boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
            try {
                ((LinkAdaptor)m_adaptor).link(effectiveSource.getPath(), effectiveLink.getPath(), overwrite);
            } catch(IncorrectState e) {
                if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                    // make parent directories
                    this._makeParentDirs();
                    // create link
                    ((LinkAdaptor)m_adaptor).link(effectiveSource.getPath(), effectiveLink.getPath(), overwrite);
                } else {
                    throw e;
                }
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme());
        }
    }

    public void move(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.CREATEPARENTS.or(Flags.OVERWRITE).or(Flags.DEREFERENCE));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URI effectiveTarget = this._getEffectiveTarget(target);
        if (m_adaptor instanceof DataMove && m_uri.getScheme().equals(effectiveTarget.getScheme())) {
            ((DataMove)m_adaptor).move(
                    m_uri.getPath(),
                    effectiveTarget.getHost(), effectiveTarget.getPort(), effectiveTarget.getPath(),
                    overwrite);
        } else {
            this.copy(effectiveTarget, flags);
            this.remove(flags);
        }
    }

    public void remove(Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof DataWriterAdaptor) {
            URI effectiveSource = _getEffectiveURI(effectiveFlags);
            ((DataWriterAdaptor)m_adaptor).removeFile(effectiveSource.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme());
        }
    }

    public void close(float timeoutInSeconds) throws NotImplemented, IncorrectState, NoSuccess {
        m_adaptor.disconnect();
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    protected URI _getEffectiveURI(FlagsContainer effectiveFlags) throws Timeout, PermissionDenied, IncorrectState, NoSuccess, BadParameter, AuthorizationFailed, NotImplemented, AuthenticationFailed {
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this.readLink();
        } else {
            return m_uri;
        }
    }

    protected URI _getEffectiveTarget(URI target) throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        if (target.getPath().endsWith("/")) {
            return target.resolve(this.getName());
        } else {
            return target;
        }
    }

    protected URI _getParentDirURI() {
        return (m_uri.getPath().endsWith("/") ? m_uri.resolve("..") : m_uri.resolve("."));
    }

    protected abstract NamespaceDirectory _openParentDir(Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    protected void _makeParentDirs() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        try {
            Flags flags = Flags.CREATE.or(Flags.CREATEPARENTS);
            _openParentDir(flags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        }
    }

    protected NamespaceEntry _createEntry(URI name, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        try {
            return NamespaceFactory.createNamespaceEntry(m_session, name, flags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        }
    }
}
