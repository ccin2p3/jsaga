package fr.in2p3.jsaga.sync.job;

import org.ogf.saga.error.*;
import org.ogf.saga.job.JobService;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SyncJobFactory
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   6 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface SyncJobFactory {
    public JobService doCreateJobServiceSync(Session session, URL rm) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException,
            PermissionDeniedException, TimeoutException, NoSuccessException;
}
