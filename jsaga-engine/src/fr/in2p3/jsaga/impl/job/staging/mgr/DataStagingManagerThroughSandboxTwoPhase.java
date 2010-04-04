package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingManagerThroughSandboxTwoPhase
 * Author: sreynaud (sreynaud@in2p3.fr)
 * Date:   18 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingManagerThroughSandboxTwoPhase extends DataStagingManagerThroughSandbox {
    public DataStagingManagerThroughSandboxTwoPhase(StagingJobAdaptorTwoPhase adaptor, String uniqId) throws NotImplementedException, BadParameterException, NoSuccessException {
        super(adaptor, uniqId);
    }

    public void preStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        StagingJobAdaptorTwoPhase adaptor = (StagingJobAdaptorTwoPhase) m_adaptor;

        // create intermediary directory
        String stagingDir = adaptor.getStagingDirectory(nativeJobId);
        if (stagingDir != null) {
            URL url = URLFactory.createURL(stagingDir);
            try {
                Directory dir = FileFactory.createDirectory(job.getSession(), url, Flags.CREATE.getValue());
                dir.close();
            } catch (IncorrectURLException e) {
                throw new NoSuccessException(e);
            } catch (AlreadyExistsException e) {
                throw new NoSuccessException(e);
            }
        }

        // for each input file
        for (StagingTransfer transfer : adaptor.getInputStagingTransfer(nativeJobId)) {
            transfer(job.getSession(), transfer);
        }
    }
}
