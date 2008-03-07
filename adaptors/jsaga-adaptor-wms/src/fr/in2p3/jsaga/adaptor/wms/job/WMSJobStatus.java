package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

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
    public WMSJobStatus(String jobId, StatName state, String stateString) {
        super(jobId, state, stateString);
    }

    public WMSJobStatus(String jobId, StatName state, String stateString, String cause) {
        super(jobId, state, stateString, cause);
    }
    
    public String getModel() {
        return "TODO";
    }

    public SubState getSubState() {
    	String jobState = ((StatName) m_nativeStateCode).getValue();
    	if(jobState.equals(StatName._RUNNING)) {
            return SubState.RUNNING_ACTIVE; }
    	else if(jobState.equals(StatName._ABORTED))  {
    		return SubState.FAILED_ABORTED; }
    	else if(jobState.equals(StatName._CANCELLED)) {
    		return SubState.CANCELED; }
		else if(jobState.equals(StatName._CLEARED)) {
			return SubState.DONE; }
		else if(jobState.equals(StatName._DONE)) {
			return SubState.DONE; }
		else if(jobState.equals(StatName._PURGED)) {
			return SubState.DONE; }
		else if(jobState.equals(StatName._READY)) {
			return SubState.SUBMITTED; }
		else if(jobState.equals(StatName._SCHEDULED)) {
			return SubState.RUNNING_QUEUED; }
		else if(jobState.equals(StatName._SUBMITTED)) {
			return SubState.SUBMITTED; }
		else if(jobState.equals(StatName._WAITING)) {
			return SubState.SUBMITTED; }
		else {
			if(jobState.equals(StatName._UNKNOWN)) 
				System.err.println("Invalid state : UNKNOWN");
			else
				System.err.println("Invalid state :"+jobState);
			return null;
		}
    }
}
