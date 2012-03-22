package fr.in2p3.jsaga.adaptor.bes_genesis2.job;

import java.util.Map;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesGenesisIIJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 sept 2011
* ***************************************************/
public class BesGenesisIIJobControlAdaptor extends /*BesJobControlStagingOnePhaseAdaptorAbstract*/BesJobControlAdaptor implements JobControlAdaptor {

    public String getType() {
        return "bes-genesis2";
    }
    
	public int getDefaultPort() {
		return 18443;
	}

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(ATTR_REF_PARAM_NS, "http://edu.virginia.vcgr.genii/ref-params"),
    			new Default(ATTR_REF_PARAM_NAME, "resource-key"),
    	};
    }
    
    protected String getJobDescriptionTranslatorFilename() throws NoSuccessException {
    	return "xsl/job/bes-genesis2-jsdl.xsl";
    }
    
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BesGenesisIIJobMonitorAdaptor();
    }
    
}