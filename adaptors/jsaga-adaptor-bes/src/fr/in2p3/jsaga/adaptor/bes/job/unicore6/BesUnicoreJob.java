package fr.in2p3.jsaga.adaptor.bes.job.unicore6;

import org.ogf.saga.error.NoSuccessException;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import fr.in2p3.jsaga.adaptor.bes.job.BesJob;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesUnicoreJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 Nov 2010
* ***************************************************/

public class BesUnicoreJob extends BesJob {

	protected EndpointReferenceType toActivityIdentifier() throws NoSuccessException {
		_job_endpoints = new EndpointReferenceType();
		_job_endpoints.setAddress(new AttributedURIType(_job_nativeId));
        return _job_endpoints;
	}
	
	protected String toStringIdentifier() throws NoSuccessException {
		_job_nativeId = _job_endpoints.getAddress().get_value().toString();
		return _job_nativeId;
	}
}
