package fr.in2p3.jsaga.adaptor.arex.job;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesArexJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   9 déc 2010
* ***************************************************/

public class ArexJobMonitorAdaptor extends BesJobMonitorAdaptor {
        
    public String getType() {
        return "arex";
    }

	public int getDefaultPort() {
		return 2010;
	}

	protected Class getJobClass() {
		return ArexJob.class;
	}

	protected Class getJobStatusClass() {
		return ArexJobStatus.class;
	}
}
