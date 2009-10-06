package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import org.apache.log4j.Logger;
import org.glite.wsdl.types.lb.StatName;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramrJobStatus
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WMSJobStatus extends JobStatus {
	private static Logger s_logger = Logger.getLogger(WMSJobStatus.class);

    public WMSJobStatus(String jobId, org.glite.wsdl.types.lb.JobStatus jobInfo) {
        super(jobId, jobInfo.getState(), jobInfo.getState().getValue(), jobInfo.getReason());
        if(s_logger.isDebugEnabled())
        	s_logger.debug("Status for job '"+jobId+"':"+jobInfo.getState().getValue());
    }
    
    public String getModel() {
        return "TODO";
    }

    public SubState getSubState() {
    	String jobState = ((StatName) m_nativeStateCode).getValue();

        if(jobState.equals(StatName._SUBMITTED)) {
            // The job has been submitted by the user but not yet processed by the Network Server
            return SubState.RUNNING_SUBMITTED; }
        else if(jobState.equals(StatName._WAITING)) {
            // The job has been accepted by the Network Server but not yet processed by the Workload Manager
            return SubState.RUNNING_SUBMITTED; }
        else if(jobState.equals(StatName._READY)) {
            // The job has been assigned to a Computing Element but not yet transferred to it
            return SubState.RUNNING_SUBMITTED; }

		else if(jobState.equals(StatName._SCHEDULED)) {
            // The job is waiting in the Computing Element's queue
			return SubState.RUNNING_QUEUED; }
    	else if(jobState.equals(StatName._RUNNING)) {
            // The job is running
            return SubState.RUNNING_ACTIVE; }
        else if(jobState.equals(StatName._DONE)) {
            // The job has finished, but Output Sandbox has not been transfered yet...
            return SubState.RUNNING_POST_STAGING; }

    	else if(jobState.equals(StatName._ABORTED))  {
            // The job has been aborted by the WMS (e.g. because it was too long, or the proxy certificated expired, etc.)
    		return SubState.FAILED_ABORTED; }
    	else if(jobState.equals(StatName._CANCELLED)) {
            // The job has been cancelled by the user
    		return SubState.CANCELED; }
		else if(jobState.equals(StatName._CLEARED)) {
            // The Output Sandbox has been transferred to the User Interface
			return SubState.DONE; }
		else if(jobState.equals(StatName._PURGED)) {
            // ???
			return SubState.DONE; }

		else {
			if(jobState.equals(StatName._UNKNOWN)) 
				System.err.println("Invalid state : UNKNOWN");
			else
				System.err.println("Invalid state :"+jobState);
			return null;
		}
    }
}
