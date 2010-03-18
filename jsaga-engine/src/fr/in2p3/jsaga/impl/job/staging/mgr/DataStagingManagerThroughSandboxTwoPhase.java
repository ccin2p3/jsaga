package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.adaptor.job.control.staging.*;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;

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
    public DataStagingManagerThroughSandboxTwoPhase(StagingJobAdaptor adaptor, String uniqId) throws NotImplementedException, BadParameterException, NoSuccessException {
        super(adaptor, uniqId);
    }

    public void preStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        super.preStaging(job);
        
        // for each input file
        StagingJobAdaptorTwoPhase adaptor = (StagingJobAdaptorTwoPhase) m_adaptor;
        for (StagingTransfer transfer : adaptor.getInputStagingTransfer(nativeJobId)) {
            transfer(job.getSession(), transfer);
        }
    }
}
