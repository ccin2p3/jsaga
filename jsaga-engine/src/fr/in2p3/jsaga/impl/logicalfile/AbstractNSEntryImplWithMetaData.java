package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;
import fr.in2p3.jsaga.helpers.SAGAPatternFinder;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTaskFactory;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryImplWithMetaData
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSEntryImplWithMetaData extends AbstractNSEntryImpl implements LogicalFile, AsyncAttributes<LogicalFile> {
    /** constructor for factory */
    protected AbstractNSEntryImplWithMetaData(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSEntryImplWithMetaData(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSEntryImplWithMetaData(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    ////////////////////////////////////// interface Attributes //////////////////////////////////////

    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalWriterMetaData) {
            ((LogicalWriterMetaData)m_adaptor).setMetaData(
                    m_url.getPath(),
                    key,
                    value,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            return ((LogicalReaderMetaData)m_adaptor).getMetaData(
                    m_url.getPath(),
                    key,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public void removeAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalWriterMetaData) {
            ((LogicalWriterMetaData)m_adaptor).removeMetaData(
                    m_url.getPath(),
                    key,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String[] listAttributes() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            Map<String,String> attributes = ((LogicalReaderMetaData)m_adaptor).listMetaData(
                    m_url.getPath(),
                    m_url.getQuery());
            return attributes.keySet().toArray(new String[attributes.size()]);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String[] findAttributes(String... patterns) throws NotImplementedException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            Map<String,String> attributes = ((LogicalReaderMetaData)m_adaptor).listMetaData(
                    m_url.getPath(),
                    m_url.getQuery());
            return new SAGAPatternFinder(attributes).findKey(patterns);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+m_url.getScheme(), this);
        }
    }

    public boolean isReadOnlyAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public boolean isWritableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public boolean isRemovableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public boolean isVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    ////////////////////////////////////// interface AsyncAttributes //////////////////////////////////////

    public Task<LogicalFile, Void> setAttribute(TaskMode mode, String key, String value) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,Void>().create(
                mode, m_session, this,
                "setAttribute",
                new Class[]{String.class, String.class},
                new Object[]{key, value});
    }

    public Task<LogicalFile, String> getAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,String>().create(
                mode, m_session, this,
                "getAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<LogicalFile, Void> setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<LogicalFile, String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<LogicalFile, Void> removeAttribute(TaskMode mode, String key) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,Void>().create(
                mode, m_session, this,
                "removeAttribute",
                new Class[]{String.class},
                new Object[]{key});
    }

    public Task<LogicalFile, String[]> listAttributes(TaskMode mode) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,String[]>().create(
                mode, m_session, this,
                "listAttributes",
                new Class[]{},
                new Object[]{});
    }

    public Task<LogicalFile, String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplementedException {
        return new GenericThreadedTaskFactory<LogicalFile,String[]>().create(
                mode, m_session, this,
                "findAttributes",
                new Class[]{String[].class},
                new Object[]{patterns});
    }

    public Task<LogicalFile, Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<LogicalFile, Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<LogicalFile, Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<LogicalFile, Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }
}
