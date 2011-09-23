package fr.in2p3.jsaga.adaptor.bes_genesis2.job;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesGenesisIIJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 sept 2011
* ***************************************************/

public class BesGenesisIIJobMonitorAdaptor extends BesJobMonitorAdaptor  {
        
    public String getType() {
        return "bes-genesis2";
    }

	public int getDefaultPort() {
		return 18443;
	}

    /*public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default("res", "default_bes_factory")};
    }*/
    
	// TODO uncomment to implement substates
	/*
	protected JobStatus getJobStatus(String nativeJobId, ActivityStatusType ast) throws NoSuccessException {
	}
	*/
 

	/*
    public URI getBESUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
    	URI url = super.getBESUrl(host, port, basePath, attributes);
		return new URI(url.toString()+"?genii-container-id=" + (String)attributes.get("genii-container-id"));
    }*/

}
