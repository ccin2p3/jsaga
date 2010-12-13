package fr.in2p3.jsaga.adaptor.arex.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import java.util.Map;

import javax.xml.namespace.QName;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesArexJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   9 déc 2010
* ***************************************************/

public class ArexJobMonitorAdaptor extends BesJobMonitorAdaptor {
        
    public String getType() {
        return "arex";
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
		return ArexJob.class;
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
	/*
	 * obsolete
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
    }*/
}
