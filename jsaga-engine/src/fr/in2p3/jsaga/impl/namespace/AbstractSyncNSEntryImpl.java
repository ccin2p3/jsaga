package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterTimes;
import fr.in2p3.jsaga.impl.permissions.AbstractDataPermissionsImpl;
import fr.in2p3.jsaga.impl.url.URLFactoryImpl;
import fr.in2p3.jsaga.impl.url.URLHelper;
import fr.in2p3.jsaga.sync.namespace.SyncNSEntry;
import org.apache.log4j.Logger;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncNSEntryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncNSEntryImpl extends AbstractDataPermissionsImpl implements SyncNSEntry {
    private static Logger s_logger = Logger.getLogger(AbstractSyncNSEntryImpl.class);

    protected int m_flags;
    private boolean m_disconnectable;

    /** constructor for factory */
    protected AbstractSyncNSEntryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor);
        m_flags = flags;
        m_disconnectable = true;
    }

    /** constructor for NSDirectory.open() */
    protected AbstractSyncNSEntryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir.m_session, _resolveRelativeUrl(dir.m_url, relativeUrl), dir.m_adaptor);
        m_flags = flags;
        m_disconnectable = false;
    }
    protected static URL _resolveRelativeUrl(URL baseUrl, URL relativeUrl) throws NotImplementedException, IncorrectURLException, BadParameterException, NoSuccessException {
        if (relativeUrl==null) {
            throw new IncorrectURLException("URL must not be null");
        } else if (relativeUrl.getUserInfo()!=null && !relativeUrl.getUserInfo().equals(baseUrl.getUserInfo())) {
            throw new IncorrectURLException("You must not modify the user part of the URL: "+ baseUrl.getUserInfo());
        } else if (relativeUrl.getHost()!=null && !relativeUrl.getHost().equals(baseUrl.getHost())) {
            throw new IncorrectURLException("You must not modify the host of the URL: "+ baseUrl.getHost());
        }
        // TODO: use baseUrl.resolve(relativeUrl)
        return URLHelper.createURL(baseUrl, relativeUrl);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractSyncNSEntryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry.m_session, _resolveAbsolutePath(entry.m_url, absolutePath), entry.m_adaptor);
        m_flags = flags;
        m_disconnectable = false;
    }
    private static URL _resolveAbsolutePath(URL baseUrl, String absolutePath) throws NotImplementedException, IncorrectURLException, BadParameterException, NoSuccessException {
        if (absolutePath==null) {
            throw new IncorrectURLException("URL must not be null");
        } else if (! absolutePath.startsWith("/")) {
            throw new IncorrectURLException("URL must contain an absolute path: "+ baseUrl.getPath());
        }
    	// TODO: use baseUrl.resolve
        return URLHelper.createURL(baseUrl, absolutePath);
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractSyncNSEntryImpl clone = (AbstractSyncNSEntryImpl) super.clone();
        clone.m_disconnectable = m_disconnectable;
        return clone;
    }

    //////////////////////////////////////////// interface SyncNSEntry ////////////////////////////////////////////

    public URL getURLSync() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        return m_url;
    }

    public URL getCWDSync() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            return m_url.resolve(URLFactory.createURL("."));
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }

    public URL getNameSync() throws NotImplementedException, TimeoutException, NoSuccessException {
        try {
            return URLFactoryImpl.createRelativePath(this._getEntryName());
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }

    public boolean exists() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataReaderAdaptor) {
            return ((DataReaderAdaptor)m_adaptor).exists(
                    m_url.getPath(),
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public boolean isDirSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        FileAttributes attrs = this._getFileAttributes();
        return (attrs.getType() == FileAttributes.TYPE_DIRECTORY);
    }

    public boolean isEntrySync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        FileAttributes attrs = this._getFileAttributes();
        return (attrs.getType() == FileAttributes.TYPE_FILE);
    }

    public boolean isLinkSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LinkAdaptor) {
            try {
                return ((LinkAdaptor)m_adaptor).isLink(
                        m_url.getPath());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Link does not exist: "+ m_url, doesNotExist);
            }
        } else {
            FileAttributes attrs = this._getFileAttributes();
            return (attrs.getType() == FileAttributes.TYPE_LINK);
        }
    }

    public URL readLinkSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LinkAdaptor) {
            String absolutePath;
            try {
                try {
                    absolutePath = ((LinkAdaptor)m_adaptor).readLink(
                            m_url.getPath());
                } catch (DoesNotExistException doesNotExist) {
                    throw new IncorrectStateException("Link does not exist: "+ m_url, doesNotExist);
                }
            } catch (NotLink notLink) {
                throw new IncorrectStateException("Not a link: "+ m_url, this);
            }
            try {
            	// TODO: use m_url.resolve
                return URLHelper.createURL(m_url, absolutePath);
            } catch (BadParameterException e) {
                throw new IncorrectStateException(e);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public abstract void copySync(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException;
    public void copySync(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        this.copySync(target, Flags.NONE.getValue());
    }

    public abstract void copyFromSync(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException;
    public void copyFromSync(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        this.copyFromSync(target, Flags.NONE.getValue());
    }

    /**
     * Create a link to the physical entry <code>m_url</code>.<br>
     * Note: RECURSIVE flag support is not implemented.
     * @param link the path to the link to create (may be absolute or relative to <code>m_url</code>).
     */
    public void linkSync(URL link, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE, Flags.OVERWRITE, Flags.CREATEPARENTS);
        if (Flags.RECURSIVE.isSet(flags)) {
            throw new NotImplementedException("Support of RECURSIVE flags with method link() is not implemented by the SAGA engine", this);
        }
        if (Flags.DEREFERENCE.isSet(flags)) {
            this._dereferenceEntry().linkSync(link, flags - Flags.DEREFERENCE.getValue());
            return; //==========> EXIT
        }
        URL effectiveLink = this._getEffectiveURL(link);
        if (m_adaptor instanceof LinkAdaptor) {
            boolean overwrite = Flags.OVERWRITE.isSet(flags);
            try {
                try {
                    ((LinkAdaptor)m_adaptor).link(
                            m_url.getPath(),
                            effectiveLink.getPath(),
                            overwrite);
                } catch (DoesNotExistException doesNotExist) {
                    throw new DoesNotExistException("Entry does not exist: "+ m_url, doesNotExist);
                } catch (AlreadyExistsException alreadyExists) {
                    throw new AlreadyExistsException("Target entry already exists: "+effectiveLink, alreadyExists.getCause());
                }
            } catch(DoesNotExistException e) {
                if (Flags.CREATEPARENTS.isSet(flags)) {
                    // make parent directories
                    this._makeParentDirs();
                    // create link
                    try {
                        ((LinkAdaptor)m_adaptor).link(
                                m_url.getPath(),
                                effectiveLink.getPath(),
                                overwrite);
                    } catch (DoesNotExistException doesNotExist) {
                        throw new DoesNotExistException("Entry does not exist: "+ m_url, doesNotExist);
                    } catch (AlreadyExistsException alreadyExists) {
                        throw new AlreadyExistsException("Target entry already exists: "+effectiveLink, alreadyExists.getCause());
                    }
                } else {
                    throw e;
                }
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme());
        }
    }
    public void linkSync(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        this.linkSync(target, Flags.NONE.getValue());
    }

    public void moveSync(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.OVERWRITE, Flags.CREATEPARENTS);
        if (Flags.DEREFERENCE.isSet(flags)) {
            this._dereferenceEntry().moveSync(target, flags - Flags.DEREFERENCE.getValue());
            return; //==========> EXIT
        }
        URL effectiveTarget = this._getEffectiveURL(target);
        boolean overwrite = Flags.OVERWRITE.isSet(flags);
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
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("File does not exist: "+ m_url, doesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new AlreadyExistsException("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else {
            this.copySync(effectiveTarget, flags);
            this.removeSync(flags);
        }
    }
    public void moveSync(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        this.moveSync(target, Flags.NONE.getValue());
    }

    public void removeSync(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                this._dereferenceEntry().removeSync(flags - Flags.DEREFERENCE.getValue());
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
            return; //==========> EXIT
        }
        if (m_adaptor instanceof DataWriterAdaptor) {
            URL parent = this._getParentDirURL();
            String fileName = this._getEntryName();
            try {
                ((DataWriterAdaptor)m_adaptor).removeFile(
                        parent.getPath(),
                        fileName,
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("File does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme());
        }
    }
    public void removeSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        this.removeSync(Flags.NONE.getValue());
    }

    /** timeout not supported */
    private boolean m_disconnected = false;
    public synchronized void close() throws NotImplementedException, NoSuccessException {
        if (m_disconnectable && !m_disconnected) {
            m_disconnected = true;
            m_adaptor.disconnect();
        }
    }

    public void close(float timeoutInSeconds) throws NotImplementedException, NoSuccessException {
        this.close();
    }

    public void permissionsAllowSync(String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                this._dereferenceEntry().permissionsAllowSync(id, permissions, flags - Flags.DEREFERENCE.getValue());
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
            return; //==========> EXIT
        }
        super.permissionsAllow(id, permissions);
    }

    public void permissionsDenySync(String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                this._dereferenceEntry().permissionsDenySync(id, permissions, flags - Flags.DEREFERENCE.getValue());
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            } catch (IncorrectStateException e) {
                throw new NoSuccessException(e);
            }
            return; //==========> EXIT
        }
        super.permissionsDeny(id, permissions);
    }

    /** override Object.finalize() to disconnect from server */
    public void finalize() throws Throwable {
        if (m_disconnectable && !m_disconnected) {
            s_logger.error("NSEntry objects MUST be closed in order to free resources");
        }
        super.finalize();
    }

    public long getMTimeSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        FileAttributes attrs = this._getFileAttributes();
        if (attrs!=null && attrs.getLastModified()>0) {
            return attrs.getLastModified();
        }
        throw new NotImplementedException("Not supported for this protocol: "+m_url.getScheme(), this);
    }

    /** used by method copy() */
    public void setMTime(long lastModified) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataWriterTimes) {
            try {
                ((DataWriterTimes)m_adaptor).setLastModified(
                        m_url.getPath(),
                        m_url.getQuery(),
                        lastModified);
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Entry does not exist: "+m_url, doesNotExist);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+m_url.getScheme(), this);
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    public abstract NSDirectory openAbsoluteDir(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;
    public abstract NSEntry openAbsolute(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    protected AbstractSyncNSDirectoryImpl _dereferenceDir()  throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            String absolutePath = this.readLinkSync().getPath();
            return (AbstractSyncNSDirectoryImpl) this.openAbsoluteDir(absolutePath, Flags.NONE.getValue());
        } catch (AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        } catch (DoesNotExistException e) {
            throw new IncorrectStateException(e);
        }
    }

    protected AbstractSyncNSEntryImpl _dereferenceEntry()  throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            String absolutePath = this.readLinkSync().getPath();
            return (AbstractSyncNSEntryImpl) this.openAbsolute(absolutePath, Flags.NONE.getValue());
        } catch (AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        } catch (DoesNotExistException e) {
            throw new IncorrectStateException(e);
        }
    }

    protected URL _getEffectiveURL(URL target) throws NotImplementedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (target.getPath().endsWith("/")) {
        	// TODO: use target.resolve
            return URLHelper.createURL(target, this._getEntryName());
        } else {
            return target;
        }
    }

    protected URL _getParentDirURL() throws NotImplementedException, BadParameterException, NoSuccessException {
        return URLHelper.getParentURL(m_url);
    }
    protected String _getEntryName() throws NotImplementedException {
        return URLHelper.getName(m_url);
    }

    protected void _makeParentDirs() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, TimeoutException, NoSuccessException {
        try {
            String parentAbsolutePath = this._getParentDirURL().getPath();
            this.openAbsoluteDir(parentAbsolutePath, Flags.CREATE.or(Flags.CREATEPARENTS));
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
    }

    protected AbstractNSEntryImpl _getTargetEntry_checkPreserveTimes(URL target) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException {
        AbstractNSEntryImpl targetEntry;
        try {
            targetEntry = (AbstractNSEntryImpl) NSFactory.createNSEntry(m_session, target, JSAGAFlags.BYPASSEXIST.getValue());
        } catch (DoesNotExistException e) {
            throw new NoSuccessException("Unexpected exception", e);
        }
        if (! (targetEntry.m_adaptor instanceof DataWriterTimes) ) {
            throw new NotImplementedException("Flag PRESERVETIMES ("+JSAGAFlags.PRESERVETIMES+") not supported for protocol: "+target.getScheme());
        }
        return targetEntry;
    }

    protected long _getSourceTimes_checkPreserveTimes(URL source) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (! (m_adaptor instanceof DataWriterTimes) ) {
            throw new NotImplementedException("Flag PRESERVETIMES ("+JSAGAFlags.PRESERVETIMES+") not supported for protocol: "+m_url.getScheme());
        }
        AbstractNSEntryImpl sourceEntry;
        try {
            sourceEntry = (AbstractNSEntryImpl) NSFactory.createNSEntry(m_session, source);
        } catch(AlreadyExistsException e) {
            throw new NoSuccessException("Unexpected exception", e);
        }
        return sourceEntry.getMTime();
    }
}
