package fr.in2p3.jsaga.impl.job.staging;

import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   InputDataStagingToRemote
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   20 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class InputDataStagingToRemote extends AbstractDataStagingRemote {
    protected InputDataStagingToRemote(URL localURL, URL workerURL, boolean append) {
        super(localURL, workerURL, append);
    }

    public void preStaging(Session session) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        super.copy(session, m_localURL, m_workerURL);
    }

    public boolean isInput() {
        return INPUT;
    }
}
