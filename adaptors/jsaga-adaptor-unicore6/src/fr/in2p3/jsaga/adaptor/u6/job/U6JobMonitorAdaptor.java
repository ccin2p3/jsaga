package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryFilteredJob;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.Status;
import com.intel.gpe.clients.api.exceptions.*;
import com.intel.gpe.clients.impl.jms.AtomicJobClientImpl;

import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;

import java.lang.Exception;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6JobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************/

public class U6JobMonitorAdaptor extends U6JobAdaptorAbstract implements QueryFilteredJob {
        
    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(APPLICATION_NAME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(APPLICATION_NAME, "Bash shell")};
    }
    
    public JobStatus[] getFilteredStatus(Object[] filters) throws TimeoutException, NoSuccessException {
		try {
			// list jobs
			TargetSystemInfo targetSystemInfo = findTargetSystem();
            List<JobClient> jobList = targetSystemInfo.getTargetSystem().getJobs();
            U6JobStatus[] jobListStatus = new U6JobStatus[jobList.size()];
            int index = 0;
            for (Iterator<JobClient> iterator = jobList.iterator(); iterator.hasNext();) {
				JobClient jobClient = iterator.next();
				Status jobStatus = jobClient.getStatus();
				jobListStatus[index] = new U6JobStatus(((AtomicJobClientImpl) jobClient).getId().toString(), jobStatus, jobStatus.toString());
				index ++;
			}
            return jobListStatus;
    	} catch (GPEInvalidResourcePropertyQNameException e) {
    		throw new NoSuccessException(e);
		} catch (GPEResourceUnknownException e) {
			throw new NoSuccessException(e);
		} catch (GPEUnmarshallingException e) {
			throw new NoSuccessException(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccessException(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccessException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

}
