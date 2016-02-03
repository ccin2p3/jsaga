package fr.in2p3.jsaga.impl.resource.instance;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.session.Session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class NetworkImpl extends AbstractResourceImpl<Network,NetworkDescription> implements Network {
    /** constructor for resource acquisition */
    public NetworkImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, NetworkDescription description) {
        super(Type.NETWORK, session, manager, adaptor, description);
    }

    /** constructor for reconnecting to resource already acquired 
     * @throws DoesNotExistException 
     * @throws NoSuccessException 
     * @throws TimeoutException */
    public NetworkImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, String id) throws DoesNotExistException, TimeoutException, NoSuccessException {
        super(Type.NETWORK, session, manager, adaptor, id);
    }
}
