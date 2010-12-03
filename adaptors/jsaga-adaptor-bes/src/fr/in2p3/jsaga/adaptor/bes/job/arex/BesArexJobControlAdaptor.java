package fr.in2p3.jsaga.adaptor.bes.job.arex;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.ogf.saga.error.*;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesArexJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/
public class BesArexJobControlAdaptor extends BesJobControlAdaptorAbstract 
		implements JobControlAdaptor {

    public String getType() {
        return "bes-arex";
    }
    
	public int getDefaultPort() {
		return 2010;
	}

	public Usage getUsage() {
    	return null;
    }
	
	protected Class getJobClass() {
		return BesArexJob.class;
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BesArexJobMonitorAdaptor();
    }

}