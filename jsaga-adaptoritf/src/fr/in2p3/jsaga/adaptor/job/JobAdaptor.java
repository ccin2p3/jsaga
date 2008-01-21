package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobAdaptor extends SagaSecureAdaptor {
    /**
     * @return the job service type.
     */
    public String getType();

    /**
     * @return the protocol schemes supported by sandbox management, or null if no sandbox management.
     */
    public String[] getSupportedSandboxProtocols();

    /**
     * @return the path to the stylesheet to transform the JSDL document to the native job description stream
     */
    public String getTranslator();

    /**
     * @return the array of stylesheet parameters
     */
    public Map getTranslatorParameters();

    /**
     * @return the class of the job monitor implementation
     */
    public JobMonitorAdaptor getDefaultJobMonitor();
}
