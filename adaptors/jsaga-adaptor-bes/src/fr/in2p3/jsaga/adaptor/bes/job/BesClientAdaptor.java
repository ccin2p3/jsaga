package fr.in2p3.jsaga.adaptor.bes.job;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesClientAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 jan 2011
* ***************************************************/

public interface BesClientAdaptor extends ClientAdaptor {
    /**
     * Get a Job object, depending of the BES implementation
     * 
     * @return the Job object (this object must be/extend BesJob)
     * @see BesJob
     */
	public abstract BesJob getJob();
	
	/**
	 * Get the BES URL to use
	 * 
	 * @param host
	 * @param port
	 * @param basePath
	 * @param attributes
	 * @return String the URL for the BES service
	 * @throws URISyntaxException 
	 */
    public abstract URI getBESUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException;

}
