package fr.in2p3.jsaga.impl.resource.instance;

import java.util.Properties;

import fr.in2p3.jsaga.adaptor.resource.ComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.description.ComputeDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;

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
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.session.Session;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ComputeImpl extends AbstractResourceImpl<Compute,ComputeDescription> implements Compute {
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
    public ComputeImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, ComputeDescription description) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException, IncorrectStateException, DoesNotExistException, BadParameterException {
        super(Type.COMPUTE, session, manager, adaptor, description);
    }

    /** constructor for reconnecting to resource already acquired 
     * @throws DoesNotExistException 
     * @throws NoSuccessException 
     * @throws TimeoutException 
     * @throws NotImplementedException 
     * @throws BadParameterException */
    public ComputeImpl(Session session, ResourceManagerImpl manager, ResourceAdaptor adaptor, String id) throws DoesNotExistException, TimeoutException, NoSuccessException, NotImplementedException, BadParameterException {
        super(Type.COMPUTE, session, manager, adaptor, id);
    }

    public void release(boolean drain) throws DoesNotExistException, NotImplementedException, NoSuccessException {
        ((ComputeResourceAdaptor)m_adaptor).release(this.getId(), drain);
    }

    @Override
    protected ComputeDescription createDescription(Properties description) {
        return new ComputeDescriptionImpl(description);
    }
}
