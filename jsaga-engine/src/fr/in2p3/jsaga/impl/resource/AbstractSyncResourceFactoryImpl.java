package fr.in2p3.jsaga.impl.resource;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.engine.factories.ResourceAdaptorFactory;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import fr.in2p3.jsaga.impl.resource.description.ComputeDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.description.NetworkDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.description.StorageDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;
import fr.in2p3.jsaga.impl.session.SessionImpl;
import fr.in2p3.jsaga.sync.resource.SyncResourceFactory;
import org.ogf.saga.error.*;
import org.ogf.saga.resource.ResourceFactory;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public abstract class AbstractSyncResourceFactoryImpl extends ResourceFactory implements SyncResourceFactory {
    private ResourceAdaptorFactory m_adaptorFactory;

    /** constructor */
    public AbstractSyncResourceFactoryImpl(ResourceAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
    }

    public ComputeDescription doCreateComputeDescription() throws NotImplementedException, NoSuccessException {
        return new ComputeDescriptionImpl();
    }

    public NetworkDescription doCreateNetworkDescription() throws NotImplementedException, NoSuccessException {
        return new NetworkDescriptionImpl();
    }

    public StorageDescription doCreateStorageDescription() throws NotImplementedException, NoSuccessException {
        return new StorageDescriptionImpl();
    }

    public ResourceManager doCreateManagerSync(Session session, URL rm) throws NotImplementedException, BadParameterException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        // rm cannot be null or empty because :
        // 1. ResourceFactory.createResourceManager() creates a URL if null
        // 2. URL.normalize() returns "./" when ""
        // But anyway, we keep the check
        if (rm!=null && !rm.toString().equals("")) {
            // get context (security + config)
            ContextImpl context;
            try {
                context = ((SessionImpl) session).getBestMatchingContext(rm);
            } catch (DoesNotExistException | PermissionDeniedException e) {
                throw new NoSuccessException(e);
            }

            // create adaptor instance
            ResourceAdaptor adaptor = m_adaptorFactory.getAdaptor(rm, context);

            // get attributes
            Map attributes = m_adaptorFactory.getAttribute(rm, context);

            // connect to control/monitor services
            m_adaptorFactory.connect(rm, adaptor, attributes, context);

            // create manager
            return new ResourceManagerImpl(session, rm, adaptor);
        } else {
            throw new NotImplementedException("Resource discovery not yet implemented");
        }
    }
}
