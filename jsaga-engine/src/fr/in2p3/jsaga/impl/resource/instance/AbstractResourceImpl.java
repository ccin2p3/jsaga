package fr.in2p3.jsaga.impl.resource.instance;

import java.util.Properties;

import fr.in2p3.jsaga.adaptor.resource.ComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.NetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.StorageResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.manager.AbstractSyncResourceManagerImpl;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;
import fr.in2p3.jsaga.impl.resource.task.AbstractResourceTaskImpl;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public abstract class AbstractResourceImpl<R extends Resource, RD extends ResourceDescription>
        extends AbstractResourceTaskImpl<R> implements Resource<R,RD>
{
    protected ResourceAdaptor m_adaptor;
    private RD m_description;
    private ResourceManager m_manager;
    private ResourceAttributes m_attributes;

    /** constructor for resource acquisition 
     * @throws DoesNotExistException 
     * @throws IncorrectStateException 
     * @throws NoSuccessException 
     * @throws TimeoutException 
     * @throws PermissionDeniedException 
     * @throws AuthorizationFailedException 
     * @throws AuthenticationFailedException 
     * @throws NotImplementedException */
    public AbstractResourceImpl(Type type, Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, RD description) 
            throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, 
                    PermissionDeniedException, TimeoutException, NoSuccessException, IncorrectStateException, 
                    DoesNotExistException {
        this(type, session, manager, adaptor);
        m_attributes.m_Description.setObject(description.toString());
        if (Type.COMPUTE.equals(type) && ! (m_adaptor instanceof ComputeResourceAdaptor)) {
            throw new NotImplementedException("This adaptor does not handle compute resources");
        }
        if (Type.STORAGE.equals(type) && ! (m_adaptor instanceof StorageResourceAdaptor)) {
            throw new NotImplementedException("This adaptor does not handle storage resources");
        }
        if (Type.NETWORK.equals(type) && ! (m_adaptor instanceof NetworkResourceAdaptor)) {
            throw new NotImplementedException("This adaptor does not handle network resources");
        }
        Properties properties = new Properties();
        for (String attr: description.listAttributes()) {
            try { // scalar attribute
                properties.setProperty(attr, description.getAttribute(attr));
            } catch (IncorrectStateException ise) {
                properties.put(attr, description.getVectorAttribute(attr));
            }
        }
        String resourceId;
        if (m_adaptor instanceof ComputeResourceAdaptor) {
            resourceId = ((ComputeResourceAdaptor)m_adaptor).acquireComputeResource(properties);
        // TODO uncommend this
//        } else if (m_adaptor instanceof StorageResourceAdaptor) {
//            resourceId = ((StorageResourceAdaptor)m_adaptor).acquireStorageResource(properties);
//        } else if (m_adaptor instanceof NetworkResourceAdaptor) {
//            resourceId = ((NetworkResourceAdaptor)m_adaptor).acquireNetworkResource(properties);
        } else {
            throw new NotImplementedException("Unkown type of resource adaptor");
        }
        // TODO remove this cast
        m_attributes.m_ResourceID.setObject(((AbstractSyncResourceManagerImpl)m_manager).toSagaId(resourceId));
        // reload description
        Properties prop;
        try {
            prop = m_adaptor.getDescription(getInternalId());
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
        m_description = createDescription(prop);
    }

    /** constructor for reconnecting to resource already acquired 
     * @throws DoesNotExistException 
     * @throws NoSuccessException 
     * @throws TimeoutException 
     * @throws NotImplementedException 
     * @throws BadParameterException */
    public AbstractResourceImpl(Type type, Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, String id) 
            throws DoesNotExistException, TimeoutException, NoSuccessException, 
                    NotImplementedException, BadParameterException {
        this(type, session, manager, adaptor);
        m_attributes.m_ResourceID.setObject(id);
        // Get description of the resource
        Properties description = m_adaptor.getDescription(getInternalId());
        if (!type.name().equals(description.getProperty(Resource.RESOURCE_TYPE))) {
            throw new NotImplementedException();
        }
        m_description = createDescription(description);
    }

    /** common to all constructors */
    private AbstractResourceImpl(Type type, Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor) {
        super(session, manager);
        m_manager = manager;
        m_adaptor = adaptor;
        m_attributes = new ResourceAttributes(this);
        m_attributes.m_Type.setObject(type);
        m_attributes.m_ManagerID.setObject(manager.getId());
        m_attributes.m_Access.setObjects(adaptor.getAccess(getId()));
    }

    // getters
    public String getId() {
        return m_attributes.m_ResourceID.getObject();
    }
    public String getInternalId() throws BadParameterException {
        return AbstractSyncResourceManagerImpl.idFromSagaId(getId());
    }
    public Type getType() {
        return m_attributes.m_Type.getObject();
    }
    public ResourceManager getManager() {
        return m_manager;
    }
    public String[] getAccess() {
        return m_attributes.m_Access.getObjects();
    }
    public RD getDescription() {
        return m_description;
    }

    protected abstract RD createDescription(Properties description);
    
    /** reconfigure 
     * @throws BadParameterException 
     * @throws NoSuccessException */
    public void reconfigure(RD description) throws BadParameterException, NoSuccessException {
        m_description = description;
        if (description != null) {
            m_attributes.m_Description.setObject(description.toString());
            try {
                m_adaptor.release(getInternalId());
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            } catch (NotImplementedException e) {
                throw new NoSuccessException(e);
            }
            // TODO: acquire and change id probably...
        }
    }

    /** release 
     * @throws NoSuccessException */
    public void release() throws NoSuccessException {
        try {
            m_adaptor.release(getInternalId());
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }
}
