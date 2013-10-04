package fr.in2p3.jsaga.adaptor.dirac.job;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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
			m_logger.debug("Input job desc:\n" + jobDesc);
			DiracRESTClient submittor = new DiracRESTClient(m_credential, m_accessToken);
			// parse JSON jobDesc to get input files and write contents to POST data
            JSONParser parser = new JSONParser();
            JSONObject diracJobDesc = (JSONObject) parser.parse(jobDesc);
            if (diracJobDesc.containsKey("JSAGADataStagingIn")) {
            	JSONArray inputTransfers = (JSONArray)diracJobDesc.get("JSAGADataStagingIn");
	            for (int i=0; i<inputTransfers.size(); i++) {
	            	JSONObject transfer = (JSONObject)inputTransfers.get(i);
	                JSONArray file = new JSONArray();
	                file.add(transfer.get("Dest").toString());
	                file.add(open(transfer.get("Source").toString()));
	                submittor.addData(file.toJSONString());
	            }
            }
            // remove JSAGA internal info from the jobDesc
            diracJobDesc.remove("JSAGADataStagingIn");
            // Add sites if requested
            if (m_sites != null) {
            	JSONArray sites = new JSONArray();
            	for (String s: m_sites) {
            		sites.add(s);
            	}
            	diracJobDesc.put("Site", sites);
            }
            // Add StdOutput and StdError to OutputSandbox
            if (diracJobDesc.containsKey("StdOutput") || diracJobDesc.containsKey("StdError")) {
	            JSONArray outputFiles = (JSONArray)diracJobDesc.get("OutputSandbox");
	            if (outputFiles == null) {
	            	outputFiles = new JSONArray();
//	            } else {
//	            	diracJobDesc.remove("OutputSandbox");
	            }
	            if (diracJobDesc.containsKey("StdOutput"))
	            	outputFiles.add(diracJobDesc.get("StdOutput"));
	            if (diracJobDesc.containsKey("StdError"))
	            	outputFiles.add(diracJobDesc.get("StdError"));
	            diracJobDesc.put("OutputSandbox", outputFiles);
            }
            jobDesc = diracJobDesc.toJSONString();
			m_logger.debug("Output job desc:\n" + jobDesc);
			submittor.addParam("manifest", jobDesc);
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
			result = new DiracRESTClient(m_credential, m_accessToken)
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
		return new StagingTransfer[]{};
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
		return new StagingTransfer[]{};
	}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {
			JSONObject diracJobDesc = new DiracRESTClient(m_credential, m_accessToken)
									.get(new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId + "/manifest"));
			if (!diracJobDesc.containsKey("OutputSandbox")) {
				return null;
			}
			Object files = diracJobDesc.get("OutputSandbox");
	    	ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer>();
			if (files instanceof JSONArray) {
				for (Object f: (JSONArray)files) {
					transfers.add(new StagingTransfer(
							new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId + "/outputsandbox/" + f).toString(),
							f.toString(),
							false));
				}
			} else {
				String f = (String)files;
				transfers.add(new StagingTransfer(
						new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId + "/outputsandbox/" + f).toString(),
						f.toString(),
						false
						));
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
	 * @deprecated
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
	
	/**
	 * open a file named as path or URI file:/
	 * @param file
	 * @return
	 * @throws IOException
	 */
    private static String open(String filename) throws IOException {
    	File file = null;
    	try {
    		URL url = new URL(filename);
    		file = new File(url.getPath());
    	} catch (MalformedURLException e) {
    		file  = new File(filename);
    	}
        byte[] buffer = new byte[(int) file.length()];
        DataInputStream stream = new DataInputStream(new FileInputStream(file));
        stream.readFully(buffer);
        stream.close();
        return new String(buffer);
    }

}
