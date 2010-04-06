package fr.in2p3.jsaga.adaptor.job.monitor;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ListenIndividualJob
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface ListenIndividualJob extends ListenJob {
    /**
     * Subscribe to receive notifications about job status changes for job nativeJobId.
     * @param nativeJobId the identifier of the job in the grid
     * @param notifier the callback
     */
    public void subscribeJob(String nativeJobId, JobStatusNotifier notifier) throws TimeoutException, NoSuccessException;

    /**
     * Unsubscribe from notifications about job status changes.
     * @param nativeJobId the identifier of the job in the grid
     */
    public void unsubscribeJob(String nativeJobId) throws TimeoutException, NoSuccessException;
}
