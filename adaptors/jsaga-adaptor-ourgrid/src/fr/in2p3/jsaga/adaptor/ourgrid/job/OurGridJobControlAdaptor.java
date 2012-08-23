package fr.in2p3.jsaga.adaptor.ourgrid.job;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/

/**
 * The OurGridJobControlAdaptor class handles the submission, cancellation and cleaning of a job
 * @author patriciaam
 *
 */
public class OurGridJobControlAdaptor extends OurGridAbstract implements
JobControlAdaptor, CleanableJobAdaptor, StagingJobAdaptorOnePhase {

	private final String CLEAN = "/clean/";
	private final String CANCEL = "/cancel/";
	private final String SUBMIT = "/addjob/";
	private final String JOB_TYPE= "job";
	private final String SUBMIT_ERROR = "Failed: HTTP error code:";
	private final String TAG_TYPE = "transferFiles";
	private final String TAG_ELEMENT = "TransferFiles";
	private final String TAG_VALUE = "UploadURL";
	private final String SPLIT = "#";

	private final String FROM = "From";
	private final String TO = "To";


	private final String PRE_STAGING = "PreStaging";
	private final String STAGING_IN = "StagingIn";

	private final String POST_STAGING = "PostStaging";
	private final String STAGING_OUT = "StagingOut";

	private final String PRE_STAGING_OUT = "PreStagingOut";
	private final String STAGING_IN_OUT = "StagingInOut";

	private WebResource webResource;
	private String host;
	private String path;
	private String resource;
	private static Document document;
	private String authentication;
	private Client client;

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
	 * 
	 * @return
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Sets the base URL of the service
	 * @param resource 
	 */
	public void setResource(String resource) {
		this.resource = resource;
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
	 * 
	 * @return
	 */
	public  Document getDocument() {
		return document;
	}
	/**
	 * Document used to 
	 * @param document
	 */
	public static  void setDocument(Document document) {

		OurGridJobControlAdaptor.document = document;
	}

	/**
	 *  Connects to the server and initializes the connection with the provided attributes
	 *  @param userInfo the user login
	 *  @param host the server
	 *  @param port the port 
	 *  @param basePath the base path
	 *  @param attributes the provided attributes
	 */
	public void connect(String userInfo, String host, int port, String basePath,  Map attributes)
			throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException,BadParameterException, TimeoutException, NoSuccessException {

		setClient(Client.create());
		setHost(host);
		setResource(OurGridConstants.HTTP + getHost());
		setWebResource(getClient().resource(getResource()));
		setAuthentication(new String(Base64.encode(m_account + ":" + m_passPhrase)));
	}

	/**
	 * Creates an instance of the default job monitor adaptor
	 * @return {@link OurGridJobMonitorAdaptor} Returns a job monitor adaptor instance
	 */
	public JobMonitorAdaptor getDefaultJobMonitor() {

		return new OurGridJobMonitorAdaptor();
	}

	/**
	 * Returns a job description translator
	 * @return translator xsl translator from jsdl to jdf
	 * @throws NoSuccessException
	 */
	public JobDescriptionTranslator getJobDescriptionTranslator()throws NoSuccessException {

		JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/jsdltranslatorsandbox.xsl");
		return translator;
	}

	/**
	 * Submit a job to execute it
	 * 
	 * @param uniqID a identifier unique to this job (not the job identifier
	 * which is not generated yet)
	 * @param jobDesc job description in the language supported by the targeted grid
	 * @param checkMatch if true then explicitly checks if job description matches job
	 * service before submitting job
	 * @return nativeJobId Returns the identifier of the job in the grid
	 */
	public String submit(String jobDesc, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException,NoSuccessException, BadResource {

		String job = getInput(jobDesc, JOB_TYPE);
		String jobId = null;
		setPath(SUBMIT);
		String authorization = OurGridConstants.BASIC + " " + getAuthentication();
		ClientResponse response = getWebResource().path(getPath()).header(OurGridConstants.AUTHORIZATION, authorization).
				type("text/plain").post(ClientResponse.class, job);

		if (response.getStatus() == 401) {

			throw new PermissionDeniedException(response.getClientResponseStatus().getReasonPhrase());
		} else {

			response.getClass();	
			jobId = response.getEntity(String.class);
			jobId.toString();
		}
		return jobId.trim().toString();
	}

	/**
	 * Cleans an ended job filtered by job id
	 * @param nativeJobId Identifier of the job
	 */
	public void clean(String nativeJobId) throws PermissionDeniedException,TimeoutException, NoSuccessException {

		setPath(CLEAN + nativeJobId);
		String authorization = OurGridConstants.BASIC + " " + getAuthentication();

		ClientResponse response =getWebResource().path(getPath()).
				header(OurGridConstants.AUTHORIZATION, authorization).delete(ClientResponse.class);   

	}

	/**
	 * Cancels a job filtered by job id 
	 * @param nativeJobId Identifier of the job
	 */
	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {

		setPath(CANCEL + nativeJobId);
		String authorization = OurGridConstants.BASIC + " " + getAuthentication();

		getWebResource().path(getPath()).
		header(OurGridConstants.AUTHORIZATION, authorization).delete(ClientResponse.class);
	}

	/*
	 * StagingJobAdaptorOnePhase interface
	 */

	/**
	 * Gets the URL of the directory where to copy job input/output files
	 * Protocol must be one of the supported protocols
	 * @param nativeJobDescription  the job description in native language
	 * @param uniqId  a identifier unique to this job
	 * (not the job identifier, which is not generated yet)
	 * @return
	 * @throws PermissionDeniedException
	 * @throws TimeoutException
	 * @throws NoSuccessException
	 */
	public String getStagingDirectory(String nativeJobDescription, String uniqId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		String stagingDirectory = null;
		String TransferFiles = getInput(nativeJobDescription, TAG_TYPE);

		try {


			setDocument(convertToXml(TransferFiles));
			stagingDirectory = getValue(getDocument(), TAG_ELEMENT, TAG_VALUE);

		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (SAXException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Gets the URL of the directory where to copy job input/output files
	 * Protocol must be one of the supported protocols
	 * @param nativeJobId the identifier of the job in the grid
	 * @return Returns the staging directory URL,
	 * or null if the staging directory is managed by the job service
	 * @throws PermissionDeniedException
	 * @throws TimeoutException
	 * @throws NoSuccessException
	 */
	public String getStagingDirectory(String nativeJobId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		String stagingDirectory = null;
		return stagingDirectory;
	}

	/**
	 * Gets pre-staging operations to perform before starting the job
	 * @param nativeJobId the identifier of the job in the grid
	 * @return Returns list of transfers that are not managed by the adaptor
	 * @throws PermissionDeniedException
	 * @throws TimeoutException
	 * @throws NoSuccessException
	 */
	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		return getTransfers(PRE_STAGING_OUT, STAGING_IN_OUT);
	}

	/**
	 * Gets pre-staging operations to perform before submitting the job
	 * @param nativeJobDescription  the job description in native language
	 * @param uniqId  a identifier unique to this job
	 * (not the job identifier, which is not generated yet)
	 * @return Returns list of transfers that are not managed by the adaptor
	 * @throws PermissionDeniedException
	 * @throws TimeoutException
	 * @throws NoSuccessException
	 */
	public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		return getTransfers(PRE_STAGING, STAGING_IN);
	}

	/**
	 * Gets post-staging operations to perform after the job is done
	 * @param nativeJobId  the identifier of the job in the grid
	 * @return Returns list of transfers that are not managed by the adaptor
	 * @throws PermissionDeniedException
	 * @throws TimeoutException
	 * @throws NoSuccessException
	 */
	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		return getTransfers(POST_STAGING, STAGING_OUT);
	}



	/**
	 * Gets the pre and post staging files to be transferred 
	 * @param stagingType type of staging
	 * @param stagingValue type of staging value
	 * @return
	 */
	public StagingTransfer[] getTransfers(String stagingType, String stagingValue){

		StagingTransfer[] stagingTransfers = new StagingTransfer[] {};
		ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer>();
		NodeList nList = getDocument().getElementsByTagName(stagingType);
		int qt = nList.getLength();

		for (int i = 1; i <= qt; i++) {

			nList = getDocument().getElementsByTagName(stagingValue + i);
			String from = getValue(getDocument(), stagingValue + i, FROM);
			String to = getValue(getDocument(), stagingValue + i, TO);
			transfers.add(new StagingTransfer(from, to, false));
		}

		return (StagingTransfer[]) transfers.toArray(stagingTransfers);
	}

	/**
	 * Gets the input to the submit or the staging operations
	 * @param jobDesc the job description in native language
	 * @param type  the type of input 
	 * @return Returns the input to the submit or transfer methods
	 */
	private String getInput(String jobDesc, String type) {

		String[] jobParts = jobDesc.split(SPLIT);
		String job = "";

		if (type.equals(JOB_TYPE)) {

			job = jobParts[0];
			return job;

		} else {

			for (int i = 1; i < jobParts.length; i++) {

				job += jobParts[i];
			}

			return job;
		}

	}
	
	/**
	 * Gets a XML document with the files to be transferred 
	 * @param xmlString transfer files 
	 * @return document XML document
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public  Document convertToXml(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {


		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xmlString));
		setDocument(builder.parse(is));

		return document;

	}
    /**
     * Gets the value of XML tag
     * @param document
     * @param tagElement
     * @param tagValue
     * @return Returns the value of the tag
     */
	public String getValue(Document document, String tagElement, String tagValue) {

		String value = null;
		document.getDocumentElement().normalize();
		NodeList nList = document.getElementsByTagName(tagElement);

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				value = getTagValue(tagValue, eElement);
			}

		}
		return value;

	}

	private String getTagValue(String sTag, Element eElement) {

		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

}