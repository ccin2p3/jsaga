package fr.in2p3.jsaga.impl.resource.instance;

import java.util.Properties;

import fr.in2p3.jsaga.adaptor.resource.ComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.NetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.StorageResourceAdaptor;
import fr.in2p3.jsaga.helpers.SAGAId;
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
    private RD m_description;
    private ResourceManager m_manager;

    /** constructor for resource acquisition 
     * @throws DoesNotExistException 
     * @throws IncorrectStateException 
     * @throws NoSuccessException 
     * @throws TimeoutException 
     * @throws PermissionDeniedException 
     * @throws AuthorizationFailedException 
     * @throws AuthenticationFailedException 
     * @throws NotImplementedException 
     * @throws BadParameterException */
    public AbstractResourceImpl(Type type, Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, RD description) 
            throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, 
                    PermissionDeniedException, TimeoutException, NoSuccessException, IncorrectStateException, 
                    DoesNotExistException, BadParameterException {
        this(type, session, manager, adaptor);
        m_attributes.m_Description.setObject(description.toString());
        // Acquire a new resource and set the ID
        String resourceId = this.acquireResource(description);
        m_attributes.m_ResourceID.setObject(SAGAId.idToSagaId(
                ((AbstractSyncResourceManagerImpl)m_manager).getURL(), 
                resourceId));
        // set access
        m_attributes.m_Access.setObjects(adaptor.getAccess(SAGAId.idFromSagaId(getId())));
        // reload description
        this.loadDescription();
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
        // simply set the ID
        m_attributes.m_ResourceID.setObject(id);
        // set access
        m_attributes.m_Access.setObjects(adaptor.getAccess(SAGAId.idFromSagaId(getId())));
        // load description of the resource
        this.loadDescription();
    }

    /** common to all constructors 
     * @throws NotImplementedException */
    private AbstractResourceImpl(Type type, Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor) throws BadParameterException {
        super(session, manager, adaptor);
        m_manager = manager;
        m_attributes = new ResourceAttributes(this);
        m_attributes.m_Type.setObject(type);
        m_attributes.m_ManagerID.setObject(manager.getId());
        // check that adaptor supports type
        this.checkDescription();
    }

    // getters
    @Override
    public Type getType() {
        return m_attributes.m_Type.getObject();
    }
    @Override
    public ResourceManager getManager() {
        return m_manager;
    }
    @Override
    public String[] getAccess() {
        return m_attributes.m_Access.getObjects();
    }
    @Override
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
            this.release();
            try {
                // check that adaptor supports type
                String resourceId = this.acquireResource(description);
                m_attributes.m_ResourceID.setObject(SAGAId.idToSagaId(
                        ((AbstractSyncResourceManagerImpl)m_manager).getURL(), 
                        resourceId));
                m_attributes.m_Access.setObjects(m_adaptor.getAccess(SAGAId.idFromSagaId(getId())));
                // reload description
                this.loadDescription();
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
        }
    }

    /** release 
     * @throws NoSuccessException */
    public void release() throws NoSuccessException {
        try {
            m_adaptor.release(SAGAId.idFromSagaId(getId()));
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (NotImplementedException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }


    //////////////////
    // Private methods
    //////////////////
    private String acquireResource(RD description) 
            throws NotImplementedException, NoSuccessException, AuthenticationFailedException, 
            AuthorizationFailedException, PermissionDeniedException, TimeoutException, 
            DoesNotExistException, IncorrectStateException {
        Properties properties = new Properties();
        for (String attr: description.listAttributes()) {
            try { // scalar attribute
                properties.setProperty(attr, description.getAttribute(attr));
            } catch (IncorrectStateException ise) {
                properties.put(attr, description.getVectorAttribute(attr));
            }
        }
        if (m_adaptor instanceof ComputeResourceAdaptor) {
            return ((ComputeResourceAdaptor)m_adaptor).acquireComputeResource(properties);
        } else if (m_adaptor instanceof StorageResourceAdaptor) {
            return ((StorageResourceAdaptor)m_adaptor).acquireStorageResource(properties);
        } else if (m_adaptor instanceof NetworkResourceAdaptor) {
            return ((NetworkResourceAdaptor)m_adaptor).acquireNetworkResource(properties);
        } else {
            throw new NotImplementedException("Unkown type of resource adaptor");
        }
    }

    private void loadDescription() throws TimeoutException, NoSuccessException, 
                DoesNotExistException, NotImplementedException, BadParameterException {
        Properties description = m_adaptor.getDescription(SAGAId.idFromSagaId(getId()));
        if (!getType().name().equals(description.getProperty(Resource.RESOURCE_TYPE))) {
            throw new NotImplementedException(getType().name() + " <> " + description.getProperty(Resource.RESOURCE_TYPE));
        }
        m_description = createDescription(description);
    }
    
    private void checkDescription() throws BadParameterException {
        this.checkDescription(getType());
    }
    
    private void checkDescription(Type type) throws BadParameterException {
        if (Type.COMPUTE.equals(type) && ! (m_adaptor instanceof ComputeResourceAdaptor)) {
            throw new BadParameterException("This adaptor does not handle compute resources");
        }
        if (Type.STORAGE.equals(type) && ! (m_adaptor instanceof StorageResourceAdaptor)) {
            throw new BadParameterException("This adaptor does not handle storage resources");
        }
        if (Type.NETWORK.equals(type) && ! (m_adaptor instanceof NetworkResourceAdaptor)) {
            throw new BadParameterException("This adaptor does not handle network resources");
        }
        
    }
}
