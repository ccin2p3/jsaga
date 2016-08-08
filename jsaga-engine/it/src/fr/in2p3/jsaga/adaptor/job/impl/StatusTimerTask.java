package fr.in2p3.jsaga.adaptor.job.impl;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatusNotifier;

import java.util.TimerTask;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   StatusTimerTask
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************/
public class StatusTimerTask extends TimerTask {
    private JobStatusNotifier m_notifier;

    public StatusTimerTask(JobStatusNotifier notifier) {
        m_notifier = notifier;
    }

    public void run() {
        m_notifier.notifyChange(new JobStatus("myjobid", null, null){
            public String getModel() {return "TEST";}
            public SubState getSubState() {return SubState.DONE;}
        });
    }
}
