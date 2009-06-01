package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.util.List;

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
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    public LogicalDirectoryImpl(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public LogicalDirectoryImpl(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    public boolean isFile(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, IncorrectStateException, TimeoutException, NoSuccessException {
        return super.isFileSync(name);
    }

    public List<URL> find(String namePattern, String[] attrPattern, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        return super.findSync(namePattern, attrPattern, flags);
    }

    public List<URL> find(String namePattern, String[] attrPattern) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        return super.findSync(namePattern, attrPattern);
    }

    public LogicalDirectory openLogicalDir(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openLogicalDir");
        if (timeout == WAIT_FOREVER) {
            return super.openLogicalDir(name, flags);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openLogicalDir");
        }
    }

    public LogicalDirectory openLogicalDir(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openLogicalDir");
        if (timeout == WAIT_FOREVER) {
            return super.openLogicalDir(name);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openLogicalDir");
        }
    }

    public LogicalFile openLogicalFile(URL name, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openLogicalFile");
        if (timeout == WAIT_FOREVER) {
            return super.openLogicalFile(name, flags);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openLogicalFile");
        }
    }

    public LogicalFile openLogicalFile(URL name) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        float timeout = this.getTimeout("openLogicalFile");
        if (timeout == WAIT_FOREVER) {
            return super.openLogicalFile(name);
        } else {
            throw new NotImplementedException("Configuring user timeout is not supported for method: openLogicalFile");
        }
    }

    ////////////////////////////////////////// private methods //////////////////////////////////////////

    private float getTimeout(String methodName) throws NoSuccessException {
        return getTimeout(LogicalDirectory.class, methodName, m_url.getScheme());
    }
}
