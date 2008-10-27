package fr.in2p3.jsaga.engine.job.monitor.listen;

import fr.in2p3.jsaga.adaptor.job.monitor.ListenIndividualJob;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorCallback;
import fr.in2p3.jsaga.engine.job.monitor.request.JobStatusRequestor;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IndividualJobStatusListener
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IndividualJobStatusListener extends AbstractJobStatusListener {
    private ListenIndividualJob m_adaptor;

    public IndividualJobStatusListener(ListenIndividualJob adaptor, JobStatusRequestor requestor) {
        super(requestor);
        m_adaptor = adaptor;
    }

    protected void doSubscribeJob(String nativeJobId, JobMonitorCallback callback) throws TimeoutException, NoSuccessException {
        m_adaptor.subscribeJob(nativeJobId, new IndividualJobStatusNotifier(callback));
    }

    public synchronized void unsubscribeJob(String nativeJobId) throws TimeoutException, NoSuccessException {
        m_adaptor.unsubscribeJob(nativeJobId);
    }
}
