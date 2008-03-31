package fr.in2p3.jsaga.adaptor.job.monitor;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

import java.util.Date;

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
    /**
     * Get status of jobs matching filter.
     * @param userID the identifier of the user.
     * @param jcName the name of the job collection.
     * @param startDate the beginning of the period.
     * @return the status of jobs matching filter.
     */
    public JobStatus[] getFilteredStatus(String userID, String jcName, Date startDate) throws Timeout, NoSuccess;
}
