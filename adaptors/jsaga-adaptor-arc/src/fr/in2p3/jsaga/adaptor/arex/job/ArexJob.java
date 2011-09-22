package fr.in2p3.jsaga.adaptor.arex.job;

import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.ogf.saga.error.NoSuccessException;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.AttributedURIType;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.ReferenceParametersType;

import fr.in2p3.jsaga.adaptor.bes.job.BesJob;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ArexJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   9 déc 2010
* ***************************************************/

/**
 * @obsolete
 */
public class ArexJob extends BesJob {

	/*
	protected EndpointReferenceType toActivityIdentifier() throws NoSuccessException {
		_job_endpoint = new EndpointReferenceType();
		// split NativeJobID in serviceURL and jobId
		int lastSlash = _job_nativeId.lastIndexOf("/");
		String service = _job_nativeId.substring(0, lastSlash);
		String jobId = _job_nativeId.substring(lastSlash+1);
		_job_endpoint.setAddress(new AttributedURIType(service));
		
		MessageElement[] msg_elements = new MessageElement[1];

		MessageElement _msg_element_jobid = new MessageElement("JobID", "x", "http://www.nordugrid.org/schemas/a-rex");
		try {
			_msg_element_jobid.addTextNode(jobId);
		} catch (SOAPException e) {
			throw new NoSuccessException(e);
		}
		msg_elements[0] = _msg_element_jobid;
		
		ReferenceParametersType rpt = new ReferenceParametersType();
		rpt.set_any(msg_elements);
		
		_job_endpoint.setReferenceParameters(rpt);
        return _job_endpoint;
	}
	
	protected String toStringIdentifier() throws NoSuccessException {
		_job_nativeId = null;
		ReferenceParametersType rpt = _job_endpoint.getReferenceParameters();
		for (MessageElement me: rpt.get_any()) {
			if ("JobSessionDir".equals(me.getName())) {
				_job_nativeId = me.getFirstChild().getNodeValue();
			}
		}
		return _job_nativeId;
	}
	*/
}
