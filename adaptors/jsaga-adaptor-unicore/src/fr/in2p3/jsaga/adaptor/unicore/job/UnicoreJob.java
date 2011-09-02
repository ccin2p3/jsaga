package fr.in2p3.jsaga.adaptor.unicore.job;

import java.util.Calendar;

import org.unigrids.services.atomic.types.StatusInfoType;
import org.unigrids.x2006.x04.services.jms.JobPropertiesDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.security.IUASSecurityProperties;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   01/09/2011
* ***************************************************/

public class UnicoreJob {
	private JobClient m_client;
	
	public UnicoreJob(JobClient jc) throws Exception {
		m_client = jc;
	}
	
	public UnicoreJob(EndpointReferenceType address, IUASSecurityProperties sec) throws Exception {
		this(new JobClient(address.getAddress().getStringValue(), address, sec));
	}
	
	public UnicoreJob(String jobNativeID, IUASSecurityProperties sec) throws Exception {
		EndpointReferenceType _epr = EndpointReferenceType.Factory.newInstance();
	    _epr.addNewAddress().setStringValue(jobNativeID);
		m_client = new JobClient(_epr.getAddress().getStringValue(), _epr, sec);
	}
	
	public String getNativeJobID() {
		return m_client.getUrl();
	}
	
	public int getExitCode() {
		return m_client.getExitCode();
	}
	
	public Calendar getSubmissionTime() {
		return m_client.getSubmissionTime();
	}
	
	public JobStatus getStatus() throws Exception {
		StatusInfoType st = m_client.getResourcePropertiesDocument().getJobProperties().getStatusInfo();
		// in some cases (jobs sleep), the status sent is FAILED with "Could not update status"
		// whereas the jobs continues to run
		// we need to throw an exception, otherwise the user thinks its job is failed
		if (st.getDescription().contains("Could not update status")) {
			throw new Exception("Could not get status: "+st.getDescription());
		}
		Integer rc = null;
		try {
			rc = m_client.getExitCode();
		} catch (Exception e) {
			// ignore if exit code is not available yet
		}
		if (rc != null) {
			return new UnicoreJobStatus(getNativeJobID(), st, rc);
		} else {
			return new UnicoreJobStatus(getNativeJobID(), st);
		}
	}
	
	public void cancel() throws Exception {
		m_client.abort();
	}
	
	public void hold() throws Exception {
		m_client.hold();
	}
	
	public void release() throws Exception {
		m_client.resume();
	}
}
