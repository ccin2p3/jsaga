package fr.in2p3.jsaga.impl.resource.instance;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.session.Session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ComputeImpl extends AbstractResourceImpl<Compute,ComputeDescription> implements Compute {
    /** constructor for resource acquisition */
    public ComputeImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, ComputeDescription description) {
        super(Type.COMPUTE, session, manager, adaptor, description);
    }

    /** constructor for reconnecting to resource already acquired */
    public ComputeImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, String id) {
        super(Type.COMPUTE, session, manager, adaptor, id);
    }

    public void release(boolean drain) {
        m_adaptor.release(drain);
    }
}