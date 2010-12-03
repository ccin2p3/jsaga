package fr.in2p3.jsaga.adaptor.bes.job.arex;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.bes.job.BesJob;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobStatus;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
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
* File:   BesArexJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

public class BesArexJobMonitorAdaptor extends BesJobMonitorAdaptor implements QueryIndividualJob, ListableJobAdaptor {
        
    public String getType() {
        return "bes-arex";
    }

	public int getDefaultPort() {
		return 2010;
	}

    public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
	protected Class getJobClass() {
		return BesArexJob.class;
	}

	/**
	 * 
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
    protected ActivityStatusType getActivityStatus(GetActivityStatusesResponseType responseStatus) throws NoSuccessException{
		MessageElement[] me_list = responseStatus.getResponse(0).get_any();
		if (me_list != null) {
			for (MessageElement me : responseStatus.getResponse(0).get_any()) {
				if ("Fault".equals(me.getName())) {
					String soap_ns = me.getNamespaceURI();
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
		return super.getActivityStatus(responseStatus);
    }
}
