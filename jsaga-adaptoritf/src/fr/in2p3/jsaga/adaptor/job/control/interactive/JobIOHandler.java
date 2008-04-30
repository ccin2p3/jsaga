package fr.in2p3.jsaga.adaptor.job.control.interactive;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIOHandler
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobIOHandler {
    /**
     * @return the identifier of the job in the grid
     */
    public String getJobId();
}
