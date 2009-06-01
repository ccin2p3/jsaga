package fr.in2p3.jsaga.sync.namespace;

import org.ogf.saga.error.*;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SyncNSFactory
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface SyncNSFactory {
    public NSEntry doCreateNSEntrySync(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public NSDirectory doCreateNSDirectorySync(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;
}
