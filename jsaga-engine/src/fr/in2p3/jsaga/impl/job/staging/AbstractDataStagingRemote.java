package fr.in2p3.jsaga.impl.job.staging;

import org.ogf.saga.url.URL;
import org.ogf.saga.session.Session;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.file.File;
import org.ogf.saga.file.FileFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   AbstractDataStagingRemote
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   27 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class AbstractDataStagingRemote extends AbstractDataStaging {
    protected URL m_workerURL;

    protected AbstractDataStagingRemote(URL localURL, URL workerURL, boolean append) {
        super(localURL, append);
        m_workerURL = workerURL;
    }

    protected void copy(Session session, URL sourceUrl, URL targetUrl) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        int append = (m_append ? Flags.APPEND : Flags.NONE).getValue();
        try {
            File file = FileFactory.createFile(session, sourceUrl, Flags.NONE.getValue());
            file.copy(targetUrl, append);
            file.close();
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }

    public void cleanup(Session session) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        try {
            File file = FileFactory.createFile(session, m_workerURL, Flags.NONE.getValue());
            file.remove();
            file.close();
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException(e);
        } catch (IncorrectURLException e) {
            throw new NoSuccessException(e);
        }
    }
}