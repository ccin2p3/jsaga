package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;
import fr.in2p3.jsaga.adaptor.u6.U6Abstract;

import org.ogf.saga.error.NoSuccess;

import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.exceptions.GPEInvalidResourcePropertyQNameException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareRemoteException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareServiceException;
import com.intel.gpe.clients.api.exceptions.GPEResourceUnknownException;
import com.intel.gpe.clients.api.exceptions.GPEUnmarshallingException;
import com.intel.gpe.clients.impl.jms.AtomicJobClientImpl;

import java.util.List;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6JobAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************/

public abstract class U6JobAdaptorAbstract extends U6Abstract implements SagaSecureAdaptor {

    public String getType() {
        return "unicore6";
    }
    
	protected JobClient getJobById(String nativeJobId) throws NoSuccess {
		try {		
	    	// TODO Optimize this
	        // list jobs
			TargetSystemInfo targetSystemInfo = findTargetSystem();
	        List<JobClient> jobList = targetSystemInfo.getTargetSystem().getJobs();
	        for (JobClient jobClient : jobList) {
	        	String currentJobId = ((AtomicJobClientImpl) jobClient).getId().toString();
	        	if(currentJobId.equals(nativeJobId)) {
					return jobClient;
				}
			}
		} catch (GPEInvalidResourcePropertyQNameException e) {
			throw new NoSuccess(e);
		} catch (GPEResourceUnknownException e) {
			throw new NoSuccess(e);
		} catch (GPEUnmarshallingException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
        throw new NoSuccess("Unable to get job:"+nativeJobId);
	}
 }
