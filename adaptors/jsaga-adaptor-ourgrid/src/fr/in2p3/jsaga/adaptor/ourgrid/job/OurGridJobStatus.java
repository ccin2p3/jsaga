package fr.in2p3.jsaga.adaptor.ourgrid.job;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/

/**
 * The OurGridJobStatus class defines the status that a job can be and 
 * it's used by the monitor class to provide the job status
 * @author patriciaam
 *
 */
public class OurGridJobStatus extends JobStatus {

	/**
	 * Constructor of the OurGridJobStatus of the class
	 * @param jobId
	 * @param nativeStateString
	 */
	public OurGridJobStatus(String jobId, String nativeStateString) {

		super(jobId, nativeStateString, nativeStateString);
	}


	public String getModel() {

		return OurGridConstants.TYPE_ADAPTOR;
	}

	/**
	 * Get the implementation-specific but middleware-independant state of the job
	 * In addition to SAGA states, this methods 
	 * may return states such as 
	 * PRE_STAGING, POST_STAGING, QUEUED, FAILED_ERROR and FAILED_ABORTED
	 * @return Returns the JSAGA state of the job
	 */
	public SubState getSubState() {

		String jobState = m_nativeStateString;

		if(jobState.equals(OurGridJobState.CANCELLED.getStateDescription())){

			return SubState.CANCELED;
		}else
		{   
			if (jobState.equals(OurGridJobState.FINISHED.getStateDescription())){

				return SubState.DONE;
			}
			else {
				if (jobState.equals(OurGridJobState.RUNNING.getStateDescription())){

					return SubState.RUNNING_ACTIVE;
				}
				else {
					if (jobState.equals(OurGridJobState.UNSTARTED.getStateDescription())){

						return SubState.RUNNING_PRE_STAGING;
					}else { 

						if (jobState.equals(OurGridJobState.FAILED.getStateDescription())){

							return SubState.FAILED_ERROR;
						}else{
							return null;
						}


					}
				}
			}
		}
	}
}
