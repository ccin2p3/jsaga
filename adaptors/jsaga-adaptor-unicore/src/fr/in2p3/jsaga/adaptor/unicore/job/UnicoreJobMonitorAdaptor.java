package fr.in2p3.jsaga.adaptor.unicore.job;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.unigrids.x2006.x04.services.tss.JobReferenceDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.EnumerationClient;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.client.TSSClient;

import fr.in2p3.jsaga.adaptor.unicore.UnicoreAbstract;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   01/09/2011
* ***************************************************/
public class UnicoreJobMonitorAdaptor extends UnicoreJobAdaptorAbstract implements
		QueryIndividualJob, QueryListJob, ListableJobAdaptor, JobInfoAdaptor {

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	try {
		    // Find a target system
			TSFClient cl = new TSFClient(m_epr.getAddress().getStringValue(), m_epr, m_uassecprop);
			Iterator<EndpointReferenceType> flavoursIter = cl.getAccessibleTargetSystems().iterator(); 
			if (flavoursIter.hasNext()) {
				EndpointReferenceType _tss_epr = flavoursIter.next();
		        m_client = new TSSClient(_tss_epr.getAddress().getStringValue(), _tss_epr, m_uassecprop);
		    } else {
		    	throw new NoSuccessException("No target system found");
		    }
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    }

	public JobStatus getStatus(String nativeJobId) throws TimeoutException,
			NoSuccessException {
		try {
			UnicoreJob uj = new UnicoreJob(nativeJobId, m_uassecprop);
			return uj.getStatus();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public JobStatus[] getStatusList(String[] nativeJobIdArray) throws TimeoutException, NoSuccessException {
		try {
			JobStatus[] tab = new JobStatus[nativeJobIdArray.length];
			for (int i=0; i<nativeJobIdArray.length; i++) {
				tab[i] = new UnicoreJob(nativeJobIdArray[i], m_uassecprop).getStatus();
			}
			return tab;
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public String[] list() throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {
			EnumerationClient<org.unigrids.x2006.x04.services.tss.JobReferenceDocument> list = m_client.getJobReferenceEnumeration();
			long nb=0;
			String[] l = null;
			if (list != null) { // not supported by server < 6.3.0
				nb = list.getNumberOfResults();
				l = new String[(int) nb];
				int index=0;
				for (Iterator<JobReferenceDocument> i = list.iterator(); i.hasNext(); ) {
					JobReferenceDocument jrd = i.next();
					l[index++] = jrd.getJobReference().getAddress().getStringValue();
				}
			} else {
				List<EndpointReferenceType> list2 = m_client.getJobs();
				nb = list2.size();
				l = new String[(int) nb];
				for (int index=0; index<nb; index++) {
					l[index] = list2.get(index).getAddress().getStringValue();
				}
			}
			return l;
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public Integer getExitCode(String nativeJobId) throws NotImplementedException, NoSuccessException {
		try {
			return new UnicoreJob(nativeJobId, m_uassecprop).getExitCode();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getCreated(String nativeJobId) throws NotImplementedException, NoSuccessException {
		try {
			return new UnicoreJob(nativeJobId, m_uassecprop).getSubmissionTime().getTime();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException, NoSuccessException {
		throw new NotImplementedException();
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException, NoSuccessException {
		try {
			return new UnicoreJob(nativeJobId, m_uassecprop).getTerminationTime().getTime();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public String[] getExecutionHosts(String nativeJobId) throws NotImplementedException, NoSuccessException {
		throw new NotImplementedException();
	}

}
