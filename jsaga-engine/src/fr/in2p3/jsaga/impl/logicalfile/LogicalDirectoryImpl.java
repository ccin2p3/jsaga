package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.helpers.SAGAPattern;
import fr.in2p3.jsaga.impl.namespace.*;
import fr.in2p3.jsaga.impl.url.URLHelper;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.logicalfile.LogicalFile;
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
    public LogicalDirectoryImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, new FlagsHelper(flags).remove(Flags.ALLLOGICALFILEFLAGS));
    }

    /** constructor for NSDirectory.open() */
    public LogicalDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, new FlagsHelper(flags).remove(Flags.ALLLOGICALFILEFLAGS));
    }

    /** constructor for NSEntry.openAbsolute() */
    public LogicalDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, new FlagsHelper(flags).remove(Flags.ALLLOGICALFILEFLAGS));
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /////////////////////////////// class AbstractNSEntryImpl ///////////////////////////////

    public NSDirectory openAbsoluteDir(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalDirectoryImpl(this, absolutePath, flags);
    }

    public NSEntry openAbsolute(String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(absolutePath)) {
            return new LogicalDirectoryImpl(this, absolutePath, flags);
        } else {
            return new LogicalFileImpl(this, absolutePath, flags);
        }
    }

    /////////////////////////////////// interface NSEntry ///////////////////////////////////

    public NSDirectory openDir(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.openLogicalDir(name, flags);
    }
    public NSDirectory openDir(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return this.openLogicalDir(name);
    }

    public NSEntry open(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(name)) {
            return this.openLogicalDir(name, flags);
        } else {
            return this.openLogicalFile(name, flags);
        }
    }
    public NSEntry open(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (URLHelper.isDirectory(name)) {
            return this.openLogicalDir(name);
        } else {
            return this.openLogicalFile(name);
        }
    }

    /////////////////////////////// interface LogicalDirectory ///////////////////////////////

    public boolean isFile(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return super.isEntry(name);
    }

    /** search in meta-data */
    public List<URL> find(String namePattern, String[] attrPattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        new FlagsHelper(flags).allowed(Flags.DEREFERENCE, Flags.RECURSIVE);
        if (Flags.DEREFERENCE.isSet(flags)) {
            try {
                return ((LogicalDirectoryImpl)this._dereferenceDir()).find(namePattern, attrPattern, flags - Flags.DEREFERENCE.getValue());
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            }
        }

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
        } else if (m_adaptor instanceof LogicalReaderMetaData) {
            // filter by meta-data
            FileAttributes[] childs;
            try {
                childs = ((LogicalReaderMetaData)m_adaptor).findAttributes(
                        m_url.getPath(),
                        keyValuePatterns,
                        Flags.RECURSIVE.isSet(flags),
                        m_url.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Logical directory does not exist: "+ m_url, doesNotExist);
            }
            // filter by entry name
            Pattern p = SAGAPattern.toRegexp(namePattern);
            for (int i=0; i<childs.length; i++) {
                if (p==null || p.matcher(childs[i].getNameOnly()).matches()) {
                    matchingPath.add(URLFactory.createURL(childs[i].getName()));
                }
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
        return matchingPath;
    }
    public List<URL> find(String namePattern, String[] attrPattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        return this.find(namePattern, attrPattern, Flags.RECURSIVE.getValue());
    }

    public LogicalDirectory openLogicalDir(URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalDirectoryImpl(this, relativeUrl, flags);
    }
    public LogicalDirectory openLogicalDir(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalDirectoryImpl(this, relativeUrl, Flags.READ.getValue());
    }

    public LogicalFile openLogicalFile(URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalFileImpl(this, relativeUrl, flags);
    }
    public LogicalFile openLogicalFile(URL relativeUrl) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new LogicalFileImpl(this, relativeUrl, Flags.READ.getValue());
    }
}
