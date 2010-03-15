package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingManagerDummy
 * Author: sreynaud (sreynaud@in2p3.fr)
 * Date:   15 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingManagerDummy implements DataStagingManager {
    public JobDescription modifyJobDescription(JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        return jobDesc;
    }

    public void preStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // do nothing
    }

    public void postStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // do nothing
    }

    public void cleanup(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException {
        // do nothing
    }
}
