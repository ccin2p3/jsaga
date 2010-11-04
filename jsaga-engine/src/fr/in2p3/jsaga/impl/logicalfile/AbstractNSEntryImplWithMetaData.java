package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

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
    private MetaDataAttributesImpl<LogicalFile> m_metadatas;

    /** constructor for factory */
    protected AbstractNSEntryImplWithMetaData(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
        m_metadatas = new MetaDataAttributesImpl<LogicalFile>(m_session, this, m_url, m_adaptor);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSEntryImplWithMetaData(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
        m_metadatas = new MetaDataAttributesImpl<LogicalFile>(m_session, this, m_url, m_adaptor);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSEntryImplWithMetaData(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
        m_metadatas = new MetaDataAttributesImpl<LogicalFile>(m_session, this, m_url, m_adaptor);
    }

    ////////////////////////////////////// interface Attributes //////////////////////////////////////

    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_metadatas.setAttribute(key, value);
    }

    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_metadatas.getAttribute(key);
    }

    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_metadatas.setVectorAttribute(key, values);
    }

    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_metadatas.getVectorAttribute(key);
    }

    public void removeAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        m_metadatas.removeAttribute(key);
    }

    public String[] listAttributes() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_metadatas.listAttributes();
    }

    public String[] findAttributes(String... patterns) throws NotImplementedException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_metadatas.findAttributes(patterns);
    }

    public boolean existsAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        return m_metadatas.existsAttribute(key);
    }

    public boolean isReadOnlyAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_metadatas.isReadOnlyAttribute(key);
    }

    public boolean isWritableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_metadatas.isWritableAttribute(key);
    }

    public boolean isRemovableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_metadatas.isRemovableAttribute(key);
    }

    public boolean isVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return m_metadatas.isVectorAttribute(key);
    }

    ////////////////////////////////////// interface AsyncAttributes //////////////////////////////////////

    public Task<LogicalFile, Void> setAttribute(TaskMode mode, String key, String value) throws NotImplementedException {
        return m_metadatas.setAttribute(mode, key, value);
    }

    public Task<LogicalFile, String> getAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.getAttribute(mode, key);
    }

    public Task<LogicalFile, Void> setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplementedException {
        return m_metadatas.setVectorAttribute(mode, key, values);
    }

    public Task<LogicalFile, String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.getVectorAttribute(mode, key);
    }

    public Task<LogicalFile, Void> removeAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.removeAttribute(mode, key);
    }

    public Task<LogicalFile, String[]> listAttributes(TaskMode mode) throws NotImplementedException {
        return m_metadatas.listAttributes(mode);
    }

    public Task<LogicalFile, String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplementedException {
        return m_metadatas.findAttributes(mode, patterns);
    }

    public Task<LogicalFile, Boolean> existsAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.existsAttribute(mode, key);
    }

    public Task<LogicalFile, Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isReadOnlyAttribute(mode, key);
    }

    public Task<LogicalFile, Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isWritableAttribute(mode, key);
    }

    public Task<LogicalFile, Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isRemovableAttribute(mode, key);
    }

    public Task<LogicalFile, Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isVectorAttribute(mode, key);
    }
}
