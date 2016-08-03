package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;
import fr.in2p3.jsaga.adaptor.job.monitor.ListenFilteredJob;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import java.util.Timer;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   ListenFilteredJobAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class ListenFilteredJobAdaptor extends JobAdaptorAbstract implements ListenFilteredJob {
    private Timer m_timer;

    public String getType() {
        return "listen-filtered";
    }

    public void subscribeFilteredJob(JobStatusNotifier notifier) throws TimeoutException, NoSuccessException {
        m_timer = new Timer();
        m_timer.schedule(new StatusTimerTask(notifier), 100);
    }

    public void unsubscribeFilteredJob() throws TimeoutException, NoSuccessException {
        m_timer.cancel();
    }
}
