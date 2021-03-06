package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.helpers.SAGAPattern;
import fr.in2p3.jsaga.impl.url.*;
import fr.in2p3.jsaga.sync.namespace.SyncNSDirectory;
import fr.in2p3.jsaga.sync.namespace.SyncNSEntry;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.*;
import java.util.regex.Pattern;
import org.ogf.saga.namespace.NSEntry;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncNSDirectoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncNSDirectoryImpl extends AbstractNSEntryDirImpl implements SyncNSDirectory {

    /** constructor for factory */
    protected AbstractSyncNSDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, URLHelper.toDirectoryURL(url), adaptor, flags);
        boolean initOK = false;
        try {
            this.init(flags);
            initOK = true;
        } finally {
            if (!initOK) {
                this.close();
            }
        }
    }

    /** constructor for NSDirectory.open() */
    protected AbstractSyncNSDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, URLHelper.toDirectoryURL(relativeUrl), flags);
        boolean initOK = false;
	try {
           this.init(flags);
           initOK = true;
        } finally {
            if (!initOK) {
                this.close();
            }
        }
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractSyncNSDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, URLHelper.toDirectoryPath(absolutePath), flags);
        boolean initOK = false;
	try {
           this.init(flags);
           initOK = true;
        } finally {
            if (!initOK) {
                this.close();
            }
        }
    }

    private void init(int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (Flags.CREATEPARENTS.isSet(flags)) {
            flags = Flags.CREATE.or(flags);
        }
        new FlagsHelper(flags).allowed(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS);
        if (Flags.CREATE.isSet(flags)) {
            if (m_adaptor instanceof DataWriterAdaptor) {
                if ("/".equals(m_url.getPath())) {
                    if (Flags.EXCL.isSet(flags)) {
                        throw new AlreadyExistsException("Not allowed to create root directory: " + m_url.toString(), this);
                    } else {
                        return;
                    }
                }
                URL parent = super._getParentDirURL();
                String directoryName = super._getEntryName();
                try {
                    // try to make current directory
                    ((DataWriterAdaptor) m_adaptor).makeDir(parent.getPath(), directoryName, m_url.getQuery());
                } catch (ParentDoesNotExist e) {
                    // make parent directories, then retry
                    if (Flags.CREATEPARENTS.isSet(flags)) {
                        this._makeParentDirs();
                        try {
                            ((DataWriterAdaptor) m_adaptor).makeDir(parent.getPath(), directoryName, m_url.getQuery());
                        } catch (ParentDoesNotExist e2) {
                            throw new DoesNotExistException("Failed to create parent directory: " + parent, e.getCause());
                        }
                    } else {
                        throw new DoesNotExistException("Parent directory does not exist: " + parent, e.getCause());
                    }
                } catch (AlreadyExistsException e) {
                    if (Flags.EXCL.isSet(flags)) {
                        throw new AlreadyExistsException("Entry already exists: " + m_url, e.getCause());
                    }
                }
            } else {
                throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme());
            }
        } else if (Flags.CREATEPARENTS.isSet(flags)) {
            this._makeParentDirs();
        } else if (!JSAGAFlags.BYPASSEXIST.isSet(flags) && !((AbstractURLImpl) m_url).hasCache() && m_adaptor instanceof DataReaderAdaptor) {
            boolean exists = ((DataReaderAdaptor) m_adaptor).exists(m_url.getPath(), m_url.getQuery());
            if (!exists) {
                throw new DoesNotExistException("Directory does not exist: " + m_url);
            }
        }
    }

    ////////////////////////////////////////// interface SyncNSDirectory //////////////////////////////////////////
    public void changeDirSync(URL dir) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (dir.getScheme() != null || dir.getUserInfo() != null || dir.getHost() != null) {
            throw new IncorrectURLException("Was expecting a absolute/relative path instead of: " + dir.toString());
        }
        m_url = _resolveRelativeUrl(m_url, dir);
    }

    public Iterator<URL> iterator() {
        try {
            return this.listSync().iterator();
        } catch (SagaException e) {
            throw new RuntimeException(e);
        }
    }

    public List<URL> listSync(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this._list(pattern, flags);
    }

    public List<URL> listSync(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this.listSync(pattern, Flags.NONE.getValue());
    }

    public List<URL> listSync(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this._list(null, flags);
    }

    public List<URL> listSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this.listSync(Flags.NONE.getValue());
    }

    private List<URL> _list(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            AbstractSyncNSDirectoryImpl dir = this._dereferenceDir();
            try {
                return dir._list(pattern, flags - Flags.DEREFERENCE.getValue());
            } finally {
                dir.close();
            }
        }

        // get list
        FileAttributes[] childs;
        try {
            if (m_adaptor instanceof DataReaderAdaptor) {
                childs = ((DataReaderAdaptor) m_adaptor).listAttributes(
                        m_url.getPath(),
                        m_url.getQuery());
            } else {
                throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
            }
        } catch (DoesNotExistException e) {
            throw new IncorrectStateException("Directory does not exist: " + m_url, e);
        }

        // filter
        Pattern p = SAGAPattern.toRegexp(pattern);
        List<URL> matchingNames = new ArrayList<URL>();
        for (int i = 0; i < childs.length; i++) {
            if (p == null || p.matcher(childs[i].getRelativePath()).matches()) {
                URL childUrl = URLFactoryImpl.createURLWithCache(childs[i]);
                matchingNames.add(childUrl);
            }
        }
        return matchingNames;
    }

    public List<URL> findSync(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                AbstractSyncNSDirectoryImpl dir = this._dereferenceDir();
                try {
                    return dir.findSync(pattern, flags - Flags.DEREFERENCE.getValue());
                } finally {
                    dir.close();
                }
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
        }

        // search
        List<URL> matchingPath = new ArrayList<URL>();
        if (m_adaptor instanceof DataReaderAdaptor) {
            Pattern p = SAGAPattern.toRegexp(pattern);
            this._doFind(p, flags, matchingPath, CURRENT_DIR_RELATIVE_PATH);
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
        return matchingPath;
    }

    public List<URL> findSync(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        return this.findSync(pattern, Flags.RECURSIVE.getValue());
    }

    private void _doFind(Pattern p, int flags, List<URL> matchingPath, URL currentRelativePath) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        // for each child
        FileAttributes[] childs = this._listAttributes(m_url.getPath());
        for (int i = 0; i < childs.length; i++) {
            // set child relative path
            URL childRelativePath = URLHelper.createURL(currentRelativePath, childs[i].getRelativePath());
            // add child relative path to matching list
            if (p == null || p.matcher(childs[i].getRelativePath()).matches()) {
                matchingPath.add(childRelativePath);
            }
            // may recurse
            if (Flags.RECURSIVE.isSet(flags) && childs[i].getType() == FileAttributes.TYPE_DIRECTORY) {
                AbstractSyncNSDirectoryImpl childDir;
                try {
                    URL childDirName = URLFactory.createURL(JSAGA_FACTORY, childs[i].getRelativePath());
                    childDir = (AbstractSyncNSDirectoryImpl) this._openNSDir(childDirName);
                    try {
                        childDir._doFind(p, flags, matchingPath, childRelativePath);
                    } finally {
                        childDir.close();
                    }
                } catch (IncorrectURLException e) {
                    throw new NoSuccessException(e);
                }
            }
        }
    }

    public boolean existsSync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            SyncNSEntry entry = this._openNSEntryWithDoesNotExist(name);
            ((NSEntry) entry).close();
	    //System.out.println("Entry exists " + name);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    public long getMTimeSync(URL name) throws NotImplementedException, IncorrectURLException, DoesNotExistException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        SyncNSEntry entry = this._openNSEntryWithDoesNotExist(name);
        try {
            return entry.getMTimeSync();
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public boolean isDirSync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        SyncNSEntry entry = this._openNSEntryWithDoesNotExist(name);
        try {
            return entry.isDirSync();
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public boolean isEntrySync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        SyncNSEntry entry = this._openNSEntryWithDoesNotExist(name);
        try {
            return entry.isEntrySync();
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public boolean isLinkSync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        SyncNSEntry entry = this._openNSEntryWithDoesNotExist(name);
        try {
            return entry.isLinkSync();
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public URL readLinkSync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        SyncNSEntry entry = this._openNSEntryWithDoesNotExist(name);
        try {
            return entry.readLinkSync();
        } finally {
            ((NSEntry) entry).close();
        }
    }
    private String[] m_entriesCache = null;

    public int getNumEntriesSync() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataReaderAdaptor) {
            m_entriesCache = this._listNames(m_url.getPath());
            return m_entriesCache.length;
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
    }

    public URL getEntrySync(int entry) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataReaderAdaptor) {
            if (m_entriesCache == null) {
                m_entriesCache = this._listNames(m_url.getPath());
            }
            if (entry < m_entriesCache.length) {
                try {
                    return URLFactory.createURL(JSAGA_FACTORY, m_entriesCache[entry]);
                } catch (BadParameterException e) {
                    throw new NoSuccessException(e);
                }
            } else {
                throw new DoesNotExistException("Invalid index: " + entry, this);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: " + m_url.getScheme(), this);
        }
    }

    public void copySync(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        SyncNSEntry entry = this._openNSEntry(source);
        try {
            entry.copySync(target, flags);
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public void copySync(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.copySync(source, target, Flags.NONE.getValue());
    }

    public void copySync(String sourcePattern, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.listSync(sourcePattern)) {
                SyncNSEntry entry = this._openNSEntry(source);
                try {
                    entry.copySync(target, flags);
                } finally {
                    ((NSEntry) entry).close();
                }
            }
        } else {
            URL source = URLFactory.createURL(JSAGA_FACTORY, sourcePattern);
            SyncNSEntry entry = this._openNSEntry(source);
            try {
                entry.copySync(target, flags);
            } finally {
                ((NSEntry) entry).close();
            }
        }
    }

    public void copySync(String sourcePattern, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.copySync(sourcePattern, target, Flags.NONE.getValue());
    }

    public void linkSync(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        SyncNSEntry entry = this._openNSEntry(source);
        try {
            entry.linkSync(target, flags);
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public void linkSync(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.linkSync(source, target, Flags.NONE.getValue());
    }

    public void linkSync(String sourcePattern, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.listSync(sourcePattern)) {
                SyncNSEntry entry = this._openNSEntry(source);
                ((NSEntry) entry).close();
            }
        } else {
            URL source = URLFactory.createURL(JSAGA_FACTORY, sourcePattern);
            SyncNSEntry entry = this._openNSEntry(source);
            try {
                entry.linkSync(target, flags);
            } finally {
                ((NSEntry) entry).close();
            }
        }
    }

    public void linkSync(String sourcePattern, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.linkSync(sourcePattern, target, Flags.NONE.getValue());
    }

    public void moveSync(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        SyncNSEntry entry = this._openNSEntry(source);
        try {
            entry.moveSync(target, flags);
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public void moveSync(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.moveSync(source, target, Flags.NONE.getValue());
    }

    public void moveSync(String sourcePattern, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.listSync(sourcePattern)) {
                SyncNSEntry entry = this._openNSEntry(source);
                ((NSEntry) entry).close();
            }
        } else {
            URL source = URLFactory.createURL(JSAGA_FACTORY, sourcePattern);
            SyncNSEntry entry = this._openNSEntry(source);
            try {
                entry.moveSync(target, flags);
            } finally {
                ((NSEntry) entry).close();
            }
        }
    }

    public void moveSync(String sourcePattern, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.moveSync(sourcePattern, target, Flags.NONE.getValue());
    }

    public void removeSync(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        SyncNSEntry entry = this._openNSEntry(target);
        try {
            entry.removeSync(flags);
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public void removeSync(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.removeSync(target, Flags.NONE.getValue());
    }

    public void removeSync(String targetPattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            for (URL target : this.listSync(targetPattern)) {
                SyncNSEntry entry = this._openNSEntry(target);
                try {
                    entry.removeSync(flags);
                } finally {
                    ((NSEntry) entry).close();
                }
            }
        } else {
            URL target = URLFactory.createURL(JSAGA_FACTORY, targetPattern);
            SyncNSEntry entry = this._openNSEntry(target);
            try {
                entry.removeSync(flags);
            } finally {
                ((NSEntry) entry).close();
            }
        }
    }

    public void removeSync(String targetPattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.removeSync(targetPattern, Flags.NONE.getValue());
    }

    public void makeDirSync(URL target, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        int makeDirFlags = Flags.CREATE.or(flags);
        NSFactory.createNSDirectory(JSAGA_FACTORY, m_session, target, makeDirFlags).close();
    }

    public void makeDirSync(URL target) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.makeDirSync(target, Flags.NONE.getValue());
    }

    public void permissionsAllowSync(URL target, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        SyncNSEntry entry = this._openNSEntry(target, flags);
        try {
            entry.permissionsAllow(id, permissions);
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public void permissionsAllowSync(URL target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsAllowSync(target, id, permissions, Flags.NONE.getValue());
    }

    public void permissionsAllowSync(String targetPattern, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            try {
                for (URL target : this.listSync(targetPattern)) {
                    SyncNSEntry entry = this._openNSEntry(target, flags);
                    try {
                        entry.permissionsAllow(id, permissions);
                    } finally {
                        ((NSEntry) entry).close();
                    }
                }
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
        } else {
            URL target = URLFactory.createURL(JSAGA_FACTORY, targetPattern);
            SyncNSEntry entry = this._openNSEntry(target, flags);
            try {
                entry.permissionsAllow(id, permissions);
            } finally {
                ((NSEntry) entry).close();
            }
        }
    }

    public void permissionsAllowSync(String targetPattern, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsAllowSync(targetPattern, id, permissions, Flags.NONE.getValue());
    }

    public void permissionsDenySync(URL target, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        SyncNSEntry entry = this._openNSEntry(target, flags);
        try {
            entry.permissionsDeny(id, permissions);
        } finally {
            ((NSEntry) entry).close();
        }
    }

    public void permissionsDenySync(URL target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsDenySync(target, id, permissions, Flags.NONE.getValue());
    }

    public void permissionsDenySync(String targetPattern, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            try {
                for (URL target : this.listSync(targetPattern)) {
                    SyncNSEntry entry = this._openNSEntry(target, flags);
                    try {
                        entry.permissionsDeny(id, permissions);
                    } finally {
                        ((NSEntry) entry).close();
                    }
                }
            } catch (IncorrectURLException | IncorrectStateException e) {
                throw new NoSuccessException(e);
            }
        } else {
            URL target = URLFactory.createURL(JSAGA_FACTORY, targetPattern);
            SyncNSEntry entry = this._openNSEntry(target, flags);
            try {
                entry.permissionsDeny(id, permissions);
            } finally {
                ((NSEntry) entry).close();
            }

        }
    }

    public void permissionsDenySync(String targetPattern, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsDenySync(targetPattern, id, permissions, Flags.NONE.getValue());
    }
    ///////////////////////////////////////// protected methods /////////////////////////////////////////
    protected static URL CURRENT_DIR_RELATIVE_PATH;

    static {
        try {
            CURRENT_DIR_RELATIVE_PATH = URLFactory.createURL(JSAGA_FACTORY, "./");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] _listNames(String absolutePath) throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        FileAttributes[] files = this._listAttributes(absolutePath);
        String[] filenames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            filenames[i] = files[i].getRelativePath();
        }
        return filenames;
    }

    private SyncNSEntry _openNSEntry(URL name, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            return (SyncNSEntry) this.open(name, flags);
        } catch (IncorrectURLException | AlreadyExistsException | DoesNotExistException | IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
    }

    private SyncNSEntry _openNSEntryWithDoesNotExist(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            return (SyncNSEntry) this.open(name, Flags.NONE.getValue());
        } catch (AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        }
    }
}
