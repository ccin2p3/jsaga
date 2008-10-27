package fr.in2p3.jsaga.adaptor.job.control.interactive;

import org.ogf.saga.error.*;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   StreamableJobInteractiveSet
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface StreamableJobInteractiveSet extends StreamableJobAdaptor {
    /**
     * submit an interactive job
     * @param jobDesc the job description in the language supported by the targeted grid
     * @param checkMatch if true then check if job description matches job service before submitting job
     * @param stdin the standard input stream of the job
     * @param stdout the standard output stream of the job
     * @param stderr the standard error stream of the job
     * @return the identifier of the job in the grid
     */
    public String submitInteractive(String jobDesc, boolean checkMatch, InputStream stdin, OutputStream stdout, OutputStream stderr)
            throws PermissionDeniedException, TimeoutException, NoSuccessException;
}
