package fr.in2p3.jsaga.adaptor.job.control.interactive;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   InteractiveJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface InteractiveJobAdaptor extends JobControlAdaptor {
    /**
     * create a job input/output handler
     * @return the created handler
     */
    public JobIOHandler createJobIOHandler();

    /**
     * submit an interactive job
     * @param jobDesc the job description in the language supported by the targeted grid
     * @param checkMatch if true then check if job description matches job service before submitting job
     * @param ioHandler the job input/output handler
     * @param hasStdin true if getStdin() has been invoked by user application
     * @return the identifier of the job in the grid
     */
    public String submitInteractive(String jobDesc, boolean checkMatch, JobIOHandler ioHandler, boolean hasStdin) throws PermissionDenied, Timeout, NoSuccess;
}
