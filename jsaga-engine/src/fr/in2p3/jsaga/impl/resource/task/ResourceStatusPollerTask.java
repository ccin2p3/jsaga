package fr.in2p3.jsaga.impl.resource.task;

import java.util.Timer;
import java.util.TimerTask;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStatusPollerTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ResourceStatusPollerTask extends TimerTask {
    private Timer m_timer;
    private Runnable m_poller;

    public ResourceStatusPollerTask(Runnable poller) {
        m_poller = poller;
    }

    public synchronized void start() {
        int pollPeriod = 5000;
        m_timer = new Timer();
        m_timer.schedule(this, 0, pollPeriod);
    }

    public synchronized void stop() {
        m_timer.cancel();
        m_timer = null;
    }

    /** invoked by timer */
    public void run() {
        m_poller.run();
    }
}
