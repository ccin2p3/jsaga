package fr.in2p3.jsaga.impl.job.staging.mgr;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.impl.job.instance.AbstractSyncJobImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.file.Directory;
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
    public static final String JSAGA_FACTORY = Base.getSagaFactory();

    public JobDescription modifyJobDescription(final JobDescription jobDesc) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException;
    public void postStaging(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException;
    public Directory cleanup(AbstractSyncJobImpl job, String nativeJobId) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, IncorrectStateException, NoSuccessException;
}
