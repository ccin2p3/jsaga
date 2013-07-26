package fr.in2p3.jsaga.adaptor.wms.job;

import holders.StringArrayHolder;

import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.holders.JobStatusArrayHolder;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryFilteredJob;
import org.glite.wsdl.types.lb.JobFlags;

public class WMSJobMonitorEnhancedAdaptor extends WMSJobMonitorAdaptor implements QueryFilteredJob{

	/**
	 * For QueryFilteredJob: Problematic when the user has thousands of jobs
	 * registered in the LB. Out of memory exceptions can happened.
	 */
	public JobStatus[] getFilteredStatus(Object[] filters) throws TimeoutException, NoSuccessException {
		try {
			// get stub
			LoggingAndBookkeepingPortType stub = getLBStub(m_credential);
			
	        // get Jobs Status
			JobStatusArrayHolder jobStatusResult = new JobStatusArrayHolder();
	        StringArrayHolder jobNativeIdResult = new StringArrayHolder();
	        stub.userJobs(jobNativeIdResult, jobStatusResult);
	        if(jobNativeIdResult != null && jobNativeIdResult.value != null) {
	        	JobStatus[] filterJobs = new WMSJobStatus[jobNativeIdResult.value.length];
	        	for (int i = 0; i < filterJobs.length; i++) {
                    org.glite.wsdl.types.lb.JobStatus jobInfo = jobStatusResult.value[i];
	        		filterJobs[i] = new WMSJobStatus(jobNativeIdResult.value[i], jobInfo);
				}
		        return filterJobs;
	        }
	        // TODO : exception or null ?
	        return null;
    	} catch (Exception e) {
    		throw new NoSuccessException(e);
    	}
	}
}
