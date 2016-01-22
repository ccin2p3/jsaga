package fr.in2p3.jsaga.impl.resource.manager;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.impl.resource.description.ComputeDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.description.NetworkDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.description.StorageDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.instance.ComputeImpl;
import fr.in2p3.jsaga.impl.resource.instance.NetworkImpl;
import fr.in2p3.jsaga.impl.resource.instance.StorageImpl;
import fr.in2p3.jsaga.impl.resource.task.StateListener;
import fr.in2p3.jsaga.sync.resource.SyncResourceManager;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.instance.Storage;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public abstract class AbstractSyncResourceManagerImpl extends AbstractSagaObjectImpl implements SyncResourceManager, StateListener {
    protected URL m_url;
    protected ResourceAdaptor m_adaptor;

    public AbstractSyncResourceManagerImpl(Session session, URL rm, ResourceAdaptor adaptor) {
        super(session);
        m_url = rm;
        m_adaptor = adaptor;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractSyncResourceManagerImpl clone = (AbstractSyncResourceManagerImpl) super.clone();
        clone.m_url = m_url;
        clone.m_adaptor = m_adaptor;
        return clone;
    }

    //----------------------------------------------------------------

    public List<String> listResourcesSync(Type type) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException,
            NoSuccessException {
        String[] array = m_adaptor.listResources();
        List<String> list = new ArrayList<String>();
        for (int i=0; array!=null && i<array.length; i++) {
            String sagaResourceId = "["+m_url.getString()+"]-["+array[i]+"]";
            list.add(sagaResourceId);
        }
        return list;
    }

    public List<String> listTemplatesSync(Type type) throws NotImplementedException,
            TimeoutException, NoSuccessException {
        String[] array = m_adaptor.listTemplates();
        List<String> list = new ArrayList<String>();
        for (int i=0; array!=null && i<array.length; i++) {
            String sagaTemplateId = "["+m_url.getString()+"]-["+array[i]+"]";
            list.add(sagaTemplateId);
        }
        return list;
    }
    public ResourceDescription getTemplateSync(String id) throws NotImplementedException,
            BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        Properties properties = m_adaptor.getTemplate(id);
        String typeString = properties.getProperty(Resource.RESOURCE_TYPE);
        if (typeString != null) {
            Type type = Type.valueOf(typeString);
            switch (type) {
                case COMPUTE:
                    return new ComputeDescriptionImpl(properties);
                case NETWORK:
                    return new NetworkDescriptionImpl(properties);
                case STORAGE:
                    return new StorageDescriptionImpl(properties);
                default:
                    throw new BadParameterException("Unexpected resource type: "+type.name());
            }
        } else {
            throw new BadParameterException("Template is missing required property: "+Resource.RESOURCE_TYPE);
        }
    }

    //----------------------------------------------------------------

    public Compute acquireComputeSync(ComputeDescription description) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            TimeoutException, NoSuccessException {
        return new ComputeImpl(m_session, (ResourceManagerImpl) this, m_adaptor, description);
    }
    public Compute acquireComputeSync(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return new ComputeImpl(m_session, (ResourceManagerImpl) this, m_adaptor, id);
    }
    public void releaseComputeSync(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        this.acquireComputeSync(id).release();
    }
    public void releaseComputeSync(String id, boolean drain) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        this.acquireComputeSync(id).release(drain);
    }

    //----------------------------------------------------------------

    public Network acquireNetworkSync(NetworkDescription description) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            TimeoutException, NoSuccessException {
        return new NetworkImpl(m_session, (ResourceManagerImpl) this, m_adaptor, description);
    }
    public Network acquireNetworkSync(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return new NetworkImpl(m_session, (ResourceManagerImpl) this, m_adaptor, id);
    }
    public void releaseNetworkSync(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        this.acquireNetworkSync(id).release();
    }

    //----------------------------------------------------------------

    public Storage acquireStorageSync(StorageDescription description) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            TimeoutException, NoSuccessException {
        return new StorageImpl(m_session, (ResourceManagerImpl) this, m_adaptor, description);
    }
    public Storage acquireStorageSync(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        return new StorageImpl(m_session, (ResourceManagerImpl) this, m_adaptor, id);
    }
    public void releaseStorageSync(String id) throws NotImplementedException,
            AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
        this.acquireStorageSync(id).release();
    }

    //----------------------------------------------------------------

    /** This method is specific to JSAGA implementation */
    public void startListening() {
        //TODO
    }
    /** This method is specific to JSAGA implementation */
    public void stopListening() {
        //TODO
    }
}
