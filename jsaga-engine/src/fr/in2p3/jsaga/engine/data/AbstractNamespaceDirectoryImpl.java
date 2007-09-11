package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.ExtensionFlags;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.engine.factories.NamespaceFactoryImpl;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNamespaceDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNamespaceDirectoryImpl extends AbstractNamespaceEntryDirImpl implements NamespaceDirectory {
    /** constructor */
    public AbstractNamespaceDirectoryImpl(Session session, URI uri, Flags flags, DataConnection connection) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, connection);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        effectiveFlags.keepNamespaceEntryFlags();
        if (effectiveFlags.contains(Flags.CREATE)) {
            if (m_adaptor instanceof DirectoryWriter) {
                if ("/".equals(m_uri.getPath())) {
                    if (effectiveFlags.contains(Flags.EXCL)) {
                        throw new AlreadyExists("Not allowed to create root directory: "+m_uri.toString(), this);
                    } else {
                        return;
                    }
                }
                URI parent = super._getParentDirURI();
                String directoryName = super.getName();
                try {
                    // try to make current directory
                    ((DirectoryWriter)m_adaptor).makeDir(parent.getPath(), directoryName);
                } catch(DoesNotExist e) {
                    // make parent directories, then retry
                    if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                        this._makeParentDirs();
                        ((DirectoryWriter)m_adaptor).makeDir(parent.getPath(), directoryName);
                    } else {
                        throw new DoesNotExist("Parent directory does not exist: "+parent, e.getCause());
                    }
                } catch(AlreadyExists e) {
                    if (effectiveFlags.contains(Flags.EXCL)) {
                        throw new AlreadyExists("Directory already exists: "+m_uri, e.getCause());
                    }
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme());
            }
        } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            this._makeParentDirs();
        } else if (! new FlagsContainer(flags).contains(ExtensionFlags.LATE_EXISTENCE_CHECK)) {
            if (m_adaptor instanceof DataReaderAdaptor && !((DataReaderAdaptor)m_adaptor).exists(m_uri.getPath())) {
                throw new DoesNotExist("Directory does not exist: "+m_uri);
            }
        }
    }

    /** constructor for deepCopy */
    protected AbstractNamespaceDirectoryImpl(AbstractNamespaceDirectoryImpl source) {
        super(source);
    }

    public void changeDir(URI dir) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (dir.getScheme()!=null || dir.getUserInfo()!=null || dir.getHost()!=null) {
            throw new IncorrectURL("Was expecting a absolute/relative path instead of: "+dir.toString());
        }
        m_uri = this._resolveRelativeURI(dir);
    }

    public List list(String pattern, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this._dereferenceDir().list(pattern, effectiveFlags.remove(Flags.DEREFERENCE));
        }
        boolean longFormat = effectiveFlags.contains(ExtensionFlags.LONG_FORMAT);
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE);
        if (m_adaptor instanceof DirectoryReader) {
            Pattern p = _toRegexp(pattern);
            List matchingNames = new ArrayList();
            // for each child
            FileAttributes[] childs = this._listAttributes(m_uri.getPath());
            for (int i=0; i<childs.length; i++) {
                if (p==null || p.matcher(childs[i].getName()).matches()) {
                    String line = (longFormat
                            ? childs[i].toString()
                            : childs[i].getName());
                    matchingNames.add(line);
                }
            }
            return matchingNames;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public List find(String pattern, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.RECURSIVE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this._dereferenceDir().find(pattern, effectiveFlags.remove(Flags.DEREFERENCE));
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE));
        if (m_adaptor instanceof DirectoryReader) {
            Pattern p = _toRegexp(pattern);
            List matchingPath = new ArrayList();
            // do find
            this._doFind(p, effectiveFlags, matchingPath, CURRENT_DIR);
            return matchingPath;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }
    private void _doFind(Pattern p, FlagsContainer effectiveFlags, List matchingPath, URI currentPath) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // for each child
        FileAttributes[] childs = this._listAttributes(m_uri.getPath());
        for (int i=0; i<childs.length; i++) {
            // set child path
            URI childPath;
            if (childs[i].getType() == FileAttributes.DIRECTORY_TYPE) {
                childPath = currentPath.resolve(childs[i].getName()+"/");
            } else {
                childPath = currentPath.resolve(childs[i].getName());
            }
            // add child path to matching list
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                matchingPath.add(childPath);
            }
            // may recurse
            if (effectiveFlags.contains(Flags.RECURSIVE) && childs[i].getType()==FileAttributes.DIRECTORY_TYPE) {
                AbstractNamespaceDirectoryImpl childDir = (AbstractNamespaceDirectoryImpl) this._openNSDir(childPath);
                childDir._doFind(p, effectiveFlags, matchingPath, childPath);
            }
        }
    }

    public boolean exists(URI name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return ((AbstractNamespaceEntryImpl)this._openNSEntry(name)).exists();
    }

    public boolean isDir(URI name, Flags flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name).isDirectory(flags);
    }

    public boolean isEntry(URI name, Flags flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name).isEntry(flags);
    }

    public boolean isLink(URI name, Flags flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name).isLink(flags);
    }

    public URI readLink(URI name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name).readLink();
    }

    private String[] m_entriesCache = null;
    public int getNumEntries() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof DirectoryReader) {
            m_entriesCache = this._listNames(m_uri.getPath());
            return m_entriesCache.length;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }
    public URI getEntry(int entry) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof DirectoryReader) {
            if (m_entriesCache == null) {
                m_entriesCache = this._listNames(m_uri.getPath());
            }
            if (entry < m_entriesCache.length) {
                try {
                    return new URI(m_entriesCache[entry]);
                } catch (URISyntaxException e) {
                    throw new NoSuccess(e);
                }
            } else {
                throw new DoesNotExist("Invalid index: "+entry, this);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void copy(URI source, URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).copy(target, flags);
    }

    public void link(URI source, URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).link(target, flags);
    }

    public void move(URI source, URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).move(target, flags);
    }

    public void remove(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(target).remove(flags);
    }

    public void makeDir(URI target, Flags flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        try {
            Flags createFlags = flags!=null ? flags.or(Flags.CREATE) : Flags.CREATE;
            NamespaceFactoryImpl.createNamespaceDirectory(m_session, target, createFlags);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        }
    }

    public abstract NamespaceDirectory openDir(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;
    public abstract NamespaceEntry openEntry(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess;

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    /**
     * Convert wildcards to a regular expression.
     * @param pattern file name with wildcards supported by SAGA.
     * @return a compiled regular expression.
     */
    protected static Pattern _toRegexp(String pattern) {
        if (pattern==null || pattern.equals("") || pattern.equals("*")) {
            return null;
        } else {
            // escape some characters
            String regexp = pattern;
            regexp = regexp.replaceAll("\\.", "\\\\.");
            // convert wildcards to regular expression
            regexp = regexp.replaceAll("\\*", ".*");
            regexp = regexp.replaceAll("\\?", ".?");
            regexp = regexp.replaceAll("\\[!", "[^");
            // compile regular expression
            return Pattern.compile(regexp);
        }
    }

    protected static URI CURRENT_DIR;
    static {
        try {
            CURRENT_DIR = new URI("./");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    protected URI _resolveRelativeURI(URI relativePath) throws IncorrectURL {
        if (relativePath==null) {
            throw new IncorrectURL("URI must not be null");
        } else if (relativePath.getScheme()!=null && !relativePath.getScheme().equals(m_uri.getScheme())) {
            throw new IncorrectURL("You must not modify the scheme of the URI: "+m_uri.getScheme());
        } else if (relativePath.getUserInfo()!=null && !relativePath.getUserInfo().equals(m_uri.getUserInfo())) {
            throw new IncorrectURL("You must not modify the user part of the URI: "+m_uri.getUserInfo());
        } else if (relativePath.getHost()!=null && !relativePath.getHost().equals(m_uri.getHost())) {
            throw new IncorrectURL("You must not modify the host of the URI: "+m_uri.getHost());
        }
        try {
            URI directoryUri = (m_uri.getPath().endsWith("/")
                    ? m_uri
                    : new URI(new java.net.URI(m_uri.getScheme(), m_uri.getUserInfo(), m_uri.getHost(), m_uri.getPort(), m_uri.getPath()+"/", m_uri.getQuery(), m_uri.getFragment())));
            return directoryUri.resolve(relativePath);
        } catch (URISyntaxException e) {
            throw new IncorrectURL(e);
        }
    }

    private String[] _listNames(String absolutePath) throws NotImplemented, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        FileAttributes[] files = this._listAttributes(absolutePath);
        String[] filenames = new String[files.length];
        for (int i=0; i<files.length; i++) {
            filenames[i] = files[i].getName();
        }
        return filenames;
    }
}
