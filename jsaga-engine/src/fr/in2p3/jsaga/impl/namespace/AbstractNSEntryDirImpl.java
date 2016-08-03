package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.impl.file.FileImpl;
import fr.in2p3.jsaga.impl.file.copy.AbstractCopyTask;
import fr.in2p3.jsaga.impl.url.AbstractURLImpl;
import fr.in2p3.jsaga.impl.url.URLHelper;
import fr.in2p3.jsaga.sync.namespace.SyncNSDirectory;
import fr.in2p3.jsaga.sync.namespace.SyncNSEntry;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractNSEntryDirImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   17 sept. 2007
 * ***************************************************
 * Description:                                      */
/**
 * This class override some methods of AbstractNSEntryImpl for directories
 */
public abstract class AbstractNSEntryDirImpl extends AbstractNSEntryImpl implements SyncNSDirectory {

    /** constructor for factory */
    protected AbstractNSEntryDirImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSEntryDirImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSEntryDirImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    ////////////////////////////////////// override methods of SyncNSEntry //////////////////////////////////////
    /** override super.getCWDSync() */
    public URL getCWDSync() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        return m_url.normalize();
    }

    /** override super.permissionsAllowSync() */
    public void permissionsAllowSync(String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        try {
            if (Flags.DEREFERENCE.isSet(flags)) {
                AbstractSyncNSEntryImpl dir = this._dereferenceDir();
                try {
                    dir.permissionsAllowSync(id, permissions, flags - Flags.DEREFERENCE.getValue());
                } finally {
                    dir.close();
                }
                return; //==========> EXIT
            }
            // allow permission on current directory
            super.permissionsAllowSync(id, permissions, flags - Flags.RECURSIVE.getValue());
            // allow permission on child entries
            for (FileAttributes child : this._listAttributes(m_url.getPath())) {
                SyncNSEntry childEntry = this._openNS(child);
                try {
                    int childFlags = (childEntry instanceof AbstractSyncNSDirectoryImpl
                            ? flags
                            : flags - Flags.RECURSIVE.getValue());
                    childEntry.permissionsAllowSync(id, permissions, childFlags);
                } finally {
                    ((NSEntry) childEntry).close();
                }
            }
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }

    /** override super.permissionsDenySync() */
    public void permissionsDenySync(String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        try {
            if (Flags.DEREFERENCE.isSet(flags)) {
                AbstractSyncNSEntryImpl dir = this._dereferenceDir();
                try {
                    dir.permissionsDenySync(id, permissions, flags - Flags.DEREFERENCE.getValue());
                } finally {
                    dir.close();
                }
                return; //==========> EXIT
            }
            // deny permission on current directory
            super.permissionsDenySync(id, permissions, flags - Flags.RECURSIVE.getValue());
            // deny permission on child entries
            for (FileAttributes child : this._listAttributes(m_url.getPath())) {
                SyncNSEntry childEntry = this._openNS(child);
                try {
                    int childFlags = (childEntry instanceof AbstractSyncNSDirectoryImpl
                            ? flags
                            : flags - Flags.RECURSIVE.getValue());
                    childEntry.permissionsDenySync(id, permissions, childFlags);
                } finally {
                    ((NSEntry) childEntry).close();
                }
            }
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }

    /** override super.copySync() */
    public void copySync(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        this._copyAndMonitor(target, flags, null);
    }

    public void _copyAndMonitor(URL target, int flags, AbstractCopyTask progressMonitor) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.RECURSIVE, Flags.OVERWRITE, Flags.CREATEPARENTS);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            AbstractSyncNSEntryImpl dir = this._dereferenceDir();
            try {
                dir.copySync(target, flags - Flags.DEREFERENCE.getValue());
            } finally {
                dir.close();
            }
            return; //==========> EXIT
        }
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_adaptor instanceof DataReaderAdaptor) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // copy source childs
            FileAttributes[] sourceChilds = this._listAttributes(m_url.getPath());
            for (int i = 0; i < sourceChilds.length; i++) {
                SyncNSEntry sourceChildEntry = this._openNS(sourceChilds[i]);
                try {
                    if (sourceChildEntry instanceof AbstractSyncNSDirectoryImpl) {
                        ((AbstractSyncNSDirectoryImpl) sourceChildEntry)._copyAndMonitor(effectiveTarget, flags, progressMonitor);
                    } else {
                        // remove RECURSIVE flag (which is always set for NSDirectory.copy())
                        int childFlags = flags - Flags.RECURSIVE.getValue();
                        if (sourceChildEntry instanceof FileImpl) {
                            ((FileImpl) sourceChildEntry)._copyAndMonitor(effectiveTarget, childFlags, progressMonitor);
                        } else {
                            sourceChildEntry.copySync(effectiveTarget, childFlags);
                        }
                    }
                } finally {
                    ((NSEntry) sourceChildEntry).close();
                }
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
    }

    /** override super.copyFromSync() */
    public void copyFromSync(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(JSAGAFlags.PRESERVETIMES, Flags.DEREFERENCE, Flags.RECURSIVE, Flags.OVERWRITE);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            AbstractSyncNSEntryImpl dir = this._dereferenceDir();
            try {
                dir.copyFromSync(source, flags - Flags.DEREFERENCE.getValue());
            } finally {
                dir.close();
            }
            return; //==========> EXIT
        }
        if (m_adaptor instanceof DataWriterAdaptor) {
            try {
                NSDirectory sourceDir = NSFactory.createNSDirectory(JSAGA_FACTORY, m_session, source);
                try {
                    sourceDir.copy(m_url, flags);
                } finally {
                    sourceDir.close();
                }
            } catch (AlreadyExistsException e) {
                throw new IncorrectStateException("Unexpected exception", e);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
    }

    /** override super.moveSync() */
    public void moveSync(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE, Flags.OVERWRITE, Flags.CREATEPARENTS);
        new FlagsHelper(flags).required(Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            AbstractSyncNSEntryImpl dir = this._dereferenceDir();
            try {
                dir.moveSync(target, flags - Flags.DEREFERENCE.getValue());
            } finally {
                dir.close();
            }
            return; //==========> EXIT
        }
        URL effectiveTarget;
        try {
            NSFactory.createNSDirectory(JSAGA_FACTORY, m_session, target, Flags.NONE.getValue()).close();
            // Target Directory already exists, operation is MOVE
            effectiveTarget = this._getEffectiveURL(target);
        } catch (DoesNotExistException dnee) {
            // Target directory does not exist: operation is RENAME
            effectiveTarget = target;
        }
        if (m_adaptor instanceof DataRename
                && m_url.getScheme().equals(effectiveTarget.getScheme())
                && (m_url.getUserInfo() == null || m_url.getUserInfo().equals(effectiveTarget.getUserInfo()))
                && (m_url.getHost() == null || m_url.getHost().equals(effectiveTarget.getHost()))
                && (m_url.getPort() == effectiveTarget.getPort())) {
            boolean overwrite = Flags.OVERWRITE.isSet(flags);
            try {
                ((DataRename) m_adaptor).rename(
                        m_url.getPath(),
                        effectiveTarget.getPath(),
                        overwrite,
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Directory does not exist: " + m_url, doesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new AlreadyExistsException("Target entry already exists: " + effectiveTarget, alreadyExists.getCause());
            }
            if (m_url instanceof AbstractURLImpl) {
                ((AbstractURLImpl) m_url).setCache(null);
            }
        } else if (m_adaptor instanceof DataReaderAdaptor) {
            // make target directory
            this._makeDir(effectiveTarget, flags);
            // move source childs to target directory
            FileAttributes[] sourceChilds = this._listAttributes(m_url.getPath());
            for (int i = 0; i < sourceChilds.length; i++) {
                URL remoteChild = URLHelper.createURL(effectiveTarget, sourceChilds[i].getRelativePath());
                SyncNSEntry entry = this._openNS(sourceChilds[i]);
                try {
                    if (entry instanceof SyncNSDirectory) {
                        entry.moveSync(remoteChild, flags);
                    } else {
                        // remove RECURSIVE flag (always set for NSDirectory.move())
                        entry.moveSync(remoteChild, flags - Flags.RECURSIVE.getValue());
                    }
                } finally {
                    ((NSEntry) entry).close();
                }
            }
            // remove source directory
            if (Flags.OVERWRITE.isSet(flags))
            	flags = flags - Flags.OVERWRITE.getValue();
            this.removeSync(flags);
            if (m_url instanceof AbstractURLImpl) {
                ((AbstractURLImpl) m_url).setCache(null);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
    }

    /**
     * override super.removeSync()
     * <br>Note: does not throw a BadParamater exception when RECURSIVE flag is not set, unless directory has some descendants.
     */
    public void removeSync(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                AbstractSyncNSEntryImpl dir = this._dereferenceDir();
                try {
                    dir.removeSync(flags - Flags.DEREFERENCE.getValue());
                } finally {
                    dir.close();
                }
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
            return; //==========> EXIT
        }
        if (m_adaptor instanceof DataReaderAdaptor && m_adaptor instanceof DataWriterAdaptor) {
            if (Flags.RECURSIVE.isSet(flags)) {
                // remove childs
                FileAttributes[] sourceChilds = this._listAttributes(m_url.getPath());
                for (int i = 0; i < sourceChilds.length; i++) {
                    SyncNSEntry entry;
                    try {
                        entry = this._openNS(sourceChilds[i]);
                        try {
                            if (entry instanceof SyncNSDirectory) {
                                entry.removeSync(flags);
                            } else {
                                // remove RECURSIVE flag (always set here)
                                entry.removeSync(flags - Flags.RECURSIVE.getValue());
                            }
                        } finally {
                            ((NSEntry) entry).close();
                        }
                    } catch (IncorrectURLException e) {
                        throw new NoSuccessException(e);
                    }
                }
            } else {
                // check that there is no child
                FileAttributes[] sourceChilds = this._listAttributes(m_url.getPath());
                if (sourceChilds.length > 0) {
                    throw new BadParameterException("Flag 'Recursive' is required for non-empty directory: " + m_url);
                }
            }

            // remove this directory
            URL parent = super._getParentDirURL();
            String directoryName = super._getEntryName();
            try {
                ((DataWriterAdaptor) m_adaptor).removeDir(
                        parent.getPath(),
                        directoryName,
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Directory does not exist: " + m_url, doesNotExist);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
    }

    ////////////////////////////////////// interface NSDirectory //////////////////////////////////////
    public abstract NSDirectory openDir(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public abstract NSDirectory openDir(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public abstract NSEntry open(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public abstract NSEntry open(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    ///////////////////////////////////////// protected methods /////////////////////////////////////////
    /** override super._getEffectiveURL() */
    protected URL _getEffectiveURL(URL target) throws NotImplementedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        if (target.getPath().endsWith("/")) {
            return URLHelper.createURL(target, super._getEntryName() + "/");
        } else {
            return target;
        }
    }

    protected FileAttributes[] _listAttributes(String absolutePath) throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataReaderAdaptor) {
            try {
                return ((DataReaderAdaptor) m_adaptor).listAttributes(absolutePath, m_url.getQuery());
            } catch (BadParameterException badParameter) {
                throw new IncorrectStateException("Entry is not a directory: " + absolutePath, badParameter);
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Directory does not exist: " + absolutePath, doesNotExist);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
    }

    //does not throw DoesNotExistException because it would mean "parent directory does not exist"
    protected SyncNSEntry _openNS(FileAttributes attr) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        switch (attr.getType()) {
            case FileAttributes.TYPE_DIRECTORY:
                return this._openNSDir(URLFactory.createURL(JSAGA_FACTORY, attr.getRelativePath()));
            case FileAttributes.TYPE_FILE:
            case FileAttributes.TYPE_LINK:
                return this._openNSEntry(URLFactory.createURL(JSAGA_FACTORY, attr.getRelativePath()));
            case FileAttributes.TYPE_UNKNOWN:
            default:
                SyncNSEntry entry = this._openNSEntry(URLFactory.createURL(JSAGA_FACTORY, attr.getRelativePath()));
                if (entry.isDirSync()) {
                    return this._openNSDir(URLFactory.createURL(JSAGA_FACTORY, attr.getRelativePath()));
                } else {
                    return entry;
                }
        }
    }

    //does not throw DoesNotExistException because it would mean "parent directory does not exist"
    protected SyncNSDirectory _openNSDir(URL name) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        try {
            return (SyncNSDirectory) this.openDir(name, Flags.NONE.getValue());
        } catch (DoesNotExistException | AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        }
    }

    //does not throw DoesNotExistException because it would mean "parent directory does not exist"
    protected SyncNSEntry _openNSEntry(URL name) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        try {
            return (SyncNSEntry) this.open(name, Flags.NONE.getValue());
        } catch (DoesNotExistException | AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        }
    }

    protected void _makeDir(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        // set makeDirFlags
        int makeDirFlags = Flags.CREATE.getValue();
        if (!Flags.OVERWRITE.isSet(flags)) {
            makeDirFlags = Flags.EXCL.or(makeDirFlags);
        }
        if (Flags.CREATEPARENTS.isSet(flags)) {
            makeDirFlags = Flags.CREATEPARENTS.or(makeDirFlags);
        }

        // makeDir
        NSFactory.createNSDirectory(JSAGA_FACTORY, m_session, target, makeDirFlags).close();
    }
}
