package fr.in2p3.jsaga.adaptor.bes.job;

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
     * Get the class of the appropriate Job object, depending of the BES implementation
     * 
     * @return Class the Java class of the Job object (this object must extend BesJob)
     * @see BesJob
     */
	public abstract Class getJobClass();
	
	/**
	 * Get the BES URL to use
	 * 
	 * @param host
	 * @param port
	 * @param basePath
	 * @param attributes
	 * @return String the URL build as "https://"+host+":"+port+basePath
	 */
    public abstract String getBESUrl(String host, int port, String basePath, Map attributes);
	
}
