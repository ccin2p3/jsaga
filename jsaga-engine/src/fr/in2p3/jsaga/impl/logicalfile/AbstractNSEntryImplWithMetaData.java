package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;
import fr.in2p3.jsaga.helpers.SAGAPatternFinder;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.task.GenericThreadedTask;
import org.ogf.saga.url.URL;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;

import java.lang.Exception;
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
public abstract class AbstractNSEntryImplWithMetaData extends AbstractNSEntryImpl implements NSEntry, AsyncAttributes {
    /** constructor for factory */
    public AbstractNSEntryImplWithMetaData(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    public AbstractNSEntryImplWithMetaData(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    public AbstractNSEntryImplWithMetaData(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        super(entry, absolutePath, flags);
    }

    ////////////////////////////////////// interface Attributes //////////////////////////////////////

    public void setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriterMetaData) {
            ((LogicalWriterMetaData)m_adaptor).setMetaData(
                    m_url.getPath(),
                    key,
                    value,
                    m_url.getQuery());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            return ((LogicalReaderMetaData)m_adaptor).getMetaData(
                    m_url.getPath(),
                    key,
                    m_url.getQuery());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented");
    }

    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented");
    }

    public void removeAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriterMetaData) {
            ((LogicalWriterMetaData)m_adaptor).removeMetaData(
                    m_url.getPath(),
                    key,
                    m_url.getQuery());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);            
        }
    }

    public String[] listAttributes() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            Map<String,String> attributes = ((LogicalReaderMetaData)m_adaptor).listMetaData(
                    m_url.getPath(),
                    m_url.getQuery());
            return attributes.keySet().toArray(new String[attributes.size()]);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);            
        }
    }

    public String[] findAttributes(String... patterns) throws NotImplemented, BadParameter, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            Map<String,String> attributes = ((LogicalReaderMetaData)m_adaptor).listMetaData(
                    m_url.getPath(),
                    m_url.getQuery());
            return new SAGAPatternFinder(attributes).findKey(patterns);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_url.getScheme(), this);
        }
    }

    public boolean isReadOnlyAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented");
    }

    public boolean isWritableAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented");
    }

    public boolean isRemovableAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented");
    }

    public boolean isVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NotImplemented("Not implemented");
    }

    ////////////////////////////////////// interface AsyncAttributes //////////////////////////////////////

    public Task setAttribute(TaskMode mode, String key, String value) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImplWithMetaData.class.getMethod("setAttribute", new Class[]{String.class, String.class}),
                    new Object[]{key, value}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String> getAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImplWithMetaData.class.getMethod("getAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplemented {
        throw new NotImplemented("Not implemented");
    }

    public Task<String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplemented {
        throw new NotImplemented("Not implemented");
    }

    public Task removeAttribute(TaskMode mode, String key) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImplWithMetaData.class.getMethod("removeAttribute", new Class[]{String.class}),
                    new Object[]{key}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String[]> listAttributes(TaskMode mode) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImplWithMetaData.class.getMethod("listAttributes", new Class[]{}),
                    new Object[]{}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplemented {
        try {
            return prepareTask(mode, new GenericThreadedTask(
                    m_session,
                    this,
                    AbstractNSEntryImplWithMetaData.class.getMethod("findAttributes", new Class[]{String[].class}),
                    new Object[]{patterns}));
        } catch (Exception e) {
            throw new NotImplemented(e);
        }
    }

    public Task<Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplemented {
        throw new NotImplemented("Not implemented");
    }

    public Task<Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplemented {
        throw new NotImplemented("Not implemented");
    }

    public Task<Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplemented {
        throw new NotImplemented("Not implemented");
    }

    public Task<Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplemented {
        throw new NotImplemented("Not implemented");
    }
}
