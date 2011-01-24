package fr.in2p3.jsaga.adaptor.bes_unicore.job;

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
* Date:   9 déc. 2010
* ***************************************************/

public class BesUnicoreJob extends BesJob {

	protected EndpointReferenceType toActivityIdentifier() throws NoSuccessException {
		_job_endpoint = new EndpointReferenceType();
		_job_endpoint.setAddress(new AttributedURIType(_job_nativeId));
        return _job_endpoint;
	}
	
	protected String toStringIdentifier() throws NoSuccessException {
		_job_nativeId = _job_endpoint.getAddress().get_value().toString();
		return _job_nativeId;
	}
}
