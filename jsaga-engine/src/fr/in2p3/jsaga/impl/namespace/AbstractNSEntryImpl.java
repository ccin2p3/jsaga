package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterTimes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.helpers.URLFactory;
import org.ogf.saga.*;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.util.Date;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSEntryImpl extends AbstractAsyncNSEntryImpl implements NSEntry {
    private boolean m_disconnectable;

    /** constructor for factory */
    public AbstractNSEntryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor);
        this.init(flags);
        m_disconnectable = true;
    }

    /** constructor for NSDirectory.open() */
    public AbstractNSEntryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(dir.m_session, _resolveRelativeUrl(dir.m_url, relativeUrl), dir.m_adaptor);
        this.init(flags);
        m_disconnectable = false;
    }
    protected static URL _resolveRelativeUrl(URL baseUrl, URL relativeUrl) throws NotImplemented, IncorrectURL, BadParameter, NoSuccess {
        if (relativeUrl==null) {
            throw new IncorrectURL("URL must not be null");
        } else if (relativeUrl.getScheme()!=null && !relativeUrl.getScheme().equals(baseUrl.getScheme())) {
            throw new IncorrectURL("You must not modify the scheme of the URL: "+ baseUrl.getScheme());
        } else if (relativeUrl.getUserInfo()!=null && !relativeUrl.getUserInfo().equals(baseUrl.getUserInfo())) {
            throw new IncorrectURL("You must not modify the user part of the URL: "+ baseUrl.getUserInfo());
        } else if (relativeUrl.getHost()!=null && !relativeUrl.getHost().equals(baseUrl.getHost())) {
            throw new IncorrectURL("You must not modify the host of the URL: "+ baseUrl.getHost());
        }
        return URLFactory.createURL(baseUrl, relativeUrl);
    }

    /** constructor for NSEntry.openAbsolute() */
    public AbstractNSEntryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(entry.m_session, _resolveAbsolutePath(entry.m_url, absolutePath), entry.m_adaptor);
        this.init(flags);
        m_disconnectable = false;
    }
    private static URL _resolveAbsolutePath(URL baseUrl, String absolutePath) throws NotImplemented, IncorrectURL, BadParameter, NoSuccess {
        if (absolutePath==null) {
            throw new IncorrectURL("URL must not be null");
        } else if (! absolutePath.startsWith("/")) {
            throw new IncorrectURL("URL must contain an absolute path: "+ baseUrl.getPath());
        }
        return URLFactory.createURL(baseUrl, absolutePath);
    }

    private void init(int flags) throws BadParameter {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        effectiveFlags.checkAllowed(Flags.CREATE.or(Flags.EXCL.or(Flags.CREATEPARENTS)));
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractNSEntryImpl clone = (AbstractNSEntryImpl) super.clone();
        clone.m_disconnectable = m_disconnectable;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.NSENTRY;
    }

    public URL getURL() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        return m_url;
    }

    public URL getCWD() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        try {
            return m_url.resolve(new URL("."));
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        }
    }

    public URL getName() throws NotImplemented, Timeout, NoSuccess {
        try {
            String name = this._getEntryName();
            if (name != null) {
                if (name.matches("[A-Za-z]:")) {
                    // windows drive
                    return new URL("/"+name);
                } else {
                    // entry name
                    return new URL(JSagaURL.encodePath(name));
                }
            } else {
                return null;
            }
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        }
    }

    public boolean exists() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (m_adaptor instanceof DataReaderAdaptor) {
            return ((DataReaderAdaptor)m_adaptor).exists(
                    m_url.getPath(),
                    m_url.getQuery());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public boolean isDir() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_url instanceof JSagaURL) {
            return ((JSagaURL)m_url).getAttributes().getType() == FileAttributes.DIRECTORY_TYPE;
        } else if (m_adaptor instanceof DataReaderAdaptor) {
            try {
                return ((DataReaderAdaptor)m_adaptor).isDirectory(
                        m_url.getPath(),
                        m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Entry does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public boolean isEntry() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_url instanceof JSagaURL) {
            return ((JSagaURL)m_url).getAttributes().getType() == FileAttributes.FILE_TYPE;
        } else if (m_adaptor instanceof DataReaderAdaptor) {
            try {
                return ((DataReaderAdaptor)m_adaptor).isEntry(
                        m_url.getPath(),
                        m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Entry does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public boolean isLink() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_url instanceof JSagaURL) {
            return ((JSagaURL)m_url).getAttributes().getType() == FileAttributes.LINK_TYPE;
        } else if (m_adaptor instanceof LinkAdaptor) {
            try {
                return ((LinkAdaptor)m_adaptor).isLink(
                        m_url.getPath());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Link does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public URL readLink() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LinkAdaptor) {
            String absolutePath;
            try {
                try {
                    absolutePath = ((LinkAdaptor)m_adaptor).readLink(
                            m_url.getPath());
                } catch (DoesNotExist doesNotExist) {
                    throw new IncorrectState("Link does not exist: "+ m_url, doesNotExist);
                }
            } catch (NotLink notLink) {
                throw new BadParameter("Not a link: "+ m_url, this);
            }
            return URLFactory.createURL(m_url, absolutePath);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public abstract void copy(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL;
    public void copy(URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        this.copy(target, Flags.NONE.getValue());
    }

    public abstract void copyFrom(URL source, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL;
    public void copyFrom(URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        this.copyFrom(target, Flags.NONE.getValue());
    }

    /**
     * Create a link to the physical entry <code>m_url</code>.<br>
     * Note: RECURSIVE flag support is not implemented.
     * @param link the path to the link to create (may be absolute or relative to <code>m_url</code>).
     */
    public void link(URL link, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().link(link, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE.or(Flags.CREATEPARENTS.or(Flags.OVERWRITE))));
        if (effectiveFlags.contains(Flags.RECURSIVE)) {
            throw new NotImplemented("Support of RECURSIVE flags with method link() is not implemented by the SAGA engine", this);
        }
        URL effectiveLink = this._getEffectiveURL(link);
        if (m_adaptor instanceof LinkAdaptor) {
            boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
            try {
                try {
                    ((LinkAdaptor)m_adaptor).link(
                            m_url.getPath(),
                            effectiveLink.getPath(),
                            overwrite);
                } catch (DoesNotExist doesNotExist) {
                    throw new IncorrectState("Entry does not exist: "+ m_url, doesNotExist);
                } catch (AlreadyExists alreadyExists) {
                    throw new AlreadyExists("Target entry already exists: "+effectiveLink, alreadyExists.getCause());
                }
            } catch(IncorrectState e) {
                if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                    // make parent directories
                    this._makeParentDirs();
                    // create link
                    try {
                        ((LinkAdaptor)m_adaptor).link(
                                m_url.getPath(),
                                effectiveLink.getPath(),
                                overwrite);
                    } catch (DoesNotExist doesNotExist) {
                        throw new IncorrectState("Entry does not exist: "+ m_url, doesNotExist);
                    } catch (AlreadyExists alreadyExists) {
                        throw new AlreadyExists("Target entry already exists: "+effectiveLink, alreadyExists.getCause());
                    }
                } else {
                    throw e;
                }
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme());
        }
    }
    public void link(URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        this.link(target, Flags.NONE.getValue());
    }

    public void move(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().move(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.CREATEPARENTS.or(Flags.OVERWRITE)));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_adaptor instanceof DataRename
                && m_url.getScheme().equals(effectiveTarget.getScheme())
                && (m_url.getUserInfo()==null || m_url.getUserInfo().equals(effectiveTarget.getUserInfo()))
                && (m_url.getHost()==null || m_url.getHost().equals(effectiveTarget.getHost()))
                && (m_url.getPort()==effectiveTarget.getPort()))
        {
            try {
                ((DataRename)m_adaptor).rename(
                        m_url.getPath(),
                        effectiveTarget.getPath(),
                        overwrite,
                        m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("File does not exist: "+ m_url, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else {
            this.copy(effectiveTarget, flags);
            this.remove(flags);
        }
    }
    public void move(URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        this.move(target, Flags.NONE.getValue());
    }

    public void remove(int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            try {
                this._dereferenceEntry().remove(flags);
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            }
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.getValue());
        if (m_adaptor instanceof DataWriterAdaptor) {
            URL parent = this._getParentDirURL();
            String fileName = this._getEntryName();
            try {
                ((DataWriterAdaptor)m_adaptor).removeFile(
                        parent.getPath(),
                        fileName,
                        m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("File does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme());
        }
    }
    public void remove() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        this.remove(Flags.NONE.getValue());
    }

    private boolean m_disconnected = false;
    public synchronized void close() throws NotImplemented, IncorrectState, NoSuccess {
        if (m_disconnectable && !m_disconnected) {
            m_adaptor.disconnect();
            m_disconnected = true;
        }
    }

    public void close(float timeoutInSeconds) throws NotImplemented, IncorrectState, NoSuccess {
        this.close();
    }

    public void permissionsAllow(String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess {
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                this._dereferenceEntry().permissionsAllow(id, permissions);
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            }
            return; //==========> EXIT
        }
        super.permissionsAllow(id, permissions);
    }

    public void permissionsDeny(String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                this._dereferenceEntry().permissionsDeny(id, permissions);
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            } catch (IncorrectState e) {
                throw new NoSuccess(e);
            }
            return; //==========> EXIT
        }
        super.permissionsDeny(id, permissions);
    }

    /** override Object.finalize() to disconnect from server */
    public void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public Task close(TaskMode mode) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    public Task close(TaskMode mode, float timeoutInSeconds) throws NotImplemented {
        throw new NotImplemented("Not implemented by the SAGA engine", this);
    }

    /** deviation from SAGA specification */
    public Date getLastModified() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        FileAttributes attrs = null;
        if (m_url instanceof JSagaURL) {
            attrs = ((JSagaURL)m_url).getAttributes();
        } else if (m_adaptor instanceof DataReaderAdaptor) {
            try {
                attrs = ((DataReaderAdaptor)m_adaptor).getAttributes(
                        m_url.getPath(),
                        m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Entry does not exist: "+m_url, doesNotExist);
            }
        }
        if (attrs!=null && attrs.getLastModified()>0) {
            return new Date(attrs.getLastModified());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_url.getScheme());
        }
    }

    /** deviation from SAGA specification */
    public void setLastModified(Date lastModified) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof DataWriterTimes) {
            try {
                ((DataWriterTimes)m_adaptor).setLastModified(
                        m_url.getPath(),
                        m_url.getQuery(),
                        lastModified.getTime());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Entry does not exist: "+m_url, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_url.getScheme());
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    public abstract NSDirectory openAbsoluteDir(String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    public abstract NSEntry openAbsolute(String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    protected AbstractNSDirectoryImpl _dereferenceDir()  throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            String absolutePath = this.readLink().getPath();
            return (AbstractNSDirectoryImpl) this.openAbsoluteDir(absolutePath, Flags.NONE.getValue());
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        }
    }

    protected AbstractNSEntryImpl _dereferenceEntry()  throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            String absolutePath = this.readLink().getPath();
            return (AbstractNSEntryImpl) this.openAbsolute(absolutePath, Flags.NONE.getValue());
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        }
    }

    protected URL _getEffectiveURL(URL target) throws NotImplemented, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (target.getPath().endsWith("/")) {
            return URLFactory.createURL(target, this._getEntryName());
        } else {
            return target;
        }
    }

    protected URL _getParentDirURL() throws NotImplemented, BadParameter, NoSuccess {
        return URLFactory.getParentURL(m_url);
    }
    protected String _getEntryName() throws NotImplemented {
        return URLFactory.getName(m_url);
    }

    protected void _makeParentDirs() throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, Timeout, NoSuccess {
        try {
            String parentAbsolutePath = this._getParentDirURL().getPath();
            this.openAbsoluteDir(parentAbsolutePath, Flags.CREATE.or(Flags.CREATEPARENTS));
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

    protected void _preserveTimes(URL target) throws IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        if (EngineProperties.getBoolean(EngineProperties.DATA_COPY_PRESERVE_TIMES)) {
            Date lastModified;
            try {
                lastModified = this.getLastModified();
            } catch(NotImplemented e) {
                return; //======> EXIT
            }
            AbstractNSEntryImpl targetEntry;
            try {
                targetEntry = (AbstractNSEntryImpl) NSFactory.createNSEntry(m_session, target);
            } catch (DoesNotExist e) {
                throw new NoSuccess("Copy failed", e);
            } catch (NotImplemented e) {
                throw new NoSuccess("Unexpected exception", e);
            }
            try {
                targetEntry.setLastModified(lastModified);
            } catch(NotImplemented e) {
                // ignore
            }
        }
    }

    protected void _preserveTimesFrom(URL source) throws IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (EngineProperties.getBoolean(EngineProperties.DATA_COPY_PRESERVE_TIMES)) {
            AbstractNSEntryImpl sourceEntry;
            try {
                sourceEntry = (AbstractNSEntryImpl) NSFactory.createNSEntry(m_session, source);
            } catch(AlreadyExists e) {
                throw new NoSuccess("Unexpected exception", e);
            } catch (NotImplemented e) {
                throw new NoSuccess("Unexpected exception", e);
            }
            Date lastModified;
            try {
                lastModified = sourceEntry.getLastModified();
            } catch(NotImplemented e) {
                return; //======> EXIT
            }
            try {
                this.setLastModified(lastModified);
            } catch(NotImplemented e) {
                // ignore
            }
        }
    }
}
