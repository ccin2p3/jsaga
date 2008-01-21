package fr.in2p3.jsaga.adaptor.job.monitor;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ListenFilteredJob
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface ListenFilteredJob extends ListenJob {
    public void subscribeFilteredJob(JobStatusNotifier notifier) throws Timeout, NoSuccess;
    public void unsubscribeFilteredJob() throws Timeout, NoSuccess;
}
