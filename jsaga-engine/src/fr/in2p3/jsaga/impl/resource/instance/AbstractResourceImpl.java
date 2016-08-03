package fr.in2p3.jsaga.impl.resource.instance;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.SecuredResource;
import fr.in2p3.jsaga.adaptor.resource.compute.ComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.compute.SecuredComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.compute.UnsecuredComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.network.NetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.network.SecuredNetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.network.UnsecuredNetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.storage.SecuredStorageResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.storage.StorageResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.storage.UnsecuredStorageResourceAdaptor;
import fr.in2p3.jsaga.helpers.SAGAId;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.resource.manager.AbstractSyncResourceManagerImpl;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;
import fr.in2p3.jsaga.impl.resource.task.AbstractResourceTaskImpl;

import org.apache.log4j.Logger;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
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
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public abstract class AbstractResourceImpl<R extends Resource, RD extends ResourceDescription>
        extends AbstractResourceTaskImpl<R,RD> implements Resource<R,RD>
{
    protected Logger m_logger = Logger.getLogger(AbstractResourceImpl.class);

    private ResourceAttributes m_attributes;
    private RD m_description;
    private ResourceManager m_manager;
    private SecuredResource m_securedResourceContext = null;
    
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
    public String getResourceId() {
        return m_attributes.m_ResourceID.getObject();
    }
    @Override
    public Type getType() {
        return m_attributes.m_Type.getObject();
    }
    @Override
    public ResourceManager getManager() {
        return m_manager;
    }
    
    /**
     * Get accesses. Accesses are not required when resource is built because they might be
     * unavailable before resource is fully ACTIVE.
     */
    @Override
    public String[] getAccess() throws NotImplementedException, AuthenticationFailedException, 
                    AuthorizationFailedException, TimeoutException, NoSuccessException {
        try {
            if (m_attributes.m_Access.getValues().length == 0) {
                String[] accesses = m_adaptor.getAccess(SAGAId.idFromSagaId(getResourceId()));
                // set access
                m_attributes.m_Access.setObjects(accesses);
                // now that we have access we can build the context and add it if not already added
                if (m_securedResourceContext != null && m_securedResourceContext.getId() != null &&
                        m_securedResourceContext.getContextType() != null) {
                    try {
                        // Add the access to BaseUrlIncludes: this make the context unique
                        m_securedResourceContext.put(ContextImpl.BASE_URL_INCLUDES, m_attributes.m_Access.getObjects());
                        // Build the new context
                        Context context = ContextFactory.createContext(JSAGA_FACTORY, 
                                m_securedResourceContext.getContextType());
                        for (Entry<Object, Object> entry: m_securedResourceContext.entrySet()) {
                            if (entry.getValue() instanceof String) {
                                context.setAttribute((String)entry.getKey(), (String)entry.getValue());
                            } else if (entry.getValue() instanceof String[]) {
                                context.setVectorAttribute((String)entry.getKey(), (String[])entry.getValue());
                            }
                        }
                        // add it to the session
                        m_session.addContext(context);
                    } catch (BadParameterException bpe) {
                        throw new NoSuccessException(bpe);
                    } catch (PermissionDeniedException e) {
                        throw new AuthorizationFailedException(e);
                    }
                }
            }
        } catch (IncorrectStateException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
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
            try {
                this.release();
                // check that adaptor supports type
                String resourceId = this.acquireResource(description);
                m_attributes.m_ResourceID.setObject(SAGAId.idToSagaId(
                        ((AbstractSyncResourceManagerImpl)m_manager).getURL(), 
                        resourceId));
                m_attributes.m_Access.setObjects(m_adaptor.getAccess(SAGAId.idFromSagaId(getResourceId())));
                // reload description
                this.loadDescription();
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
        }
    }

    /** release 
     * @throws NoSuccessException 
     * @throws IncorrectStateException */
    public void release() throws NoSuccessException, IncorrectStateException {
        try {
            m_adaptor.release(SAGAId.idFromSagaId(getResourceId()));
        } catch (DoesNotExistException | NotImplementedException | BadParameterException e) {
            throw new NoSuccessException(e);
        }
        // Try to remove security context from session
        for (Context c: m_session.listContexts()) {
            try {
                if (c.existsAttribute(ContextImpl.BASE_URL_INCLUDES)) {
                    String[] urls = c.getVectorAttribute(ContextImpl.BASE_URL_INCLUDES);
                    if (Arrays.equals(urls, m_attributes.m_Access.getObjects())) {
                        m_logger.debug("Removing context: " + c.getId());
                        m_session.removeContext(c);
                        return;
                    }
                }
            } catch (Exception e) {
                m_logger.warn("could not remove context", e);
            }
        }
    }


    //////////////////
    // Private methods
    //////////////////
    /*
     * Acquire a resource from a description.
     * Returns the ID of the acquired resource.
     */
    private String acquireResource(RD description) 
            throws NotImplementedException, NoSuccessException, AuthenticationFailedException, 
            AuthorizationFailedException, PermissionDeniedException, TimeoutException, 
            DoesNotExistException, IncorrectStateException {
        // translate the description into a Properties for the adaptor
        Properties properties = new Properties();
        for (String attr: description.listAttributes()) {
            try { // scalar attribute
                properties.setProperty(attr, description.getAttribute(attr));
            } catch (IncorrectStateException ise) {
                properties.put(attr, description.getVectorAttribute(attr));
            }
        }
        if (description instanceof ComputeDescription) {
            if (m_adaptor instanceof SecuredComputeResourceAdaptor) {
                // this adaptor sends back a resourceID along with properties necessary to build a security context
                // the security context will be build at getAccess stage as the IP address of the resource may not
                // be available yet
                m_securedResourceContext = ((SecuredComputeResourceAdaptor)m_adaptor).acquireComputeResource(properties);
                // returns the ID only
                return m_securedResourceContext.getId();
            } else if (m_adaptor instanceof UnsecuredComputeResourceAdaptor) {
                return ((UnsecuredComputeResourceAdaptor)m_adaptor).acquireComputeResource(properties);
            }
        } else if (description instanceof StorageDescription) {
            if (m_adaptor instanceof SecuredStorageResourceAdaptor) {
                m_securedResourceContext = ((SecuredStorageResourceAdaptor)m_adaptor).acquireStorageResource(properties);
                return m_securedResourceContext.getId();
            } else if (m_adaptor instanceof UnsecuredStorageResourceAdaptor) {
                return ((UnsecuredStorageResourceAdaptor)m_adaptor).acquireStorageResource(properties);
            }
        } else if (description instanceof NetworkDescription) {
            if (m_adaptor instanceof SecuredNetworkResourceAdaptor) {
                m_securedResourceContext = ((SecuredNetworkResourceAdaptor)m_adaptor).acquireNetworkResource(properties);
                return m_securedResourceContext.getId();
            } else if (m_adaptor instanceof UnsecuredNetworkResourceAdaptor) {
                return ((UnsecuredNetworkResourceAdaptor)m_adaptor).acquireNetworkResource(properties);
            }
        }
        throw new NotImplementedException("Unkown type of resource adaptor");
    }

    /*
     * Asks the adaptor to send back the resource identified by ID.
     */
    private void loadDescription() throws TimeoutException, NoSuccessException, 
                DoesNotExistException, NotImplementedException, BadParameterException {
        // adaptor sends back a properties with resource description
        Properties description = m_adaptor.getDescription(SAGAId.idFromSagaId(getResourceId()));
        if (!getType().name().equals(description.getProperty(Resource.RESOURCE_TYPE))) {
            throw new NotImplementedException(getType().name() + " <> " + description.getProperty(Resource.RESOURCE_TYPE));
        }
        // the subclass instantiates the appropriate Resource object
        m_description = createDescription(description);
    }
    
    /*
     * checks that the type of resource matches with the adaptor instance
     */
    private void checkDescription() throws BadParameterException {
        this.checkDescription(getType());
    }
    
    /*
     * checks that the type in parameter matches with the adaptor instance
     */
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
