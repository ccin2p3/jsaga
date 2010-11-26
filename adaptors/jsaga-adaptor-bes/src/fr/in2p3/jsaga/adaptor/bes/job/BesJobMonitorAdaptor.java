package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.rmi.RemoteException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

public class BesJobMonitorAdaptor extends BesJobAdaptorAbstract implements QueryIndividualJob {
        
    public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
		GetActivityStatusesType requestStatus = new GetActivityStatusesType();
		EndpointReferenceType epr[] = new EndpointReferenceType[1];
        EndpointReferenceType e= new EndpointReferenceType();
        e.setAddress(new AttributedURIType(nativeJobId));
		epr[0]= e;
		requestStatus.setActivityIdentifier(epr);
		GetActivityStatusesResponseType responseStatus;
		try {
			responseStatus = _bes_pt.getActivityStatuses(requestStatus);
		} catch (InvalidRequestMessageFaultType e1) {
			throw new NoSuccessException(e1);
		} catch (RemoteException e1) {
			throw new NoSuccessException(e1);
		}
		GetActivityStatusResponseType activityStatusArray = responseStatus.getResponse(0);
		
    	return new BesJobStatus(nativeJobId, activityStatusArray.getActivityStatus());
	}

}
