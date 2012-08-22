package fr.in2p3.jsaga.adaptor.ourgrid.job;

import java.util.Date;
import java.util.Map;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/

public class OurGridJobMonitorAdaptor extends OurGridAbstract implements JobMonitorAdaptor, QueryIndividualJob, JobInfoAdaptor {

	private String authentication;
	private Client client;
	private WebResource webResource ;
	private String host;
	private String path;
	private final String STATUS_JOB_ID = "/status/job_id/";
	private final String STATUS_WORKERNAME = "/status/workername/";
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	
	
	public String getHost() {
		
		return host;
	}

	public void setHost(String host) {
		
		this.host = host;
	}

	public String getAuthentication() {
		
		return authentication;
	}

	public void setAuthentication(String authentication) {
		
		this.authentication = authentication;
	}

	public Client getClient() {
		
		return client;
	}

	public void setClient(Client client) {
		
		this.client = client;
	}

	public WebResource getWebResource() {
		
		return webResource;
	}

	public void setWebResource(WebResource webResource) {
		
		this.webResource = webResource;
	}

	public void connect(String userInfo, String host, int port,String basePath, Map attributes) 
			throws NotImplementedException,AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException,
			NoSuccessException {

		setClient(Client.create());
		setHost(host);
		String resource = OurGridConstants.HTTP + getHost();
		setWebResource(getClient().resource(resource));
		setAuthentication(new String(Base64.encode(m_account + ":" + m_passPhrase)));

	}


	/**
	 * Gets the status of the job matching a job id.
	 * 
	 * @param nativeJobId Identifier of the job
	 * @return jobStatus Returns the status of the job
	 */
	public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
		
		String nativeStatus = null;
		setPath(STATUS_JOB_ID + nativeJobId);
		OurGridJobStatus jobStatus = null;
		String authorization = OurGridConstants.BASIC + " " + getAuthentication();
		
		ClientResponse response = getWebResource().path(getPath()).
		header(OurGridConstants.AUTHORIZATION, authorization).get(ClientResponse.class);
		

		if (response.getStatus()==401){
			try {
				throw new PermissionDeniedException(response.getClientResponseStatus().getReasonPhrase());
			} catch (PermissionDeniedException e) {
				
				e.printStackTrace();
			}
		}else{
			
			response.getClass();
			nativeStatus=response.getEntity(String.class);
			jobStatus = new OurGridJobStatus(nativeJobId,nativeStatus);
		}
		return jobStatus;

	}

	public String[] getExecutionHosts(String nativeJobId)
			throws NotImplementedException, NoSuccessException{

		String workerName = null;
		String executionHosts[] = null;
		setPath(STATUS_WORKERNAME + nativeJobId);
		String authorization = OurGridConstants.BASIC + " " + getAuthentication();
		
		ClientResponse response = getWebResource().path(getPath()).
				header(OurGridConstants.AUTHORIZATION, authorization).get(ClientResponse.class);
		if (response.getStatus()==401){
			try {
				throw new PermissionDeniedException(response.getClientResponseStatus().getReasonPhrase());
			} catch (PermissionDeniedException e) {
				
				e.printStackTrace();
			}
		}else{
			
		response.getClass();	
		workerName=response.getEntity(String.class).replaceAll("^\\[|\\]$|[\'\"]","");
		executionHosts=workerName.trim().split(",");
		}
		return executionHosts;
	}
	

	
	public Date getCreated(String nativeJobId) throws NotImplementedException,NoSuccessException {

		return null;
	}


	public Integer getExitCode(String nativeJobId) throws NotImplementedException,	NoSuccessException {

		return null;
		
	}
	public Date getFinished(String nativeJobId) throws NotImplementedException, NoSuccessException {

		return null;
		
	}
	
	public Date getStarted(String nativeJobId) throws NotImplementedException,	NoSuccessException {

		return null;
		
	}
}