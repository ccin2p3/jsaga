package fr.in2p3.jsaga.adaptor.resource;

import fr.in2p3.jsaga.adaptor.language.JobDescriptionContainer;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ResourceSelectionAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface ResourceSelectionAdaptor {
    /**
     * Initialise resource selection algorithm
     * <br>Notes:
     * <br>MAY perform the match-making once for ALL jobs of the collection
     * @param desc JSDL or native job description
     * @param nbJobsTotal Total number of jobs in collection
     */
    public void init(JobDescriptionContainer desc, int nbJobsTotal);

    /**
     * Select a set of available resources
     * <br>Notes:
     * <br>MUST perform ranking for current job subset
     * <br>MAY perform the match-making also
     * @param desc JSDL or native job description
     * @param maxSlots Number of jobs to submit
     * @return set of available resources
     */
    public SelectedResource[] select(JobDescriptionContainer desc, int maxSlots);

    /**
     * Notify status of terminated job
     * <br>Notes:
     * <br>MAY remove this resource from the list of maching resources for this jobId, if failed or aborted
     * <br>MAY decrease the score of this resource if failed or aborted
     * <br>MAY increase the score of this resource if completed
     * @param jobId
     * @param status
     * @param resource
     * @param queue
     */
    public void notifyTerminatedJob(String jobId, int status, String resource, String queue);
}
