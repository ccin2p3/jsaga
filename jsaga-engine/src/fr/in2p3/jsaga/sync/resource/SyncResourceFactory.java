package fr.in2p3.jsaga.sync.resource;

import org.ogf.saga.error.*;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public interface SyncResourceFactory {
    public ResourceManager doCreateManagerSync(Session session, URL rm) throws NotImplementedException,
            BadParameterException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;
}
