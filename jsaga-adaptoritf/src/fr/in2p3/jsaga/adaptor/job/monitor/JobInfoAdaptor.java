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
    public Integer getExitCode(String nativeJobId) throws NotImplementedException, NoSuccessException;
    public Date getCreated(String nativeJobId) throws NotImplementedException, NoSuccessException;
    public Date getStarted(String nativeJobId) throws NotImplementedException, NoSuccessException;
    public Date getFinished(String nativeJobId) throws NotImplementedException, NoSuccessException;
    public String[] getExecutionHosts(String nativeJobId) throws NotImplementedException, NoSuccessException;
}
