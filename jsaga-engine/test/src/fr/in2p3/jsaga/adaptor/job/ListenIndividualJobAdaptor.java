package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;
import fr.in2p3.jsaga.adaptor.job.monitor.ListenIndividualJob;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import java.util.Timer;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ListenIndividualJobAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class ListenIndividualJobAdaptor extends JobAdaptorAbstract implements ListenIndividualJob {
    private Timer m_timer;

    public String getType() {
        return "listen-individual";
    }

    public void subscribeJob(String nativeJobId, JobStatusNotifier notifier) throws TimeoutException, NoSuccessException {
        m_timer = new Timer();
        m_timer.schedule(new StatusTimerTask(notifier), 100);
    }
    public void unsubscribeJob(String nativeJobId) throws TimeoutException, NoSuccessException {
        m_timer.cancel();
    }
}
