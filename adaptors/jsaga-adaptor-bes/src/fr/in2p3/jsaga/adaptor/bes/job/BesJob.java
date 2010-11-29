package fr.in2p3.jsaga.adaptor.bes.job;

import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 Nov 2010
* ***************************************************/

public class BesJob {

	protected EndpointReferenceType[] _job_endpoints;
	
	public BesJob(String nativeJobId) {
		_job_endpoints = new EndpointReferenceType[1];
        EndpointReferenceType rep= new EndpointReferenceType();
        rep.setAddress(new AttributedURIType(nativeJobId));
        _job_endpoints[0]= rep;
	}

	public EndpointReferenceType[] getReferenceEndpoints() {
        return _job_endpoints;
    }
}
