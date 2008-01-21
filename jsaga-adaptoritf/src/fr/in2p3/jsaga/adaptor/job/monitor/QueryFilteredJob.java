package fr.in2p3.jsaga.adaptor.job.monitor;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   QueryFilteredJob
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface QueryFilteredJob extends QueryJob {
    public JobStatus[] getFilteredStatus(String filter) throws Timeout, NoSuccess;
}
