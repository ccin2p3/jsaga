package fr.in2p3.jsaga.adaptor.job.monitor;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

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
    /** Item at position USER_ID is the identifier of the user (String) */
    public static final int USER_ID = 0;
    /** Item at position COLLECTION_NAME is the name of the job collection (String) */
    public static final int COLLECTION_NAME = 1;
    /** Item at position START_DATE is the beginning of the period (Date) */
    public static final int START_DATE = 2;

    /**
     * Get status of jobs matching filter.
     * @param filters the filter values.
     * @return the status of jobs matching filter.
     */
    public JobStatus[] getFilteredStatus(Object[] filters) throws TimeoutException, NoSuccessException;
}
