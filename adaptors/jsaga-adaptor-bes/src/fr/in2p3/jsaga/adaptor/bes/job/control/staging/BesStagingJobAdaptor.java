package fr.in2p3.jsaga.adaptor.bes.job.control.staging;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesStagingJobAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 jan 2011
* ***************************************************/

public interface BesStagingJobAdaptor extends StagingJobAdaptorOnePhase {
	
	/**
	 * Get the staging protocol
	 * @return the staging protocol
	 */
    //public abstract String getDataStagingProtocol();
    
    /**
     * Get the staging port
     * @return the staging port
     */
    //public abstract int getDataStagingPort();
    
    public abstract URI getDataStagingUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException;
}
