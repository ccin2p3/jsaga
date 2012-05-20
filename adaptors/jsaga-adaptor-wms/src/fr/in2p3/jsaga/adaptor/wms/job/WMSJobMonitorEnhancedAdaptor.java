package fr.in2p3.jsaga.adaptor.wms.job;

import holders.StringArrayHolder;

import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.JobFlagsValue;
import org.glite.wsdl.types.lb.QueryAttr;
import org.glite.wsdl.types.lb.QueryConditions;
import org.glite.wsdl.types.lb.QueryOp;
import org.glite.wsdl.types.lb.QueryRecValue;
import org.glite.wsdl.types.lb.QueryRecord;
import org.glite.wsdl.types.lb.holders.JobStatusArrayHolder;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryFilteredJob;

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
            JobFlagsValue[] jobFlagsValue = new JobFlagsValue[1];
            jobFlagsValue[0] = JobFlagsValue.CLASSADS;
            //JobFlags jobFlags = new JobFlags(jobFlagsValue);
	        
            JobStatusArrayHolder jobStatusResult = new JobStatusArrayHolder();
	        StringArrayHolder jobNativeIdResult = new StringArrayHolder();
	       
	        QueryConditions[] queryConditions = new  QueryConditions[1];
	        queryConditions[0] = new QueryConditions();
	        queryConditions[0].setAttr(QueryAttr.JOBID);
	        
	        QueryRecord[] qR = new QueryRecord[1];
	        QueryRecValue value1 = new QueryRecValue();
	        value1.setC("https://"+m_lbHost+"/");
	        qR[0] = new QueryRecord(QueryOp.UNEQUAL, value1, null );	        
	        queryConditions[0].setRecord(qR);	        
	        // Cannot use stub.userJobs() because not yet implemented (version > 1.8 needed)
	        stub.queryJobs(queryConditions, jobFlagsValue, jobNativeIdResult, jobStatusResult);
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
