package fr.in2p3.jsaga.adaptor.job.local;


import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.ogf.saga.error.*;

import java.lang.Exception;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   29 avril 2008
* ***************************************************/

public class LocalJobMonitorAdaptor extends LocalAdaptorAbstract implements QueryIndividualJob {

	public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {

    	try {    		
    		Process p = (Process) LocalAdaptorAbstract.sessionMap.get(nativeJobId);    		
    		return new LocalJobStatus(nativeJobId, p.exitValue());						
    	} catch (Exception e) {
    		if (e instanceof IllegalThreadStateException)
    			// return "process has not exited" message
    			return new LocalJobStatus(nativeJobId, -1);
    		else
    			throw new NoSuccessException(e);
		}
    }        

}
