package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.LogicalReaderMetaDataExtended;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryImplWithMetaData
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSDirectoryImplWithMetaData extends AbstractNSDirectoryImpl implements LogicalDirectory, AsyncAttributes<LogicalDirectory> {
    private MetaDataAttributesImpl<LogicalDirectory> m_metadatas;

    /** constructor for factory */
    protected AbstractNSDirectoryImplWithMetaData(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
        m_metadatas = new MetaDataAttributesImpl<LogicalDirectory>(m_session, this, m_url, m_adaptor);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSDirectoryImplWithMetaData(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
        m_metadatas = new MetaDataAttributesImpl<LogicalDirectory>(m_session, this, m_url, m_adaptor);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSDirectoryImplWithMetaData(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
        m_metadatas = new MetaDataAttributesImpl<LogicalDirectory>(m_session, this, m_url, m_adaptor);
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

    public Task<LogicalDirectory, Void> setAttribute(TaskMode mode, String key, String value) throws NotImplementedException {
        return m_metadatas.setAttribute(mode, key, value);
    }

    public Task<LogicalDirectory, String> getAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.getAttribute(mode, key);
    }

    public Task<LogicalDirectory, Void> setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplementedException {
        return m_metadatas.setVectorAttribute(mode, key, values);
    }

    public Task<LogicalDirectory, String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.getVectorAttribute(mode, key);
    }

    public Task<LogicalDirectory, Void> removeAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.removeAttribute(mode, key);
    }

    public Task<LogicalDirectory, String[]> listAttributes(TaskMode mode) throws NotImplementedException {
        return m_metadatas.listAttributes(mode);
    }

    public Task<LogicalDirectory, String[]> findAttributes(TaskMode mode, String... patterns) throws NotImplementedException {
        return m_metadatas.findAttributes(mode, patterns);
    }

    public Task<LogicalDirectory, Boolean> existsAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.existsAttribute(mode, key);
    }

    public Task<LogicalDirectory, Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isReadOnlyAttribute(mode, key);
    }

    public Task<LogicalDirectory, Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isWritableAttribute(mode, key);
    }

    public Task<LogicalDirectory, Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isRemovableAttribute(mode, key);
    }

    public Task<LogicalDirectory, Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        return m_metadatas.isVectorAttribute(mode, key);
    }

    ////////////////////////////////////// LogicialDirectoryImpl //////////////////////////////////////

    public String[] listAttributesRecursive(Map<String, String> keyValuePatterns) throws NotImplementedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaDataExtended) {
            return ((LogicalReaderMetaDataExtended) m_adaptor).listMetadataNames(
                    MetaDataAttributesImpl.getNormalizedPath(m_url),
                    keyValuePatterns,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Method listAttributesRecursive() is not supported for this protocol: "+m_url.getScheme());
        }
    }

    public String[] listAttributeValuesRecursive(String key, Map<String, String> keyValuePatterns) throws NotImplementedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaDataExtended) {
            return ((LogicalReaderMetaDataExtended) m_adaptor).listMetadataValues(
                    MetaDataAttributesImpl.getNormalizedPath(m_url),
                    key,
                    keyValuePatterns,
                    m_url.getQuery());
        } else {
            throw new NotImplementedException("Method listAttributeValuesRecursive() is not supported for this protocol: "+m_url.getScheme());
        }
    }
}
