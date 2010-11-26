package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryFilteredJob;
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

/*
import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.Status;
import com.intel.gpe.clients.api.exceptions.*;
import com.intel.gpe.clients.impl.jms.AtomicJobClientImpl;

import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;
*/

import java.lang.Exception;
import java.rmi.RemoteException;
import java.util.Iterator;
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

public class BesJobMonitorAdaptor extends BesJobAdaptorAbstract implements QueryIndividualJob {
        
    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(APPLICATION_NAME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(APPLICATION_NAME, "Bash shell")};
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
		
		//String state= activityStatusArray.getActivityStatus().getState().getValue();
    	return new BesJobStatus(nativeJobId, activityStatusArray.getActivityStatus());
	}

}
