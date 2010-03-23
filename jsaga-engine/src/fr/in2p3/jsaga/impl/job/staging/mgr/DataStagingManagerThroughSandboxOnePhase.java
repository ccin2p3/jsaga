package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingManagerThroughSandboxOnePhase
 * Author: sreynaud (sreynaud@in2p3.fr)
 * Date:   18 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingManagerThroughSandboxOnePhase extends DataStagingManagerThroughSandbox {
    public DataStagingManagerThroughSandboxOnePhase(StagingJobAdaptorOnePhase adaptor, String uniqId) throws NotImplementedException, BadParameterException, NoSuccessException {
        super(adaptor, uniqId);
    }

    public void preStaging(AbstractSyncJobImpl job, String nativeJobDescription, String uniqId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        StagingJobAdaptorOnePhase adaptor = (StagingJobAdaptorOnePhase) m_adaptor;

        // create intermediary directory
        String stagingDir = adaptor.getStagingDirectory(nativeJobDescription, uniqId);
        if (stagingDir != null) {
            URL url = URLFactory.createURL(stagingDir);
            try {
                FileFactory.createDirectory(job.getSession(), url, Flags.CREATE.getValue());
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            } catch (AlreadyExistsException e) {
                throw new NoSuccessException(e);
            }
        }

        // for each input file
        for (StagingTransfer transfer : adaptor.getInputStagingTransfer(nativeJobDescription, uniqId)) {
            transfer(job.getSession(), transfer);
        }
    }
}
