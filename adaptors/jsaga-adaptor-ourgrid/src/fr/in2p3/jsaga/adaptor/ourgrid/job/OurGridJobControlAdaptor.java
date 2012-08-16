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
 * This class handles the submission, cancel and clean of a job 
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
	private static Document document;
	private String authentication;
	private Client client;

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}


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

	public WebResource getWebResource() {
		return webResource;
	}

	public void setWebResource(WebResource webResource) {
		this.webResource = webResource;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	public  Document getDocument() {
		return document;
	}

	public static  void setDocument(Document document) {

		OurGridJobControlAdaptor.document = document;
	}

	public void connect(String userInfo, String host, int port, String basePath,  Map attributes)
			throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException,BadParameterException, TimeoutException, NoSuccessException {

		setClient(Client.create());
		setHost(host);
		String resource = OurGridConstants.HTTP + getHost();
		setWebResource(getClient().resource(resource));
		setAuthentication(new String(Base64.encode(m_account + ":" + m_passPhrase)));
	}

	public JobMonitorAdaptor getDefaultJobMonitor() {

		return new OurGridJobMonitorAdaptor();
	}

	public JobDescriptionTranslator getJobDescriptionTranslator()throws NoSuccessException {

		JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/jsdltranslatorsandbox.xsl");
		return translator;
	}

	/**
	 * Submit a job to execute it
	 * 
	 * @param uniqID
	 *            a identifier unique to this job (not the job identifier, which
	 *            is not generated yet)
	 * @param jobDesc
	 *            job description in the language supported by the targeted grid
	 * @param checkMatch
	 *            if true then explicitly checks if job description matches job
	 *            service before submitting job
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
			throw new PermissionDeniedException(SUBMIT_ERROR + response);
		} else if (response.getStatus() != 200) {

			throw new NoSuccessException(SUBMIT_ERROR + response.getStatus());
		} else {

			response.getClass();
			jobId = response.getEntity(String.class);
			jobId.toString();
		}
		return jobId.trim().toString();
	}

	/**
	 * Cleans an ended job filtered by job id
	 * 
	 * @param nativeJobId Identifier of the job
	 */
	public void clean(String nativeJobId) throws PermissionDeniedException,TimeoutException, NoSuccessException {

		setPath(CLEAN + nativeJobId);
		String authorization = OurGridConstants.BASIC + " " + getAuthentication();
		getWebResource().path(getPath()).
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


	public String getStagingDirectory(String nativeJobId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		String stagingDirectory = null;
		return stagingDirectory;
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		return getTransfers(PRE_STAGING_OUT, STAGING_IN_OUT);
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		return getTransfers(PRE_STAGING, STAGING_IN);
	}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)throws PermissionDeniedException, TimeoutException,NoSuccessException {

		return getTransfers(POST_STAGING, STAGING_OUT);
	}


	public StagingTransfer[] getTransfers(String tagingType, String stagingType){

		StagingTransfer[] stagingTransfers = new StagingTransfer[] {};
		ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer>();
		NodeList nList = getDocument().getElementsByTagName(tagingType);
		int qt = nList.getLength();

		for (int i = 1; i <= qt; i++) {

			nList = getDocument().getElementsByTagName(stagingType + i);
			String from = getValue(getDocument(), stagingType + i, FROM);
			String to = getValue(getDocument(), stagingType + i, TO);
			transfers.add(new StagingTransfer(from, to, false));
		}

		return (StagingTransfer[]) transfers.toArray(stagingTransfers);
	}


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

	public  Document convertToXml(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {


		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xmlString));
		setDocument(builder.parse(is));

		return document;

	}

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