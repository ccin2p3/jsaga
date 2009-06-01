package fr.in2p3.jsaga.sync.file;

import org.ogf.saga.error.*;
import org.ogf.saga.file.*;
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
public interface SyncFileFactory {
    public File doCreateFileSync(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public FileInputStream doCreateFileInputStreamSync(Session session, URL name)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public FileOutputStream doCreateFileOutputStreamSync(Session session, URL name, boolean append)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;

    public Directory doCreateDirectorySync(Session session, URL name, int flags)
            throws NotImplementedException, IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException, BadParameterException,
            AlreadyExistsException, DoesNotExistException, TimeoutException, NoSuccessException;
}
