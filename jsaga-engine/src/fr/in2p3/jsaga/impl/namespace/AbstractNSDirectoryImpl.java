package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.FilteredDirectoryReader;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.helpers.SAGAPattern;
import fr.in2p3.jsaga.helpers.URLFactory;
import org.ogf.saga.ObjectType;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
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
    public AbstractNSDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, URLFactory.toDirectoryURL(url), adaptor, flags);
        this.init(flags);
    }

    /** constructor for open() */
    public AbstractNSDirectoryImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, URLFactory.toDirectoryURL(url), flags);
        this.init(flags);
    }

    private void init(int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.CREATE)) {
            if (m_adaptor instanceof DirectoryWriter) {
                if ("/".equals(m_url.getPath())) {
                    if (effectiveFlags.contains(Flags.EXCL)) {
                        throw new AlreadyExists("Not allowed to create root directory: "+ m_url.toString(), this);
                    } else {
                        return;
                    }
                }
                URL parent = super._getParentDirURL();
                String directoryName = super._getEntryName();
                try {
                    // try to make current directory
                    ((DirectoryWriter)m_adaptor).makeDir(parent.getPath(), directoryName);
                } catch(ParentDoesNotExist e) {
                    // make parent directories, then retry
                    if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                        this._makeParentDirs();
                        try {
                            ((DirectoryWriter)m_adaptor).makeDir(parent.getPath(), directoryName);
                        } catch (ParentDoesNotExist e2) {
                            throw new DoesNotExist("Failed to create parent directory: "+parent, e.getCause());
                        }
                    } else {
                        throw new DoesNotExist("Parent directory does not exist: "+parent, e.getCause());
                    }
                } catch(AlreadyExists e) {
                    if (effectiveFlags.contains(Flags.EXCL)) {
                        throw new AlreadyExists("Directory already exists: "+ m_url, e.getCause());
                    }
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme());
            }
        } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            this._makeParentDirs();
        } else if (! JSAGAFlags.BYPASSEXIST.isSet(flags)) {
            if (m_adaptor instanceof DataReaderAdaptor && !((DataReaderAdaptor)m_adaptor).exists(m_url.getPath())) {
                throw new DoesNotExist("Directory does not exist: "+ m_url);
            }
        }
    }

    public ObjectType getType() {
        return ObjectType.NSDIRECTORY;
    }

    public void changeDir(URL dir) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (dir.getScheme()!=null || dir.getUserInfo()!=null || dir.getHost()!=null) {
            throw new IncorrectURL("Was expecting a absolute/relative path instead of: "+dir.toString());
        }
        m_url = this._resolveRelativeURL(dir);
    }

    public List<URL> list(String pattern, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        return this._list(pattern, false, flags);
    }
    public List<URL> list(String pattern) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        return this.list(pattern, Flags.NONE.getValue());
    }

    public List<URL> list(int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        return this._list(null, false, flags);
    }
    public List<URL> list() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        return this.list(Flags.NONE.getValue());
    }

    public List<String> listWithLongFormat(String pattern, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        return this._list(pattern, true, flags);
    }
    private List _list(String pattern, boolean longFormat, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this._dereferenceDir()._list(pattern, longFormat, effectiveFlags.remove(Flags.DEREFERENCE));
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.getValue());

        // get list
        FileAttributes[] childs;
        try {
            if (m_adaptor instanceof FilteredDirectoryReader && pattern.endsWith("/")) {
                childs = ((FilteredDirectoryReader)m_adaptor).listDirectories(m_url.getPath());
            } else if (m_adaptor instanceof DirectoryReader) {
                childs = ((DirectoryReader)m_adaptor).listAttributes(m_url.getPath());
            } else {
                throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
            }
        } catch (DoesNotExist e) {
            throw new IncorrectState("Directory does not exist: "+m_url.getPath(), e);
        }

        // filter
        Pattern p = SAGAPattern.toRegexp(pattern);
        List<Object> matchingNames = new ArrayList<Object>();
        for (int i=0; i<childs.length; i++) {
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                if (longFormat) {
                    matchingNames.add(childs[i].toString());
                } else {
                    String correctedName = childs[i].getName().replaceAll(" ", "%20");
                    matchingNames.add(new URL(correctedName));
                }
            }
        }
        return matchingNames;
    }

    public List<URL> find(String pattern, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            try {
                return this._dereferenceDir().find(pattern, effectiveFlags.remove(Flags.DEREFERENCE));
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            }
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE));

        // search
        List<URL> matchingPath = new ArrayList<URL>();
        if (m_adaptor instanceof DirectoryReader) {
            Pattern p = SAGAPattern.toRegexp(pattern);
            this._doFind(p, effectiveFlags, matchingPath, CURRENT_DIR_RELATIVE_PATH);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        return matchingPath;
    }
    public List<URL> find(String pattern) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this.find(pattern, Flags.RECURSIVE.getValue());
    }

    private void _doFind(Pattern p, FlagsBytes effectiveFlags, List<URL> matchingPath, URL currentRelativePath) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // for each child
        FileAttributes[] childs = this._listAttributes(m_url.getPath());
        for (int i=0; i<childs.length; i++) {
            // set child relative path
            URL childRelativePath = URLFactory.createURL(currentRelativePath, childs[i].getName());
            // add child relative path to matching list
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                matchingPath.add(childRelativePath);
            }
            // may recurse
            if (effectiveFlags.contains(Flags.RECURSIVE) && childs[i].getType()==FileAttributes.DIRECTORY_TYPE) {
                AbstractNSDirectoryImpl childDir;
                try {
                    childDir = (AbstractNSDirectoryImpl) this._openNSDir(childRelativePath);
                } catch (IncorrectURL e) {
                    throw new NoSuccess(e);
                }
                childDir._doFind(p, effectiveFlags, matchingPath, childRelativePath);
            }
        }
    }

    public boolean exists(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return ((AbstractNSEntryImpl)this._openNSEntry(name)).exists();
    }

    public boolean isDir(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, DoesNotExist {
        return this._openNSEntryWithDoesNotExist(name).isDir();
    }

    public boolean isEntry(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, DoesNotExist {
        return this._openNSEntryWithDoesNotExist(name).isEntry();
    }

    public boolean isLink(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, DoesNotExist {
        return this._openNSEntryWithDoesNotExist(name).isLink();
    }

    public URL readLink(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, DoesNotExist {
        return this._openNSEntryWithDoesNotExist(name).readLink();
    }

    private String[] m_entriesCache = null;
    public int getNumEntries() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof DirectoryReader) {
            m_entriesCache = this._listNames(m_url.getPath());
            return m_entriesCache.length;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public URL getEntry(int entry) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof DirectoryReader) {
            if (m_entriesCache == null) {
                m_entriesCache = this._listNames(m_url.getPath());
            }
            if (entry < m_entriesCache.length) {
                try {
                    return new URL(m_entriesCache[entry]);
                } catch (BadParameter e) {
                    throw new NoSuccess(e);
                }
            } else {
                throw new DoesNotExist("Invalid index: "+entry, this);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void copy(URL source, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).copy(target, flags);
    }
    public void copy(URL source, URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.copy(source, target, Flags.NONE.getValue());
    }

    public void copy(String sourcePattern, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.list(sourcePattern)) {
                this._openNSEntry(source).copy(target, flags);
            }
        } else {
            URL source = new URL(sourcePattern);
            this._openNSEntry(source).copy(target, flags);
        }
    }
    public void copy(String sourcePattern, URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.copy(sourcePattern, target, Flags.NONE.getValue());
    }

    public void link(URL source, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).link(target, flags);
    }
    public void link(URL source, URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.link(source, target, Flags.NONE.getValue());
    }

    public void link(String sourcePattern, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.list(sourcePattern)) {
                this._openNSEntry(source);
            }
        } else {
            URL source = new URL(sourcePattern);
            this._openNSEntry(source).link(target, flags);
        }
    }
    public void link(String sourcePattern, URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.link(sourcePattern, target, Flags.NONE.getValue());
    }

    public void move(URL source, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).move(target, flags);
    }
    public void move(URL source, URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.move(source, target, Flags.NONE.getValue());
    }
    
    public void move(String sourcePattern, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (SAGAPattern.hasWildcard(sourcePattern)) {
            for (URL source : this.list(sourcePattern)) {
                this._openNSEntry(source);
            }
        } else {
            URL source = new URL(sourcePattern);
            this._openNSEntry(source).move(target, flags);
        }
    }
    public void move(String sourcePattern, URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.move(sourcePattern, target, Flags.NONE.getValue());
    }

    public void remove(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(target).remove(flags);
    }
    public void remove(URL target) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        this.remove(target, Flags.NONE.getValue());
    }
    
    public void remove(String targetPattern, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            for (URL target : this.list(targetPattern)) {
                this._openNSEntry(target).remove(flags);
            }
        } else {
            URL target = new URL(targetPattern);
            this._openNSEntry(target).remove(flags);
        }
    }
    public void remove(String targetPattern) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        this.remove(targetPattern, Flags.NONE.getValue());
    }

    public void makeDir(URL target, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        int makeDirFlags = Flags.CREATE.or(flags);
        NSFactory.createNSDirectory(m_session, target, makeDirFlags);
    }
    public void makeDir(URL target) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.makeDir(target, Flags.NONE.getValue());
    }

    public void permissionsAllow(URL target, String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess {
        this._openNSEntry(target, flags).permissionsAllow(id, permissions);
    }
    public void permissionsAllow(URL target, String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess {
        this.permissionsAllow(target, id, permissions, Flags.NONE.getValue());
    }

    public void permissionsAllow(String targetPattern, String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            try {
                for (URL target : this.list(targetPattern)) {
                    this._openNSEntry(target, flags).permissionsAllow(id, permissions);
                }
            } catch(IncorrectURL e) {throw new NoSuccess(e);}
        } else {
            URL target = new URL(targetPattern);
            this._openNSEntry(target, flags).permissionsAllow(id, permissions);
        }
    }
    public void permissionsAllow(String targetPattern, String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess {
        this.permissionsAllow(targetPattern, id, permissions, Flags.NONE.getValue());
    }

    public void permissionsDeny(URL target, String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        this._openNSEntry(target, flags).permissionsDeny(id, permissions);
    }
    public void permissionsDeny(URL target, String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        this.permissionsDeny(target, id, permissions, Flags.NONE.getValue());
    }
    
    public void permissionsDeny(String targetPattern, String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        if (SAGAPattern.hasWildcard(targetPattern)) {
            try {
                for (URL target : this.list(targetPattern)) {
                    this._openNSEntry(target, flags).permissionsDeny(id, permissions);
                }
            } catch(IncorrectURL e) {throw new NoSuccess(e);
            } catch(IncorrectState e) {throw new NoSuccess(e);}
        } else {
            URL target = new URL(targetPattern);
            this._openNSEntry(target, flags).permissionsDeny(id, permissions);
        }
    }
    public void permissionsDeny(String targetPattern, String id, int permissions) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        this.permissionsDeny(targetPattern, id, permissions, Flags.NONE.getValue());
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    protected static URL CURRENT_DIR_RELATIVE_PATH;
    static {
        try {
            CURRENT_DIR_RELATIVE_PATH = new URL("./");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected URL _resolveRelativeURL(URL relativePath) throws NotImplemented, IncorrectURL, BadParameter, NoSuccess {
        if (relativePath==null) {
            throw new IncorrectURL("URL must not be null");
        } else if (relativePath.getScheme()!=null && !relativePath.getScheme().equals(m_url.getScheme())) {
            throw new IncorrectURL("You must not modify the scheme of the URL: "+ m_url.getScheme());
        } else if (relativePath.getUserInfo()!=null && !relativePath.getUserInfo().equals(m_url.getUserInfo())) {
            throw new IncorrectURL("You must not modify the user part of the URL: "+ m_url.getUserInfo());
        } else if (relativePath.getHost()!=null && !relativePath.getHost().equals(m_url.getHost())) {
            throw new IncorrectURL("You must not modify the host of the URL: "+ m_url.getHost());
        }
        return URLFactory.createURL(m_url, relativePath.getPath());
    }

    private String[] _listNames(String absolutePath) throws NotImplemented, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        FileAttributes[] files = this._listAttributes(absolutePath);
        String[] filenames = new String[files.length];
        for (int i=0; i<files.length; i++) {
            filenames[i] = files[i].getName();
        }
        return filenames;
    }

    private NSEntry _openNSEntry(URL name, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        try {
            return this.open(name, flags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        } catch (AlreadyExists e) {
            throw new NoSuccess(e);
        }
    }
    private NSEntry _openNSEntryWithDoesNotExist(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        try {
            return this.open(name, Flags.NONE.getValue());
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        }
    }
}
