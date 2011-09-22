package fr.in2p3.jsaga.adaptor.bes.job;

import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.ogf.saga.error.NoSuccessException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.AttributedURIType;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.ReferenceParametersType;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 Nov 2010
* ***************************************************/

public class BesJob {

	protected String _id = null;
	protected String _id_tag = null;
	protected String _id_tag_ns = null;
	protected String _address = null;
	
	/**
	 * set activity identifier as an xml Element
	 * 
	 * This method parses a XML org.w3c.dom.Element object
	 * It is used by the BesUnicoreJobAdaptor only (submit)
	 * @param xmlActivityIdentifier
	 * @throws SAXException
	 */
	// TODO: remove this method when BesUnicoreJobControlAdaptor.submit will be removed
	public void setActivityId(Element xmlActivityIdentifier) throws SAXException {
        Element addr = (Element) xmlActivityIdentifier.getElementsByTagName("add:Address").item(0);
        if (addr == null)
        	throw new SAXException("<add:Address> tag not found");
		_address = addr.getFirstChild().getTextContent();
		Element ref = (Element) xmlActivityIdentifier.getElementsByTagName("add:ReferenceParameters").item(0);
		if (ref == null)
        	throw new SAXException("<add:ReferenceParameters> tag not found");
		Element id = (Element) ref.getFirstChild();
		_id_tag = id.getTagName();
		_id_tag_ns = id.getAttributes().item(0).getNodeValue();
		_id = id.getFirstChild().getTextContent();
	}
	
	/**
	 * set activity identifier as an EndpointReferenceType
	 * 
	 * This methods parses a fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType object
	 * like:
	 *  <ns1:ActivityIdentifier xsi:type="ns2:EndpointReferenceType" xmlns:ns2="http://www.w3.org/2005/08/addressing">
	 *    	<ns2:Address xsi:type="ns2:AttributedURIType">https://localhost6.localdomain6:8080/DEMO-SITE/services/BESActivity?res=70cc4add-1787-46d7-ad4c-7bc378883294</ns2:Address>
  	 * 		<ns2:ReferenceParameters xsi:type="ns2:ReferenceParametersType">
   	 * 			<unic:ResourceId xmlns:unic="http://www.unicore.eu/unicore6">70cc4add-1787-46d7-ad4c-7bc378883294</unic:ResourceId>
  	 *		</ns2:ReferenceParameters>
 	 * 	</ns1:ActivityIdentifier>
 	 * _address <- https://localhost6.localdomain6:8080/DEMO-SITE/services/BESActivity?res=70cc4add-1787-46d7-ad4c-7bc378883294
 	 * _id_tag <- unic:ResourceId
 	 * _id_tag_ns <- http://www.unicore.eu/unicore6
 	 * _id <- 70cc4add-1787-46d7-ad4c-7bc378883294
	 * @param epr
	 */
	public void setActivityId(EndpointReferenceType epr) {
		_address = epr.getAddress().get_value().toString();
		ReferenceParametersType rpt = epr.getReferenceParameters();
		// Get first ME
		_id_tag = rpt.get_any()[0].getName();
		_id_tag_ns = rpt.get_any()[0].getNamespaceURI();
		_id = rpt.get_any()[0].getFirstChild().getNodeValue();
	}

	/**
	 * set activity identifier as a String
	 * 
	 * This method parses a String like:
	 * <service_address>;<job_id>;<job_id_tag>;<job_id_tag_namespace>
	 * For example:
	 * https://interop.grid.niif.hu:2010/arex-x509;93413166832541139194248;JobID;http://www.nordugrid.org/schemas/a-rex
 	 * _address <- interop.grid.niif.hu:2010/arex-x509
 	 * _id_tag <- JobID
 	 * _id_tag_ns <- http://www.nordugrid.org/schemas/a-rex
 	 * _id <- 93413166832541139194248
	 * @param jobId
	 */
	public void setNativeId(String jobId) {
		String[] _parts = jobId.split(";");
		_address = _parts[0];
		_id = _parts[1];
		_id_tag = _parts[2];
		_id_tag_ns = _parts[3];
	}
	
	/**
	 * returns the native job identifier
	 * @return String the native job identifier
	 */
	public String getNativeId() throws NoSuccessException {
		return _address + ";" + _id + ";" + _id_tag + ";" + _id_tag_ns;
	}
	
	/**
	 * returns the BES activity identifier (EndpointReferenceType)
	 * @return EndpointReferenceType the BES activity identifier
	 */
	public EndpointReferenceType getActivityIdentifier() throws NoSuccessException {
		EndpointReferenceType _job_endpoint = new EndpointReferenceType();
		_job_endpoint.setAddress(new AttributedURIType(_address));
		
		MessageElement[] msg_elements = new MessageElement[1];

		MessageElement _msg_element_jobid = new MessageElement(_id_tag, "ns1", _id_tag_ns);
		try {
			_msg_element_jobid.addTextNode(_id);
		} catch (SOAPException e) {
			throw new NoSuccessException(e);
		}
		msg_elements[0] = _msg_element_jobid;
		
		ReferenceParametersType rpt = new ReferenceParametersType();
		rpt.set_any(msg_elements);
		
		_job_endpoint.setReferenceParameters(rpt);
        return _job_endpoint;
	}
	
}
