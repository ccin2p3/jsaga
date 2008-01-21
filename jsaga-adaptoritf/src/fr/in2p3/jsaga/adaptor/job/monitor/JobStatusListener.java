package fr.in2p3.jsaga.adaptor.job.monitor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStatusListener
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class JobStatusListener {
    protected JobStatusNotifier m_notifier;

    public JobStatusListener(JobStatusNotifier notifier) {
        m_notifier = notifier;
    }
}
