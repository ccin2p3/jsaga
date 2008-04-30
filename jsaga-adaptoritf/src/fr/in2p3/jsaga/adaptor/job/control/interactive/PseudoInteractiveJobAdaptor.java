package fr.in2p3.jsaga.adaptor.job.control.interactive;

import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;

import java.io.InputStream;

import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PseudoInteractiveJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface PseudoInteractiveJobAdaptor extends JobControlAdaptor {
    /**
     * submit an interactive job
     * @param jobDesc the job description in the language supported by the targeted grid
     * @param checkMatch if true then check if job description matches job service before submitting job
     * @param stdin the job standard input stream
     * @return the job input/output streams handler
     */
    public JobIOHandler submitInteractive(String jobDesc, boolean checkMatch, InputStream stdin) throws PermissionDenied, Timeout, NoSuccess;
}
