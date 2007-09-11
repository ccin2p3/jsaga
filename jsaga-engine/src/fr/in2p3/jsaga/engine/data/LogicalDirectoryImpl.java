package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.MetaDataReader;
import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
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
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalDirectoryImpl extends AbstractNamespaceDirectoryImpl implements LogicalDirectory {
    /** constructor */
    public LogicalDirectoryImpl(Session session, URI uri, LogicalEntryFlags flags, DataConnection connection) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, connection);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, PhysicalEntryFlags.READ);
        effectiveFlags.keepLogicalEntryFlags();
    }

    /** constructor for deepCopy */
    protected LogicalDirectoryImpl(AbstractNamespaceDirectoryImpl source) {
        super(source);
    }
    public SagaBase deepCopy() {
        return new LogicalDirectoryImpl(this);
    }

    public boolean isFile(URI name, LogicalEntryFlags flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        return super.isEntry(name, flags);
    }

    /** search in meta-data */
    public List find(String namePattern, String[] keyPattern, String[] valPattern, LogicalEntryFlags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, LogicalEntryFlags.RECURSIVE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            return ((LogicalDirectoryImpl)this._dereferenceDir()).find(namePattern, keyPattern, valPattern, PhysicalEntryFlags.cast(effectiveFlags.remove(Flags.DEREFERENCE)));
        }
        effectiveFlags.keepLogicalEntryFlags();
        effectiveFlags.checkAllowed(Flags.RECURSIVE.or(Flags.DEREFERENCE));

        // convert meta-data keys and values to map
        int keyLen = (keyPattern!=null ? keyPattern.length : 0);
        int valLen = (valPattern!=null ? valPattern.length : 0);
        if (keyLen != valLen) {
            throw new BadParameter("Number of meta-data key and value must be the same: "+keyLen+"/"+valLen, this);
        }
        Map keyValuePatterns = new HashMap();
        for (int i=0; i<keyLen; i++) {
            keyValuePatterns.put(keyPattern[i], valPattern[i]);
        }

        // search
        if (keyValuePatterns.isEmpty()) {
            return super.find(namePattern, flags);
        } else if (m_adaptor instanceof MetaDataReader) {
            Pattern p = _toRegexp(namePattern);
            List matchingPath = new ArrayList();
            this._doFind(p, keyValuePatterns, effectiveFlags, matchingPath, CURRENT_DIR);
            return matchingPath;
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }
    private void _doFind(Pattern p, Map keyValuePatterns, FlagsContainer effectiveFlags, List matchingPath, URI currentPath) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        // for each child
        FileAttributes[] childs;
        try {
            childs = ((MetaDataReader)m_adaptor).listAttributes(m_uri.getPath(), keyValuePatterns);
        } catch (DoesNotExist doesNotExist) {
            throw new IncorrectState("Logical directory does not exist: "+m_uri, doesNotExist);
        }
        for (int i=0; i<childs.length; i++) {
            if (p==null || p.matcher(childs[i].getName()).matches()) {
                // add relative path
                URI childPath = currentPath.resolve(childs[i].getName());
                matchingPath.add(childPath);
                // may recurse
                if (effectiveFlags.contains(Flags.RECURSIVE) && childs[i].getType()==FileAttributes.DIRECTORY_TYPE) {
                    LogicalDirectoryImpl childDir = (LogicalDirectoryImpl) this._openNSDir(childPath);
                    childDir._doFind(p, keyValuePatterns, effectiveFlags, matchingPath, childPath);
                }
            }
        }
    }

    public NamespaceDirectory openDir(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalDirectoryImpl(m_session, super._resolveRelativeURI(name), PhysicalEntryFlags.cast(flags), m_connection);
    }

    public NamespaceEntry openEntry(URI name, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalFileImpl(m_session, super._resolveRelativeURI(name), PhysicalEntryFlags.cast(flags), m_connection);
    }
}
