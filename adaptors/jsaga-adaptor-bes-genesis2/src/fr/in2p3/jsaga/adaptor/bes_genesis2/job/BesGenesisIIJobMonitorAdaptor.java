package fr.in2p3.jsaga.adaptor.bes_genesis2.job;

import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.ogf.saga.error.NoSuccessException;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

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
	
	@Override
	protected JobStatus getJobStatus(String nativeJobId, ActivityStatusType ast) throws NoSuccessException {
		return new BesGenesisIIJobStatus(nativeJobId, ast);
	}

}
