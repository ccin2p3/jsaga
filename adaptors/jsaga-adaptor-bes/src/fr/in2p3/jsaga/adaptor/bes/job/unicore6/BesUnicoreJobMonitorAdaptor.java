package fr.in2p3.jsaga.adaptor.bes.job.unicore6;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.bes.job.BesJob;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobStatus;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;
import org.ggf.schemas.bes.x2006.x08.besFactory.FactoryResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesUnicoreJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

public class BesUnicoreJobMonitorAdaptor extends BesJobMonitorAdaptor implements QueryIndividualJob, ListableJobAdaptor {
        
    public String getType() {
        return "bes-unicore";
    }

	public int getDefaultPort() {
		return 8080;
	}

    public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
		GetActivityStatusesType requestStatus = new GetActivityStatusesType();
		requestStatus.setActivityIdentifier(new BesJob(nativeJobId).getReferenceEndpoints());
		GetActivityStatusesResponseType responseStatus;
		try {
			responseStatus = _bes_pt.getActivityStatuses(requestStatus);
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
		/*StringWriter writer = new StringWriter();
		try {
			ObjectSerializer.serialize(writer, responseStatus, 
					new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", "GetActivityStatusesResponseType"));
			System.out.println(writer);
		} catch (SerializationException e) {
			e.printStackTrace();
		}*/
		MessageElement[] me_list = responseStatus.getResponse(0).get_any();
		if (me_list != null) {
			for (MessageElement me : responseStatus.getResponse(0).get_any()) {
				if ("Fault".equals(me.getName())) {
					String soap_ns = me.getNamespaceURI();
					/* it is possible that error is sent in the SOAP envelope
  <soapenv:Fault xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Code>
    <soapenv:Value>soap-env:Sender</soapenv:Value>
   </soapenv:Code>
   <soapenv:Reason>
    <soapenv:Text>Missing a-rex:JobID in ActivityIdentifier</soapenv:Text>
   </soapenv:Reason>
   <soapenv:Detail>
    <ns1:UnknownActivityIdentifierFault>
     <ns1:Message>Unrecognized EPR in ActivityIdentifier</ns1:Message>
    </ns1:UnknownActivityIdentifierFault>
   </soapenv:Detail>
  </soapenv:Fault>
				 */
					MessageElement soap_fault = me.getChildElement(new QName(soap_ns, "Reason"));
					if (soap_fault != null) {
						MessageElement me_text = soap_fault.getChildElement(new QName(soap_ns, "Text"));
						if (me_text != null) {
							Text text = (Text)me_text.getChildElements().next();
							throw new NoSuccessException(text.getNodeValue());
						}
					}
				}
			}
		}
		GetActivityStatusResponseType activityStatus = responseStatus.getResponse(0);
    	return new BesJobStatus(nativeJobId, activityStatus.getActivityStatus());
	}

	public String[] list() throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		List<String> urls = new ArrayList<String>();
		GetFactoryAttributesDocumentResponseType r;
		try {
			r = _bes_pt.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
		FactoryResourceAttributesDocumentType attr = r.getFactoryResourceAttributesDocument();
		for (EndpointReferenceType epr: attr.getActivityReference()) {
			urls.add(epr.getAddress().get_value().toString());
		}
		return (String[])urls.toArray(new String[urls.size()]);
	}

}
