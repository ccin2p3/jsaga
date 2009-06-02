package fr.in2p3.jsaga.sync.logicalfile;

import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalDirectory;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SyncFileFactory
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface SyncLogicalFileFactory {
    public LogicalFile doCreateLogicalFileSync(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public LogicalDirectory doCreateLogicalDirectorySync(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;
}
