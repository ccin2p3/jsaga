package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.helpers.URLFactory;
import org.ogf.saga.ObjectType;
import org.ogf.saga.URL;
import org.ogf.saga.attributes.Attributes;
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
public abstract class AbstractNSDirectoryImpl extends AbstractNSDirectoryTaskImpl implements NSDirectory, Attributes {
    /** constructor for factory */
    public AbstractNSDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, URLFactory.toDirectoryURL(url), adaptor, flags);
        this.init(flags);
    }

    /** constructor for open() */
    public AbstractNSDirectoryImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, URLFactory.toDirectoryURL(url), flags);
        this.init(flags);
    }

    private void init(int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
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
    public List<String> listWithLongFormat(String pattern, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        return this._list(pattern, true, flags);
    }
    private List _list(String pattern, boolean longFormat, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return this._dereferenceDir()._list(pattern, longFormat, effectiveFlags.remove(Flags.DEREFERENCE));
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.getValue());
        if (m_adaptor instanceof DirectoryReader) {
            Pattern p = _toRegexp(pattern);
            List<Object> matchingNames = new ArrayList<Object>();
            // for each child
            FileAttributes[] childs = this._listAttributes(m_url.getPath());
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
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
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
            Pattern p = _toRegexp(pattern);
            this._doFind(p, effectiveFlags, matchingPath, CURRENT_DIR_RELATIVE_PATH);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        return matchingPath;
    }
    private void _doFind(Pattern p, FlagsBytes effectiveFlags, List<URL> matchingPath, URL currentRelativePath) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // for each child
        FileAttributes[] childs = this._listAttributes(m_url.getPath());
        for (int i=0; i<childs.length; i++) {
            // set child relative path
            URL childRelativePath;
            if (childs[i].getType() == FileAttributes.DIRECTORY_TYPE) {
                childRelativePath = URLFactory.createURL(currentRelativePath, childs[i].getName()+"/");
            } else {
                childRelativePath = URLFactory.createURL(currentRelativePath, childs[i].getName());
            }
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

    public boolean isDir(URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name, flags).isDir();
    }

    public boolean isEntry(URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name, flags).isEntry();
    }

    public boolean isLink(URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name, flags).isLink();
    }

    public URL readLink(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this._openNSEntry(name).readLink();
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

    public void link(URL source, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).link(target, flags);
    }

    public void move(URL source, URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(source).move(target, flags);
    }

    public void remove(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectURL, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        this._openNSEntry(target).remove(flags);
    }

    public void makeDir(URL target, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        int makeDirFlags = Flags.CREATE.or(flags);
        NSFactory.createNSDirectory(m_session, target, makeDirFlags);
    }

    public void permissionsAllow(URL target, String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, Timeout, NoSuccess {
        try {
            this._openNSEntry(target, flags).permissionsAllow(id, permissions);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
    }

    public void permissionsDeny(URL target, String id, int permissions, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        try {
            this._openNSEntry(target, flags).permissionsDeny(id, permissions);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectState e) {
            throw new NoSuccess(e);
        }
    }

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

    private NSEntry _openNSEntry(URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        try {
            return this.open(name, flags);
        } catch (DoesNotExist e) {
            throw new IncorrectState(e);
        } catch (AlreadyExists e) {
            throw new IncorrectState(e);
        }
    }
}
