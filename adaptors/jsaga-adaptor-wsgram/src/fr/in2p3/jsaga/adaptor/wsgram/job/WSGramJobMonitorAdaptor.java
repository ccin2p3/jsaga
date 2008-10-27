package fr.in2p3.jsaga.adaptor.wsgram.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.globus.exec.client.GramJob;
import org.globus.gram.GramException;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************/

public class WSGramJobMonitorAdaptor extends WSGramJobAdaptorAbstract implements QueryIndividualJob {
    
    public JobStatus getStatus(String nativeJobHandle) throws TimeoutException, NoSuccessException {
    	GramJob job = this.getGramJobById(nativeJobHandle);
        try {
			job.refreshStatus();
		} catch (GramException e) {
			this.rethrowException(e);
        } catch (GSSException e) {
        	throw new NoSuccessException(e);
        } catch (Exception e) {
        	throw new NoSuccessException(e);
		}       
        return new WSGramJobStatus(job.getHandle(),job.getState(), job.getState().toString());
    }
    
    private void rethrowException(GramException e) throws TimeoutException, NoSuccessException {
    	switch(e.getErrorCode()) {
            case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
            case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
                throw new TimeoutException(e);
            default:
                throw new NoSuccessException(e);
        }
    }

}
