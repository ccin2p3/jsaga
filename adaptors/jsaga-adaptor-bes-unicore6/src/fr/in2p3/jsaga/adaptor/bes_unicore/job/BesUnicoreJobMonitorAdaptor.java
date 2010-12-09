package fr.in2p3.jsaga.adaptor.bes_unicore.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

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

public class BesUnicoreJobMonitorAdaptor extends BesJobMonitorAdaptor implements QueryIndividualJob, ListableJobAdaptor {
        
    public String getType() {
        return "bes-unicore";
    }

	public int getDefaultPort() {
		return 8080;
	}

    public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
	protected Class getJobClass() {
			return BesUnicoreJob.class;
	}

}
