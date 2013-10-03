package fr.in2p3.jsaga.adaptor.dirac.job;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracRESTClient;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DiracJobControlAdaptor
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   30 sept 2013
 * ***************************************************/

public class DiracJobControlAdaptor extends DiracJobAdaptorAbstract implements JobControlAdaptor, StagingJobAdaptorOnePhase {

	public JobDescriptionTranslator getJobDescriptionTranslator()
			throws NoSuccessException {
        JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/dirac.xsl");
        return translator;
	}

	public JobMonitorAdaptor getDefaultJobMonitor() {
		return new DiracJobMonitorAdaptor();
	}

	public String submit(String jobDesc, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException, BadResource {
		try {
			jobDesc="{ \"Executable\": \"/bin/date\"}";
			m_logger.debug(jobDesc);
			DiracRESTClient submittor = new DiracRESTClient(m_credential, m_accessToken);
//			submittor.addData(jobDesc);
			submittor.addParam("manifest", jobDesc);
			// parse JSON jobDesc to get input files and write contents to POST data
//            JSONParser parser = new JSONParser();
//            JSONObject diracJobDesc = (JSONObject) parser.parse(jobDesc);
//            if (diracJobDesc.containsKey("InputSandbox")) {
//            	JSONArray inputFiles = (JSONArray)diracJobDesc.get("InputSandbox");
//	            for (int i=0; i<inputFiles.size(); i++) {
//	                JSONArray file = new JSONArray();
//	                file.add(inputFiles.get(i).toString());
//	                file.add(open(new File(inputFiles.get(i).toString())));
//	                submittor.addData(file.toJSONString());
//	            }
//            }
			JSONObject submitResult = submittor.post(new URL(m_url, DiracConstants.DIRAC_PATH_JOBS));
	        // resulting job ID(s) are returned as a list ( e.g. when bulk submission )
			m_logger.debug(submitResult.toJSONString());
			JSONArray jobIdArray = (JSONArray)submitResult.get(DiracConstants.DIRAC_GET_RETURN_JIDS);
			return ((Long)jobIdArray.get(0)).toString();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		JSONObject result;
		try {
			result = (JSONObject)new DiracRESTClient(m_credential, m_accessToken)
					.delete(new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId));
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		if (!nativeJobId.equals(((Long)result.get(DiracConstants.DIRAC_GET_RETURN_JID)).toString())) {
			throw new NoSuccessException("DELETE returned:" + result.toJSONString());
		}
	}

	/*------------- StagingOnePhase ---------------*/
	public String getStagingDirectory(String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		// managed by Dirac : /JOBS/<jobId>
		return null;
	}

	public StagingTransfer[] getInputStagingTransfer(
			String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		// no staging IN to do (sent at submission as multipart HTTP post request)
		return null;
	}

	/*------------- StagingTwoPhase ---------------*/
	public String getStagingDirectory(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		// managed by Dirac : /JOBS/<jobId>
		return null;
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		// no staging IN to do (sent at submission as multipart HTTP post request)
		return null;
	}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {
			JSONObject diracJobDesc = new DiracRESTClient(m_credential, m_accessToken)
									.delete(new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId + "/manifest"));
//			return this.getStagingTransfers(nativeJobId, jobDesc, "OutputSandox");
			if (!diracJobDesc.containsKey("OutputSandbox")) {
				return null;
			}
			JSONArray files = (JSONArray)diracJobDesc.get("OutputSandbox");
	    	ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer>();
			for (Object f: files) {
				transfers.add(new StagingTransfer(
						new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId + "/outputsandbox/" + f).toString(),
						f.toString(),
						false));
			}
	    	StagingTransfer[] st = new StagingTransfer[]{};
	    	return (StagingTransfer[]) transfers.toArray(st);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	/**
	 * get the list of staging transfers for the jobs
	 * @param nativeJobId
	 * @param diracJobDesc: the JSON formatted job description
	 * @param sandbox: "InputSandbox" or "OutputSandbox"
	 * @return an Array of staging transfers for the jobs
	 * @throws MalformedURLException
	 */
	private StagingTransfer[] getStagingTransfers(String nativeJobId, JSONObject diracJobDesc, String sandbox) throws MalformedURLException {
		if (!diracJobDesc.containsKey(sandbox)) {
			return null;
		}
		JSONArray files = (JSONArray)diracJobDesc.get(sandbox);
    	ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer>();
		for (Object f: files) {
			transfers.add(new StagingTransfer(
					new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId + "/outputsandbox/" + f).toString(),
					f.toString(),
					false));
		}
    	StagingTransfer[] st = new StagingTransfer[]{};
    	return (StagingTransfer[]) transfers.toArray(st);
	}
	
    private static String open(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        DataInputStream stream = new DataInputStream(new FileInputStream(file));
        stream.readFully(buffer);
        stream.close();
        return new String(buffer);
    }

}
