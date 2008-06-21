package fr.in2p3.jsaga.jobcollection;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionManager
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobCollectionManager extends SagaObject {
    /**
     * Creates a job collection instance as specified by the job collection description provided.
     * @param jd the job collection description.
     * @return the job collection.
     */
    public JobCollection createJobCollection(JobCollectionDescription jd) throws NotImplemented,
           AuthenticationFailed, AuthorizationFailed, PermissionDenied,
           BadParameter, Timeout, NoSuccess;

    /**
     * Creates a job collection instance as specified by the job collection description provided.
     * @param jd the job collection description.
     * @param force cleanup previous execution of job collection if needed
     * @return the job collection.
     */
    public JobCollection createJobCollection(JobCollectionDescription jd, boolean force) throws NotImplemented,
           AuthenticationFailed, AuthorizationFailed, PermissionDenied,
           BadParameter, Timeout, NoSuccess;

    /**
     * Obtains the list of job collections that are currently known to this
     * job collection manager.
     * @return a list of job collection ids.
     */
    public List<String> list() throws NotImplemented, AuthenticationFailed,
           AuthorizationFailed, PermissionDenied, Timeout, NoSuccess;

    /**
     * Returns the job collection instance associated with the specified job collection
     * identification.
     * @param jobCollectionId the job collection identification.
     * @return the job collection instance.
     */
    public JobCollection getJobCollection(String jobCollectionId) throws NotImplemented,
           AuthenticationFailed, AuthorizationFailed, PermissionDenied,
           BadParameter, DoesNotExist, Timeout, NoSuccess;
}
