package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.ggf.schemas.bes.x2006.x08.besFactory.FactoryResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

public class BesJobMonitorAdaptor extends BesJobAdaptorAbstract implements QueryIndividualJob, ListableJobAdaptor {
        
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
