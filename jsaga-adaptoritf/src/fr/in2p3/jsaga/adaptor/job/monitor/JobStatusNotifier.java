package fr.in2p3.jsaga.adaptor.job.monitor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobStatusNotifier
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobStatusNotifier {
    public void notifyChange(JobStatus status);
}
