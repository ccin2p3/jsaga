package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.staging.*;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;

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
    public DataStagingManagerThroughSandboxOnePhase(StagingJobAdaptor adaptor, String uniqId) throws NotImplementedException, BadParameterException, NoSuccessException {
        super(adaptor, uniqId);
    }

    public void preStaging(AbstractSyncJobImpl job, String nativeJobDescription, String uniqId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        super.preStaging(job);

        // for each input file
        StagingJobAdaptorOnePhase adaptor = (StagingJobAdaptorOnePhase) m_adaptor;
        for (StagingTransfer transfer : adaptor.getInputStagingTransfer(nativeJobDescription, uniqId)) {
            transfer(job.getSession(), transfer);
        }
    }
}
