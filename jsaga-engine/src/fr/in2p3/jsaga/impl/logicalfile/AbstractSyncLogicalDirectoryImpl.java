package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.helpers.SAGAPattern;
import fr.in2p3.jsaga.impl.namespace.*;
import fr.in2p3.jsaga.impl.url.URLFactoryImpl;
import fr.in2p3.jsaga.impl.url.URLHelper;
import fr.in2p3.jsaga.sync.logicalfile.SyncLogicalDirectory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.util.*;
import java.util.regex.Pattern;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractSyncLogicalDirectoryImpl
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractSyncLogicalDirectoryImpl extends AbstractNSDirectoryImplWithMetaData implements SyncLogicalDirectory {
    /** constructor for factory */
    public AbstractSyncLogicalDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
    }

    /** constructor for NSDirectory.open() */
    public AbstractSyncLogicalDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
    }

    /** constructor for NSEntry.openAbsolute() */
    public AbstractSyncLogicalDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, new FlagsHelper(flags).keep(JSAGAFlags.BYPASSEXIST, Flags.ALLNAMESPACEFLAGS));
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /////////////////////////////// class AbstractNSEntryImpl ///////////////////////////////

    /** timeout not supported */
    public NSDirectory openAbsoluteDir(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalDirectoryImpl(this, absolutePath, flags);
    }

    /** timeout not supported */
    public NSEntry openAbsolute(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(absolutePath)) {
            return new LogicalDirectoryImpl(this, absolutePath, flags);
        } else {
            return new LogicalFileImpl(this, absolutePath, flags);
        }
    }

    /////////////////////////////////// interface NSEntry ///////////////////////////////////

    /** timeout not supported */
    public NSDirectory openDir(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.openLogicalDir(name, flags);
    }
    /** timeout not supported */
    public NSDirectory openDir(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.openLogicalDir(name);
    }

    /** timeout not supported */
    public NSEntry open(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(name)) {
            return this.openLogicalDir(name, flags);
        } else {
            return this.openLogicalFile(name, flags);
        }
    }
    /** timeout not supported */
    public NSEntry open(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(name)) {
            return this.openLogicalDir(name);
        } else {
            return this.openLogicalFile(name);
        }
    }

    /////////////////////////////// interface LogicalDirectory ///////////////////////////////

    public boolean isFileSync(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return super.isEntrySync(name);
    }

    /** search in meta-data */
    public List<URL> findSync(String namePattern, String[] attrPattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                AbstractSyncLogicalDirectoryImpl entry = (AbstractSyncLogicalDirectoryImpl) this._dereferenceDir();
                try {
                    return entry.findSync(namePattern, attrPattern, flags - Flags.DEREFERENCE.getValue());
                } finally {
                    entry.close();
                }
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
        }

        // convert meta-data keys and values to map
        Map<String,String> keyValuePatterns = new HashMap<String,String>();
        for (int i=0; attrPattern!=null && i<attrPattern.length; i++) {
            if (attrPattern[i] != null) {
                int pos = attrPattern[i].indexOf('=');
                switch (pos) {
                    case -1:
                        throw new BadParameterException("Missing separator '=' in attribute pattern: "+attrPattern[i], this);
                    case 0:
                        throw new BadParameterException("Missing name in attribute pattern: "+attrPattern[i], this);
                    default:
                        String key = attrPattern[i].substring(0, pos);
                        String value = attrPattern[i].substring(pos+1);
                        keyValuePatterns.put(key, value);
                        break;
                }
            }
        }

        // search
        List<URL> matchingPath = new ArrayList<URL>();
        if (keyValuePatterns.isEmpty()) {
            return super.findSync(namePattern, flags);
        } else if (m_adaptor instanceof LogicalReaderMetaData) {
            // filter by meta-data
            FileAttributes[] childs;
            try {
                childs = ((LogicalReaderMetaData)m_adaptor).findAttributes(
                        MetaDataAttributesImpl.getNormalizedPath(m_url),
                        keyValuePatterns,
                        Flags.RECURSIVE.isSet(flags),
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Logical directory does not exist: "+ m_url, doesNotExist);
            }
            // filter by entry name
            Pattern p = SAGAPattern.toRegexp(namePattern);
            for (int i=0; i<childs.length; i++) {
                if (p==null || p.matcher(childs[i].getName()).matches()) {
                    URL childUrl = URLFactoryImpl.createURLWithCache(childs[i]);
                    matchingPath.add(childUrl);
                }
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        return matchingPath;
    }
    public List<URL> findSync(String namePattern, String[] attrPattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        return this.findSync(namePattern, attrPattern, Flags.RECURSIVE.getValue());
    }

    /** timeout not supported */
    public LogicalDirectory openLogicalDir(URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalDirectoryImpl(this, relativeUrl, flags);
    }
    /** timeout not supported */
    public LogicalDirectory openLogicalDir(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalDirectoryImpl(this, relativeUrl, Flags.READ.getValue());
    }

    /** timeout not supported */
    public LogicalFile openLogicalFile(URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalFileImpl(this, relativeUrl, flags);
    }
    /** timeout not supported */
    public LogicalFile openLogicalFile(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalFileImpl(this, relativeUrl, Flags.READ.getValue());
    }
}
