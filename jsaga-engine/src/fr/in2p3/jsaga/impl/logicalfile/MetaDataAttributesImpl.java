package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;
import fr.in2p3.jsaga.helpers.SAGAPatternFinder;
import fr.in2p3.jsaga.impl.task.AbstractThreadedTask;
import org.ogf.saga.attributes.AsyncAttributes;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.Task;
import org.ogf.saga.task.TaskMode;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetaDataAttributesImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 mars 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MetaDataAttributesImpl<T extends NSEntry> implements AsyncAttributes<T> {
    private Session m_session;
    private T m_object;
    private URL m_url;
    private DataAdaptor m_adaptor;

    private Map<String,String[]> m_cache;
    private long m_cacheTimestamp;

    public MetaDataAttributesImpl(Session session, T object, URL url, DataAdaptor adaptor) {
        m_session = session;
        m_object = object;
        m_url = url;
        m_adaptor = adaptor;
    }

    private void _invalidateCache() {
        m_cacheTimestamp = 0;
    }
    private void _refreshCache() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        m_cache = ((LogicalReaderMetaData)m_adaptor).listMetaData(
                getNormalizedPath(m_url),
                m_url.getQuery());
        m_cacheTimestamp = System.currentTimeMillis();
    }
    
    //////////////////////////////////////// interface Attributes /////////////////////////////////////////

    public synchronized void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.setVectorAttribute(key, new String[]{value});
    }

    public synchronized String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        String[] values = this.getVectorAttribute(key);
        switch (values.length) {
            case 0:
                throw new DoesNotExistException("Metadata does not exist: "+key);
            case 1:
                return values[0];
            default:
                throw new IncorrectStateException("Attribute is not scalar: "+key);
        }
    }

    public synchronized void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalWriterMetaData) {
            ((LogicalWriterMetaData)m_adaptor).setMetaData(
                    getNormalizedPath(m_url),
                    key,
                    values,
                    m_url.getQuery());
            this._invalidateCache();
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), m_object);
        }
    }

    public synchronized String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            if (m_cache==null || System.currentTimeMillis() - m_cacheTimestamp > 10000) {
                this._refreshCache();
            }
            String[] values = m_cache.get(key);
            if (values!=null && values.length>0) {
                return values;
            } else {
                throw new DoesNotExistException("Metadata does not exist: "+key);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), m_object);
        }
    }

    public synchronized void removeAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalWriterMetaData) {
            ((LogicalWriterMetaData)m_adaptor).removeMetaData(
                    getNormalizedPath(m_url),
                    key,
                    m_url.getQuery());
            this._invalidateCache();
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), m_object);
        }
    }

    public synchronized String[] listAttributes() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            this._refreshCache();
            return m_cache.keySet().toArray(new String[m_cache.size()]);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+ m_url.getScheme(), m_object);
        }
    }

    public synchronized String[] findAttributes(String... patterns) throws NotImplementedException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_adaptor instanceof LogicalReaderMetaData) {
            this._refreshCache();
            return new SAGAPatternFinder(m_cache).findKey(patterns);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+m_url.getScheme(), m_object);
        }
    }

    public synchronized boolean isReadOnlyAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public synchronized boolean isWritableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public synchronized boolean isRemovableAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        throw new NotImplementedException("Not implemented");
    }

    public synchronized boolean isVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        String[] values;
        try {
            values = this.getVectorAttribute(key);
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        }
        switch (values.length) {
            case 0:
                throw new DoesNotExistException("Metadata does not exist: "+key);
            case 1:
                return false;
            default:
                return true;
        }
    }

    ////////////////////////////////////// interface AsyncAttributes //////////////////////////////////////

    public Task<T, Void> setAttribute(TaskMode mode, final String key, final String value) throws NotImplementedException {
        return new AbstractThreadedTask<T,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                MetaDataAttributesImpl.this.setAttribute(key, value);
                return null;
            }
        };
    }

    public Task<T, String> getAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,String>(mode) {
            public String invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return MetaDataAttributesImpl.this.getAttribute(key);
            }
        };
    }

    public Task<T, Void> setVectorAttribute(TaskMode mode, String key, String[] values) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<T, String[]> getVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<T, Void> removeAttribute(TaskMode mode, final String key) throws NotImplementedException {
        return new AbstractThreadedTask<T,Void>(mode) {
            public Void invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                MetaDataAttributesImpl.this.removeAttribute(key);
                return null;
            }
        };
    }

    public Task<T, String[]> listAttributes(TaskMode mode) throws NotImplementedException {
        return new AbstractThreadedTask<T,String[]>(mode) {
            public String[] invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return MetaDataAttributesImpl.this.listAttributes();
            }
        };
    }

    public Task<T, String[]> findAttributes(TaskMode mode, final String... patterns) throws NotImplementedException {
        return new AbstractThreadedTask<T,String[]>(mode) {
            public String[] invoke() throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException {
                return MetaDataAttributesImpl.this.findAttributes(patterns);
            }
        };
    }

    public Task<T, Boolean> isReadOnlyAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<T, Boolean> isWritableAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<T, Boolean> isRemovableAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    public Task<T, Boolean> isVectorAttribute(TaskMode mode, String key) throws NotImplementedException {
        throw new NotImplementedException("Not implemented");
    }

    ////////////////////////////////////// private methods //////////////////////////////////////

    static String getNormalizedPath(URL url) {
        String path = url.getPath();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        if (path.equals("")) {
            return "/";
        } else {
            return path;
        }
    }
}
