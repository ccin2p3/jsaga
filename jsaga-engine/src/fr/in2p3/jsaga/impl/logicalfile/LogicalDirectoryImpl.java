package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.MetaDataReader;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.helpers.SAGAPattern;
import fr.in2p3.jsaga.helpers.URLFactory;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.ogf.saga.*;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.util.*;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalDirectoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalDirectoryImpl extends AbstractAsyncLogicalDirectoryImpl implements LogicalDirectory {
    /** constructor for factory */
    public LogicalDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for open() */
    public LogicalDirectoryImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, url, flags);
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public ObjectType getType() {
        return ObjectType.LOGICALDIRECTORY;
    }

    /** implements super.openDir() */
    public NSDirectory openDir(URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openLogicalDir(name, flags);
    }
    public NSDirectory openDir(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openDir(name, Flags.READ.getValue());
    }

    /** implements super.open() */
    public NSEntry open(URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (URLFactory.isDirectory(name)) {
            return this.openLogicalDir(name, flags);
        } else {
            return this.openLogicalFile(name, flags);
        }
    }
    public NSEntry open(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.open(name, Flags.READ.getValue());
    }

    public boolean isFile(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        return super.isEntry(name);
    }

    /** search in meta-data */
    public List<URL> find(String namePattern, String[] attrPattern, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            try {
                return ((LogicalDirectoryImpl)this._dereferenceDir()).find(namePattern, attrPattern, effectiveFlags.remove(Flags.DEREFERENCE));
            } catch (IncorrectURL e) {
                throw new NoSuccess(e);
            }
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE));

        // convert meta-data keys and values to map
        Map<String,String> keyValuePatterns = new HashMap<String,String>();
        for (int i=0; attrPattern!=null && i<attrPattern.length; i++) {
            String[] pair = attrPattern[i].split("=");
            String key = pair[0];
            String value = pair[1];
            keyValuePatterns.put(key, value);
        }

        // search
        List<URL> matchingPath = new ArrayList<URL>();
        if (keyValuePatterns.isEmpty()) {
            for (URL current : super.find(namePattern, flags)) {
                matchingPath.add(current);
            }
        } else if (m_adaptor instanceof MetaDataReader) {
            Pattern p = SAGAPattern.toRegexp(namePattern);
            this._doFind(p, keyValuePatterns, effectiveFlags, matchingPath, CURRENT_DIR_RELATIVE_PATH);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        return matchingPath;
    }
    public List<URL> find(String namePattern, String[] attrPattern) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return this.find(namePattern, attrPattern, Flags.RECURSIVE.getValue());
    }

    private void _doFind(Pattern p, Map keyValuePatterns, FlagsBytes effectiveFlags, List<URL> matchingPath, URL currentRelativePath) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // for each child
        FileAttributes[] childs;
        try {
            childs = ((MetaDataReader)m_adaptor).listAttributes(
                    m_url.getPath(),
                    keyValuePatterns,
                    m_url.getQuery());
        } catch (DoesNotExist doesNotExist) {
            throw new IncorrectState("Logical directory does not exist: "+ m_url, doesNotExist);
        }
        for (int i=0; i<childs.length; i++) {
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                // add child relative path
                URL childRelativePath = URLFactory.createURL(currentRelativePath, childs[i].getName());
                matchingPath.add(childRelativePath);
                // may recurse
                if (effectiveFlags.contains(Flags.RECURSIVE) && childs[i].getType()==FileAttributes.DIRECTORY_TYPE) {
                    LogicalDirectoryImpl childDir;
                    try {
                        childDir = (LogicalDirectoryImpl) this._openNSDir(childRelativePath);
                    } catch (IncorrectURL e) {
                        throw new NoSuccess(e);
                    }
                    childDir._doFind(p, keyValuePatterns, effectiveFlags, matchingPath, childRelativePath);
                }
            }
        }
    }

    public LogicalDirectory openLogicalDir(URL relativePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalDirectoryImpl(this, super._resolveRelativeURL(relativePath), flags);
    }
    public LogicalDirectory openLogicalDir(URL relativePath) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openLogicalDir(relativePath, Flags.READ.getValue());
    }

    public LogicalFile openLogicalFile(URL relativePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalFileImpl(this, super._resolveRelativeURL(relativePath), flags);
    }
    public LogicalFile openLogicalFile(URL relativePath) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openLogicalFile(relativePath, Flags.READ.getValue());
    }
}
