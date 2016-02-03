package fr.in2p3.jsaga.impl.resource.instance;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;
import fr.in2p3.jsaga.impl.resource.task.AbstractResourceTaskImpl;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
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

    /** constructor for resource acquisition */
    public AbstractResourceImpl(Type type, Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, RD description) {
        this(type, session, manager, adaptor);
        // TODO adaptor.acuqire
    }

    /** constructor for reconnecting to resource already acquired 
     * @throws DoesNotExistException 
     * @throws NoSuccessException 
     * @throws TimeoutException */
    public AbstractResourceImpl(Type type, Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, String id) throws DoesNotExistException, TimeoutException, NoSuccessException {
        this(type, session, manager, adaptor);
        m_attributes.m_ResourceID.setObject(id);
        m_adaptor.check(id);
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

    /** reconfigure */
    public void reconfigure(RD description) {
        m_description = description;
        if (description != null) {
            m_attributes.m_Description.setObject(description.toString());
            m_adaptor.release(getId());
            // TODO: acquire
        }
    }

    /** release */
    public void release() {
        m_adaptor.release(getId());
    }
}
