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

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/**
 * @author patriciaam
 */
public class OurGridJobControlAdaptor extends OurGridAbstract implements
		JobControlAdaptor, CleanableJobAdaptor, StagingJobAdaptorOnePhase {

	private Client client = Client.create();
	private WebResource webResource;
	private String host;
	private String path;
	private Document document;

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
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

	public void connect(String userInfo, String host, int port,
			String basePath, @SuppressWarnings("rawtypes") Map attributes)
			throws NotImplementedException, AuthenticationFailedException,
			AuthorizationFailedException, IncorrectURLException,
			BadParameterException, TimeoutException, NoSuccessException {
		setHost(host);
		webResource = client.resource("http://" + getHost());
	}

	/**
	 * Cleans an ended job filtered by job id
	 * 
	 * @param nativeJobId
	 *            Identifier of the job
	 */
	public void clean(String nativeJobId) throws PermissionDeniedException,

	TimeoutException, NoSuccessException {
		try {
			ClientResponse response = webResource.path("/clean/" + nativeJobId)
					.get(ClientResponse.class);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed: HTTP error code: "
						+ response.getStatus());
			} else {
				String output = response.getEntity(String.class);
				System.out.println("Server response: " + output);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Cancels a job filtered by job id
	 * 
	 * @param nativeJobId
	 *            Identifier of the job
	 */
	public void cancel(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {

		try {
			ClientResponse response = webResource
					.path("/cancel/" + nativeJobId)
					.delete(ClientResponse.class);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed: HTTP error code: "
						+ response.getStatus());

			} else {
				String output = response.getEntity(String.class);
				System.out.println("Server response: " + output);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

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
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException, BadResource {
		String type = "job";
		
		String job = getInput(jobDesc, type);
		System.out.println(job);
		String jobId = null;
		try {

			ClientResponse response = webResource.path("/addjob")
					.type("text/plain").post(ClientResponse.class, job);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed: HTTP error code: "
						+ response.getStatus());
			} else {
				response.getClass();
				System.out.println("Job added with success....");
				jobId = response.getEntity(String.class);
				System.out.println("JobId: " + jobId);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}
		return jobId;
	}

	public JobMonitorAdaptor getDefaultJobMonitor() {

		return new OurGridJobMonitorAdaptor();
	}

	public JobDescriptionTranslator getJobDescriptionTranslator()
			throws NoSuccessException {

		JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT(
				"xsl/job/jsdltranslatorsandbox.xsl");
		return translator;
	}

	/*
	 * StagingJobAdaptorOnePhase interface
	 */

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		
		StagingTransfer[] preStagingTransfers = new StagingTransfer[]{};
		ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer> ();
		String preStaging = "PreStaging";
		NodeList nList = document.getElementsByTagName(preStaging);
		int qtPreStaging = nList.getLength();
		System.out.println(qtPreStaging);

		for (int i = 1; i <= qtPreStaging; i++) {
			nList = document.getElementsByTagName("Staging"+i);
			String from = getValue(document, "Staging"+i, "From");
			String to = getValue(document, "Staging"+i, "To");
			transfers.add(new StagingTransfer(from, to, false));
		}	

		return (StagingTransfer[])transfers.toArray(preStagingTransfers);
			}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		StagingTransfer[] postStagingTransfers = new StagingTransfer[]{};

		
		ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer> ();
		String postStaging = "PostStaging";
		NodeList nList = document.getElementsByTagName(postStaging);
		int qtPostStaging = nList.getLength();
		System.out.println(qtPostStaging);

		for (int i = 1; i <= qtPostStaging; i++) {
			nList = document.getElementsByTagName("Staging"+i);
			String from = getValue(document, "StagingOut"+i, "From");
			String to = getValue(document, "StagingOut"+i, "To");
			transfers.add(new StagingTransfer(from, to, false));
		}	

		return (StagingTransfer[])transfers.toArray(postStagingTransfers);
		}

	public String getStagingDirectory(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		String stagingDirectory = null;

		return stagingDirectory;
	}

	public StagingTransfer[] getInputStagingTransfer(
			String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {

		StagingTransfer[] preStagingTransfers = new StagingTransfer[]{};
		ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer> ();
		String preStaging = "PreStaging";
		NodeList nList = document.getElementsByTagName(preStaging);
		int qtPreStaging = nList.getLength();
		System.out.println(qtPreStaging);

		for (int i = 1; i <= qtPreStaging; i++) {
			nList = document.getElementsByTagName("Staging"+i);
			String from = getValue(document, "Staging"+i, "From");
			String to = getValue(document, "Staging"+i, "To");
			transfers.add(new StagingTransfer(from, to, false));
		}	

		return (StagingTransfer[])transfers.toArray(preStagingTransfers);
	}


	public String getStagingDirectory(String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		@SuppressWarnings("unused")
		String stagingDirectory = null;
		String type = "transferFiles";
		String tagElement = "TransferFiles";
		String tagValue = "UploadURL";
		System.out.println(nativeJobDescription);
		String TransferFiles = getInput(nativeJobDescription, type);
		try {
			Document doc = convertToXml(TransferFiles);
			setDocument(doc);

			stagingDirectory = getValue(document, tagElement, tagValue);

		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (SAXException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;

	}

	private static String getInput(String jobDesc, String type) {
		String[] jobParts = jobDesc.split("#");
		String job = "";

		if (type.equals("job")) {
			job = jobParts[0];

			return job;

		} else {
			for (int i = 1; i < jobParts.length; i++) {
				job += jobParts[i];
			}

			return job;
		}

	}

	public static Document convertToXml(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {
		Document document;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xmlString));
		document = builder.parse(is);

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

	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

	

}
