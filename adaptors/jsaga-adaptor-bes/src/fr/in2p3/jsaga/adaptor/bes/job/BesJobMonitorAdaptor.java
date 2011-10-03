package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;

import org.apache.log4j.Logger;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

/**
 * This class is the abstract class for the JobMonitor specific to a BES implementation
 */
public class BesJobMonitorAdaptor extends BesJobAdaptorAbstract implements QueryIndividualJob, QueryListJob, ListableJobAdaptor {
      
	/**
	 * Instanciate the appropriate JobStatus object
	 * 
	 * @param nativeJobId  the native Job Identifier
	 * @param ast  the ActivityStatusType object containing the status of the job
	 * @return the appropriate JobStatus object
	 * @throws NoSuccessException
	 * @see ArexJobStatus
	 * @see BesUnicoreJobStatus
	 * @see GenesisIIJobStatus
	 */
	protected JobStatus getJobStatus(String nativeJobId, ActivityStatusType ast) throws NoSuccessException {
		return new BesJobStatus(nativeJobId, ast);
	}
	
	// Implementation of the QueryIndividualJob interface
    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
    	GetActivityStatusResponseType[] responseStatus = getActivityStatuses(new String[]{nativeJobId});
    	return getJobStatus(nativeJobId, responseStatus[0].getActivityStatus());
	}

    // Implementation of the QueryListJob interface
	public JobStatus[] getStatusList(String[] nativeJobIdArray) throws TimeoutException, NoSuccessException {
    	GetActivityStatusResponseType[] responseStatus = getActivityStatuses(nativeJobIdArray);
		JobStatus[] statusArray = new JobStatus[responseStatus.length];
		for (int i=0; i<responseStatus.length; i++) {
				statusArray[i] = getJobStatus(nativeJobIdArray[i], responseStatus[i].getActivityStatus());
		}
		return statusArray;
	}
	
	// Implementation of the ListableJobAdaptor interface
	public String[] list() throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		List<String> urls = new ArrayList<String>();
		GetFactoryAttributesDocumentResponseType r;
		try {
			r = _bes_pt.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
			Logger.getLogger(BesJobMonitorAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(r));
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
		EndpointReferenceType[] refs = r.getFactoryResourceAttributesDocument().getActivityReference();
		if (refs != null) {
			for (EndpointReferenceType epr: refs) {
				urls.add(activityId2NativeId(epr));
			}
		}
		return (String[])urls.toArray(new String[urls.size()]);
	}

	/**
	 * Get a list of statuses
	 * 
	 * Send a GetActivityStatusesType message with a list of jobs 
	 * and receive a GetActivityStatusesResponseType response with the list of statuses
	 * 
	 * @param nativeJobIdArray an array of native Jobs Identifiers
	 * @return an array of GetActivityStatusResponseType
	 * @throws NoSuccessException
	 */
	private GetActivityStatusResponseType[] getActivityStatuses(String[] nativeJobIdArray) throws NoSuccessException{
		try {
			GetActivityStatusesType requestStatus = new GetActivityStatusesType();
			EndpointReferenceType[] refs = new EndpointReferenceType[nativeJobIdArray.length];
			int i=0;
			for (String nativeJobId: nativeJobIdArray) {
				refs[i++] = nativeId2ActivityId(nativeJobId);
			}
			requestStatus.setActivityIdentifier(refs);
			Logger.getLogger(BesJobMonitorAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(requestStatus));
			GetActivityStatusesResponseType responseStatus = _bes_pt.getActivityStatuses(requestStatus);
			Logger.getLogger(BesJobMonitorAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(responseStatus));

			return responseStatus.getResponse();
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
    	
    }
    
	
}
