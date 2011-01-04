package fr.in2p3.jsaga.adaptor.bes_unicore.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;

import org.ogf.saga.error.IncorrectStateException;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesUnicoreJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   9 déc. 2010
* ***************************************************/

public class BesUnicoreJobMonitorAdaptor extends BesJobMonitorAdaptor  {
        
    public String getType() {
        return "bes-unicore";
    }

	public int getDefaultPort() {
		return 8080;
	}

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default("res", "default_bes_factory")};
    }
    
	public Class getJobClass() {
		return BesUnicoreJob.class;
	}

	protected Class getJobStatusClass() {
		return BesUnicoreJobStatus.class;
	}
	
    public String getBESUrl(String host, int port, String basePath, Map attributes) {
    	String url = super.getBESUrl(host, port, basePath, attributes);
		url += "?res=" + (String)attributes.get("res");
		return url;
    }

}
