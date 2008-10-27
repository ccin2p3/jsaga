package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataFilteredList;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.helpers.SAGAPattern;
import fr.in2p3.jsaga.impl.url.*;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.*;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSDirectoryImpl extends AbstractAsyncNSDirectoryImpl implements NSDirectory {
    /** constructor for factory */
    protected AbstractNSDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, URLHelper.toDirectoryURL(url), adaptor, flags);
        this.init(flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, URLHelper.toDirectoryURL(relativeUrl), flags);
        this.init(flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, URLHelper.toDirectoryPath(absolutePath), flags);
        this.init(flags);
    }

    private void init(int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS);
        if (Flags.CREATE.isSet(flags)) {
            if (m_adaptor instanceof DataWriterAdaptor) {
                if ("/".equals(m_url.getPath())) {
                    if (Flags.EXCL.isSet(flags)) {
                        throw new AlreadyExistsException("Not allowed to create root directory: "+ m_url.toString(), this);
                    } else {
                        return;
                    }
                }
                URL parent = super._getParentDirURL();
                String directoryName = super._getEntryName();
                try {
                    // try to make current directory
                    ((DataWriterAdaptor)m_adaptor).makeDir(parent.getPath(), directoryName, m_url.getQuery());
                } catch(ParentDoesNotExist e) {
                    // make parent directories, then retry
                    if (Flags.CREATEPARENTS.isSet(flags)) {
                        this._makeParentDirs();
                        try {
                            ((DataWriterAdaptor)m_adaptor).makeDir(parent.getPath(), directoryName, m_url.getQuery());
                        } catch (ParentDoesNotExist e2) {
                            throw new DoesNotExistException("Failed to create parent directory: "+parent, e.getCause());
                        }
                    } else {
                        throw new DoesNotExistException("Parent directory does not exist: "+parent, e.getCause());
                    }
                } catch(AlreadyExistsException e) {
                    if (Flags.EXCL.isSet(flags)) {
                        throw new AlreadyExistsException("Entry already exists: "+ m_url, e.getCause());
                    }
                }
            } else {
                throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme());
            }
        } else if (Flags.CREATEPARENTS.isSet(flags)) {
            this._makeParentDirs();
        } else if (!JSAGAFlags.BYPASSEXIST.isSet(flags) && !((URLImpl)m_url).hasCache() && m_adaptor instanceof DataReaderAdaptor) {
            boolean exists = ((DataReaderAdaptor)m_adaptor).exists(m_url.getPath(), m_url.getQuery());
            if (! exists) {
                throw new DoesNotExistException("Directory does not exist: "+ m_url);
            }
        }
    }

    public void changeDir(URL dir) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (dir.getScheme()!=null || dir.getUserInfo()!=null || dir.getHost()!=null) {
            throw new IncorrectURLException("Was expecting a absolute/relative path instead of: "+dir.toString());
        }
        m_url = _resolveRelativeUrl(m_url, dir);
    }

    public Iterator<URL> iterator() {
        try {
            return this.list().iterator();
        } catch (SagaException e) {
            throw new RuntimeException(e);
        }
    }

    public List<URL> list(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this._list(pattern, flags);
    }
    public List<URL> list(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this.list(pattern, Flags.NONE.getValue());
    }

    public List<URL> list(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this._list(null, flags);
    }
    public List<URL> list() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        return this.list(Flags.NONE.getValue());
    }

    private List<URL> _list(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, IncorrectURLException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            return this._dereferenceDir()._list(pattern, flags - Flags.DEREFERENCE.getValue());
        }

        // get list
        FileAttributes[] childs;
        try {
            if (m_adaptor instanceof DataFilteredList && pattern.endsWith("/")) {
                childs = ((DataFilteredList)m_adaptor).listDirectories(
                        m_url.getPath(),
                        m_url.getQuery());
            } else if (m_adaptor instanceof DataReaderAdaptor) {
                childs = ((DataReaderAdaptor)m_adaptor).listAttributes(
                        m_url.getPath(),
                        m_url.getQuery());
            } else {
                throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
            }
        } catch (DoesNotExistException e) {
            throw new IncorrectStateException("Directory does not exist: "+m_url, e);
        }

        // filter
        Pattern p = SAGAPattern.toRegexp(pattern);
        List<URL> matchingNames = new ArrayList<URL>();
        for (int i=0; i<childs.length; i++) {
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                URL childUrl = URLFactoryImpl.createRelativePath(childs[i].getName());
                ((URLImpl)childUrl).setCache(childs[i]);
                matchingNames.add(childUrl);
            }
        }
        return matchingNames;
    }

    public List<URL> find(String pattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                return this._dereferenceDir().find(pattern, flags - Flags.DEREFERENCE.getValue());
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
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        return matchingPath;
    }
    public List<URL> find(String pattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        return this.find(pattern, Flags.RECURSIVE.getValue());
    }

    private void _doFind(Pattern p, int flags, List<URL> matchingPath, URL currentRelativePath) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        // for each child
        FileAttributes[] childs = this._listAttributes(m_url.getPath());
        for (int i=0; i<childs.length; i++) {
            // set child relative path
            URL childRelativePath = URLHelper.createURL(currentRelativePath, childs[i].getName());
            // add child relative path to matching list
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                matchingPath.add(childRelativePath);
            }
            // may recurse
            if (Flags.RECURSIVE.isSet(flags) && childs[i].getType()==FileAttributes.DIRECTORY_TYPE) {
                AbstractNSDirectoryImpl childDir;
                try {
                    childDir = (AbstractNSDirectoryImpl) this._openNSDir(childRelativePath);
                } catch (IncorrectURLException e) {
                    throw new NoSuccessException(e);
                }
                childDir._doFind(p, flags, matchingPath, childRelativePath);
            }
        }
    }

    public boolean exists(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        return ((AbstractNSEntryImpl)this._openNSEntry(name)).exists();
    }

    public boolean isDir(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        return this._openNSEntryWithDoesNotExist(name).isDir();
    }

    public boolean isEntry(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        return this._openNSEntryWithDoesNotExist(name).isEntry();
    }

    public boolean isLink(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        return this._openNSEntryWithDoesNotExist(name).isLink();
    }

    public URL readLink(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException, DoesNotExistException {
        return this._openNSEntryWithDoesNotExist(name).readLink();
    }

    private String[] m_entriesCache = null;
    public int getNumEntries() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataReaderAdaptor) {
            m_entriesCache = this._listNames(m_url.getPath());
            return m_entriesCache.length;
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public URL getEntry(int entry) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof DataReaderAdaptor) {
            if (m_entriesCache == null) {
                m_entriesCache = this._listNames(m_url.getPath());
            }
            if (entry < m_entriesCache.length) {
                try {
                    return URLFactory.createURL(m_entriesCache[entry]);
                } catch (BadParameterException e) {
                    throw new NoSuccessException(e);
                }
            } else {
                throw new DoesNotExistException("Invalid index: "+entry, this);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void copy(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this._openNSEntry(source).copy(target, flags);
    }
    public void copy(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.copy(source, target, Flags.NONE.getValue());
    }

    public void copy(String sourcePattern, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.list(sourcePattern)) {
                this._openNSEntry(source).copy(target, flags);
            }
        } else {
            URL source = URLFactory.createURL(sourcePattern);
            this._openNSEntry(source).copy(target, flags);
        }
    }
    public void copy(String sourcePattern, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.copy(sourcePattern, target, Flags.NONE.getValue());
    }

    public void link(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this._openNSEntry(source).link(target, flags);
    }
    public void link(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.link(source, target, Flags.NONE.getValue());
    }

    public void link(String sourcePattern, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.list(sourcePattern)) {
                this._openNSEntry(source);
            }
        } else {
            URL source = URLFactory.createURL(sourcePattern);
            this._openNSEntry(source).link(target, flags);
        }
    }
    public void link(String sourcePattern, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.link(sourcePattern, target, Flags.NONE.getValue());
    }

    public void move(URL source, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this._openNSEntry(source).move(target, flags);
    }
    public void move(URL source, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.move(source, target, Flags.NONE.getValue());
    }
    
    public void move(String sourcePattern, URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.list(sourcePattern)) {
                this._openNSEntry(source);
            }
        } else {
            URL source = URLFactory.createURL(sourcePattern);
            this._openNSEntry(source).move(target, flags);
        }
    }
    public void move(String sourcePattern, URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.move(sourcePattern, target, Flags.NONE.getValue());
    }

    public void remove(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        this._openNSEntry(target).remove(flags);
    }
    public void remove(URL target) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.remove(target, Flags.NONE.getValue());
    }
    
    public void remove(String targetPattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            for (URL target : this.list(targetPattern)) {
                this._openNSEntry(target).remove(flags);
            }
        } else {
            URL target = URLFactory.createURL(targetPattern);
            this._openNSEntry(target).remove(flags);
        }
    }
    public void remove(String targetPattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectURLException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.remove(targetPattern, Flags.NONE.getValue());
    }

    public void makeDir(URL target, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        int makeDirFlags = Flags.CREATE.or(flags);
        NSFactory.createNSDirectory(m_session, target, makeDirFlags);
    }
    public void makeDir(URL target) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.makeDir(target, Flags.NONE.getValue());
    }

    public void permissionsAllow(URL target, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        this._openNSEntry(target, flags).permissionsAllow(id, permissions);
    }
    public void permissionsAllow(URL target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsAllow(target, id, permissions, Flags.NONE.getValue());
    }

    public void permissionsAllow(String targetPattern, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            try {
                for (URL target : this.list(targetPattern)) {
                    this._openNSEntry(target, flags).permissionsAllow(id, permissions);
                }
            } catch(IncorrectURLException e) {throw new NoSuccessException(e);}
        } else {
            URL target = URLFactory.createURL(targetPattern);
            this._openNSEntry(target, flags).permissionsAllow(id, permissions);
        }
    }
    public void permissionsAllow(String targetPattern, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsAllow(targetPattern, id, permissions, Flags.NONE.getValue());
    }

    public void permissionsDeny(URL target, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        this._openNSEntry(target, flags).permissionsDeny(id, permissions);
    }
    public void permissionsDeny(URL target, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsDeny(target, id, permissions, Flags.NONE.getValue());
    }
    
    public void permissionsDeny(String targetPattern, String id, int permissions, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            try {
                for (URL target : this.list(targetPattern)) {
                    this._openNSEntry(target, flags).permissionsDeny(id, permissions);
                }
            } catch(IncorrectURLException e) {throw new NoSuccessException(e);
            } catch(IncorrectStateException e) {throw new NoSuccessException(e);}
        } else {
            URL target = URLFactory.createURL(targetPattern);
            this._openNSEntry(target, flags).permissionsDeny(id, permissions);
        }
    }
    public void permissionsDeny(String targetPattern, String id, int permissions) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        this.permissionsDeny(targetPattern, id, permissions, Flags.NONE.getValue());
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    protected static URL CURRENT_DIR_RELATIVE_PATH;
    static {
        try {
            CURRENT_DIR_RELATIVE_PATH = URLFactory.createURL("./");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] _listNames(String absolutePath) throws NotImplementedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        FileAttributes[] files = this._listAttributes(absolutePath);
        String[] filenames = new String[files.length];
        for (int i=0; i<files.length; i++) {
            filenames[i] = files[i].getName();
        }
        return filenames;
    }

    private NSEntry _openNSEntry(URL name, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        try {
            return this.open(name, flags);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        }
    }
    private NSEntry _openNSEntryWithDoesNotExist(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            return this.open(name, Flags.NONE.getValue());
        } catch (AlreadyExistsException e) {
            throw new IncorrectStateException(e);
        }
    }
}
