package fr.in2p3.jsaga.adaptor.bes.job;

import org.ogf.saga.error.NoSuccessException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 Nov 2010
* ***************************************************/

public abstract class BesJob {

	protected EndpointReferenceType _job_endpoints = null;
	protected String _job_nativeId = null;
	
	/**
	 * convert the native job identifier (URL as String) into a BES activity identifier (EndpointReferenceType)
	 * @return EndpointReferenceType the activityIdentifier
	 */
	protected abstract EndpointReferenceType toActivityIdentifier() throws NoSuccessException;
	
	/**
	 * convert the BES activity identifier (EndpointReferenceType) into a native job identifier (URL as String)
	 * @return String the native Job identifier
	 */
	protected abstract String toStringIdentifier() throws NoSuccessException;
	
	/**
	 * Initialize the native job identifier
	 * @param nativeJobId the native job identifier
	 */
	public void setNativeJobId(String nativeJobId) {
		_job_nativeId = nativeJobId;
	}
	
	/**
	 * Initialize the BES activity identifier (EndpointReferenceType)
	 * @param epr the BES activity identifier
	 */
	public void setActivityIdentifier(EndpointReferenceType epr) {
		_job_endpoints = epr;
	}
	
	/**
	 * returns the native job identifier
	 * @return String the native job identifier
	 */
	public String getNativeJobID() throws NoSuccessException {
		if (_job_nativeId != null) 
			return _job_nativeId;
		else
			return toStringIdentifier();
	}
	
	/**
	 * returns the BES activity identifier (EndpointReferenceType)
	 * @return EndpointReferenceType the BES activity identifier
	 */
	public EndpointReferenceType getActivityIdentifier() throws NoSuccessException {
		if (_job_endpoints != null) 
			return _job_endpoints;
		else
			return toActivityIdentifier();
	}
}
