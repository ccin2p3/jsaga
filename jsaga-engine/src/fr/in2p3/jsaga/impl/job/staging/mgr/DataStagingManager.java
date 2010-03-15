package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingManager
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   9 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface DataStagingManager {
    public JobDescription modifyJobDescription(final JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException;
    public void preStaging(AbstractSyncJobImpl job) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException;
    public void postStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException;
    public void cleanup(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException;
}
