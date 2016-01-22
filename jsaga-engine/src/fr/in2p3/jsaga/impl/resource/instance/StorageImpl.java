package fr.in2p3.jsaga.impl.resource.instance;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Storage;
import org.ogf.saga.session.Session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class StorageImpl extends AbstractResourceImpl<Storage,StorageDescription> implements Storage {
    /** constructor for resource acquisition */
    public StorageImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, StorageDescription description) {
        super(Type.STORAGE, session, manager, adaptor, description);
    }

    /** constructor for reconnecting to resource already acquired */
    public StorageImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, String id) {
        super(Type.STORAGE, session, manager, adaptor, id);
    }
}
