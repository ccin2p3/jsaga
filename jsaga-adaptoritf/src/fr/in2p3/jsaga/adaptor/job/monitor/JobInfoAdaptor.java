package fr.in2p3.jsaga.adaptor.job.monitor;

import org.ogf.saga.error.*;

import java.util.Date;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobInfoAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   2 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public interface JobInfoAdaptor extends JobMonitorAdaptor {
    /**
     * @param nativeJobId the identifier of the job in the grid
     * @return the exit code of the job
     * @throws NotImplementedException if not supported by the adaptor
     * @throws NoSuccessException if failed to get the exit code
     */
    public Integer getExitCode(String nativeJobId) throws NotImplementedException, NoSuccessException;

    /**
     * @param nativeJobId the identifier of the job in the grid
     * @return the job creation time
     * @throws NotImplementedException if not supported by the adaptor
     * @throws NoSuccessException if failed to get the job creation time
     */
    public Date getCreated(String nativeJobId) throws NotImplementedException, NoSuccessException;

    /**
     * @param nativeJobId the identifier of the job in the grid
     * @return the job statup time
     * @throws NotImplementedException if not supported by the adaptor
     * @throws NoSuccessException if failed to get the job startup time
     */
    public Date getStarted(String nativeJobId) throws NotImplementedException, NoSuccessException;

    /**
     * @param nativeJobId the identifier of the job in the grid
     * @return the job end time
     * @throws NotImplementedException if not supported by the adaptor
     * @throws NoSuccessException if failed to get the job end time
     */
    public Date getFinished(String nativeJobId) throws NotImplementedException, NoSuccessException;

    /**
     * Get the execution host. Several hosts may be returned if the job is a parallel job.
     * @param nativeJobId the identifier of the job in the grid
     * @return the array of execution hosts
     * @throws NotImplementedException if not supported by the adaptor
     * @throws NoSuccessException if failed to get the execution hosts
     */
    public String[] getExecutionHosts(String nativeJobId) throws NotImplementedException, NoSuccessException;
}
