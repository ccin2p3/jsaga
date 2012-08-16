package fr.in2p3.jsaga.adaptor.ourgrid.job;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/

/**
 * status of the job
 */
public enum OurGridJobState {
	
	UNSTARTED("[UNSTARTED]"),
	RUNNING("[RUNNING]"), 
	FINISHED("[FINISHED]"),
	CANCELLED("[CANCELLED]");
	
	
	private String stateDescription;

	/**
	 * @param stateDescription
	 *            Describes the status of the job
	 * */
	private OurGridJobState(String stateDescription) {
		
		this.stateDescription = stateDescription;
	}

	public String getStateDescription() {
		
		return stateDescription;
	}
}