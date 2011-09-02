package fr.in2p3.jsaga.adaptor.unicore.job;

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
	
	/*
	public UnicoreJob(String jobNativeID, IUASSecurityProperties sec) throws Exception {
		EndpointReferenceType _epr = EndpointReferenceType.Factory.newInstance();
	    _epr.addNewAddress().setStringValue(jobNativeID);
		this(_epr,sec);
	}
	*/
	
	public String getNativeJobID() {
		return m_client.getUrl();
	}
	
	public int getExitCode() {
		return m_client.getExitCode();
	}
	
	public JobStatus getStatus() throws Exception {
		Integer rc = m_client.getExitCode();
		if (rc != null) {
			return new UnicoreJobStatus(getNativeJobID(), m_client.getStatus(), "Message Unknown", rc);
		} else {
			return new UnicoreJobStatus(getNativeJobID(), m_client.getStatus(), "Message Unknown");
		}
	}
}
