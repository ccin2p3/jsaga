package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.MetaDataReader;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.impl.namespace.AbstractNamespaceEntryImpl;
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
public class LogicalDirectoryImpl extends AbstractLogicalDirectoryTaskImpl implements LogicalDirectory {
    /** constructor for factory */
    public LogicalDirectoryImpl(Session session, URI uri, DataAdaptor adaptor, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, adaptor, flags);
    }

    /** constructor for open() */
    public LogicalDirectoryImpl(AbstractNamespaceEntryImpl entry, URI uri, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, uri, flags);
    }

    /** constructor for deepCopy */
    protected LogicalDirectoryImpl(LogicalDirectoryImpl source) {
        super(source);
    }
    public SagaBase deepCopy() {
        return new LogicalDirectoryImpl(this);
    }

    public ObjectType getType() {
        return ObjectType.LOGICALDIRECTORY;
    }

    /** implements super.openDir() */
    public NamespaceDirectory openDir(URI name, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openLogicalDir(name, flags);
    }

    /** implements super.open() */
    public NamespaceEntry open(URI name, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return this.openLogicalFile(name, flags);
    }

    public boolean isFile(URI name, Flags... flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return super.isEntry(name, flags);
    }

    /** search in meta-data */
    public List<String> find(String namePattern, String[] keyPattern, String[] valPattern, Flags... flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(Flags.RECURSIVE, flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return ((LogicalDirectoryImpl)this._dereferenceDir()).find(namePattern, keyPattern, valPattern, effectiveFlags.remove(Flags.DEREFERENCE));
        }
        effectiveFlags.checkAllowed(Flags.RECURSIVE, Flags.DEREFERENCE);

        // convert meta-data keys and values to map
        int keyLen = (keyPattern!=null ? keyPattern.length : 0);
        int valLen = (valPattern!=null ? valPattern.length : 0);
        if (keyLen != valLen) {
            throw new BadParameter("Number of meta-data key and value must be the same: "+keyLen+"/"+valLen, this);
        }
        Map<String,String> keyValuePatterns = new HashMap<String,String>();
        for (int i=0; i<keyLen; i++) {
            keyValuePatterns.put(keyPattern[i], valPattern[i]);
        }

        // search
        List<String> matchingPath = new ArrayList<String>();
        if (keyValuePatterns.isEmpty()) {
            for (URI current : super.find(namePattern, flags)) {
                matchingPath.add(current.getPath());
            }
        } else if (m_adaptor instanceof MetaDataReader) {
            Pattern p = _toRegexp(namePattern);
            this._doFind(p, keyValuePatterns, effectiveFlags, matchingPath, CURRENT_DIR_RELATIVE_PATH);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
        return matchingPath;
    }
    private void _doFind(Pattern p, Map keyValuePatterns, FlagsBytes effectiveFlags, List<String> matchingPath, URI currentRelativePath) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // for each child
        FileAttributes[] childs;
        try {
            childs = ((MetaDataReader)m_adaptor).listAttributes(m_uri.getPath(), keyValuePatterns);
        } catch (DoesNotExist doesNotExist) {
            throw new IncorrectState("Logical directory does not exist: "+m_uri, doesNotExist);
        }
        for (int i=0; i<childs.length; i++) {
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                // add child relative path
                URI childRelativePath = currentRelativePath.resolve(childs[i].getName());
                matchingPath.add(childRelativePath.getPath());
                // may recurse
                if (effectiveFlags.contains(Flags.RECURSIVE) && childs[i].getType()==FileAttributes.DIRECTORY_TYPE) {
                    LogicalDirectoryImpl childDir = (LogicalDirectoryImpl) this._openNSDir(childRelativePath);
                    childDir._doFind(p, keyValuePatterns, effectiveFlags, matchingPath, childRelativePath);
                }
            }
        }
    }

    public LogicalDirectory openLogicalDir(URI relativePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalDirectoryImpl(this, super._resolveRelativeURI(relativePath), flags);
    }

    public LogicalFile openLogicalFile(URI relativePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalFileImpl(this, super._resolveRelativeURI(relativePath), flags);
    }
}
