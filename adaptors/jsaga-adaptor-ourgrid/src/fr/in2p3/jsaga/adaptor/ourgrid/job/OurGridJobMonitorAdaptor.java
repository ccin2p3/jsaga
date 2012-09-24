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
/**
 * The OurGridJobMonitorAdaptor class is responsible for retrieving the job status
 * also remains monitoring the job until its completion
 */
public class OurGridJobMonitorAdaptor extends OurGridAbstract implements JobMonitorAdaptor, QueryIndividualJob, JobInfoAdaptor {

	private String authentication;
	private Client client;
	private WebResource webResource ;
	private String host;
	private String path;
	private final String STATUS_JOB_ID = "/status/job_id/";
	private final String STATUS_WORKERNAME = "/status/workername/";
	
	/**
	 * Gets the relative path of the API's resource 
	 * @return path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Path is a relative path which is used to access the API's resources 
	 * @param path relative path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	
	/**
	 * Returns the name of the host 
	 * @return host server address
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host name
	 * @param host server address
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Gets the authentication to the server
	 * @return aurhenticathion username and a password encoded
	 */
	public String getAuthentication() {
		return authentication;
	}

	/**
	 * Authentication consists of a encoded string used to authenticate to the server
	 * @param authentication username and a password encoded
	 */
	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	/**
	 * Gets a client for the the RESTful API 
	 * @return client 
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Client is used to interoperate with the RESTful API
	 * @param client
	 */
	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * Returns the webResource used to make requests and proccess responses
	 * @return webResource
	 */
	public WebResource getWebResource() {
		return webResource;
	}

	/**
	 * WebResource used to build requests and process responses from the API
	 * @param webResource
	 */
	public void setWebResource(WebResource webResource) {
		this.webResource = webResource;
	}

	/**
	 *  Connects to the server and initializes the connection with the provided attributes
	 *  @param userInfo the user login
	 *  @param host the server
	 *  @param port the port 
	 *  @param basePath the base path
	 *  @param attributes the provided attributes
	 */
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
	 * Gets the status of the job matching a job id
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

	/**
	 * Gets the execution host. Several hosts may be returned if the job is a parallel job
	 * @param nativeJobId Identifier of the job
	 * @return executionHosts Returns the array of execution hosts
	 * @throws NotImplementedException
	 * @throws NoSuccessException
	 */
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
	

	/**
	 * Returns the job creation time
	 * @param nativeJobId  the identifier of the job in the grid
	 * @return
	 * @throws NotImplementedException
	 * @throws NoSuccessException
	 */
	public Date getCreated(String nativeJobId) throws NotImplementedException,NoSuccessException {

		return null;
	}

	/**
	 * Returns the exit code of the job
	 * @param nativeJobId  the identifier of the job in the grid
	 * @return
	 * @throws NotImplementedException
	 * @throws NoSuccessException
	 */
	public Integer getExitCode(String nativeJobId) throws NotImplementedException,	NoSuccessException {

		return null;
		
	}
	/**
	 * Returns the job end time
	 * @param nativeJobId  the identifier of the job in the grid
	 * @return
	 * @throws NotImplementedException
	 * @throws NoSuccessException
	 */
	public Date getFinished(String nativeJobId) throws NotImplementedException, NoSuccessException {

		return null;
		
	}
	
	/**
	 * Returns the job statup time
	 * @param nativeJobId  the identifier of the job in the grid
	 * @return
	 * @throws NotImplementedException
	 * @throws NoSuccessException
	 */
	public Date getStarted(String nativeJobId) throws NotImplementedException,	NoSuccessException {

		return null;
		
	}
}