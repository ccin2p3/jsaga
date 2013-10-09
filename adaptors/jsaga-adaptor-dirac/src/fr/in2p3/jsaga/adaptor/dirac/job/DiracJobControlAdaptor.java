package fr.in2p3.jsaga.adaptor.dirac.job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
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

public class DiracJobControlAdaptor extends DiracJobAdaptorAbstract implements JobControlAdaptor, 
																StagingJobAdaptorOnePhase, CleanableJobAdaptor {

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
			DiracRESTClient submittor = new DiracRESTClient();
            JSONParser parser = new JSONParser();
            JSONObject diracJobDesc = (JSONObject) parser.parse(jobDesc);
			// parse JSON jobDesc to get input files and write contents to POST data
            if (diracJobDesc.containsKey("JSAGADataStagingIn")) {
            	JSONArray inputTransfers = (JSONArray)diracJobDesc.get("JSAGADataStagingIn");
	            for (int i=0; i<inputTransfers.size(); i++) {
	            	JSONObject transfer = (JSONObject)inputTransfers.get(i);
	            	submittor.addFile(transfer.get("Dest").toString(), transfer.get("Source").toString());
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
            	diracJobDesc.put(DiracConstants.DIRAC_MANIFEST_SITE, sites);
            }
            
            // add jobname
            diracJobDesc.put(DiracConstants.DIRAC_MANIFEST_JOBNAME, "JSAGA-" + uniqId);
            
            
            // parse JSON jobDesc to get output files and dump to local file (Dirac does not accept additionnal JSON fields)
            if (diracJobDesc.containsKey("JSAGADataStagingOut")) {
            	JSONArray outputTransfers = (JSONArray)diracJobDesc.get("JSAGADataStagingOut");
            	try {
            		// Create file 
            		String jobName = (String)diracJobDesc.get(DiracConstants.DIRAC_MANIFEST_JOBNAME);
            		BufferedWriter out = new BufferedWriter(
            								new FileWriter(
            									new File(System.getProperty("java.io.tmpdir"),jobName)));
            		out.write(outputTransfers.toJSONString());
            		//Close the output stream
            		out.close();
            	} catch (Exception e) {
            		throw new NoSuccessException(e);
            	}
            }
            diracJobDesc.remove("JSAGADataStagingOut");
            
            
            // Add StdOutput and StdError to OutputSandbox
            if (diracJobDesc.containsKey(DiracConstants.DIRAC_MANIFEST_STDOUTPUT) || diracJobDesc.containsKey(DiracConstants.DIRAC_MANIFEST_STDERROR)) {
	            JSONArray outputFiles = (JSONArray)diracJobDesc.get(DiracConstants.DIRAC_MANIFEST_OUTPUTSANDBOX);
	            if (outputFiles == null) {
	            	outputFiles = new JSONArray();
	            }
	            if (diracJobDesc.containsKey(DiracConstants.DIRAC_MANIFEST_STDOUTPUT)) {
	            	if (!outputFiles.contains(diracJobDesc.get(DiracConstants.DIRAC_MANIFEST_STDOUTPUT)))
	            		outputFiles.add(diracJobDesc.get(DiracConstants.DIRAC_MANIFEST_STDOUTPUT));
	            }
	            if (diracJobDesc.containsKey(DiracConstants.DIRAC_MANIFEST_STDERROR)) {
	            	if (!outputFiles.contains(diracJobDesc.get(DiracConstants.DIRAC_MANIFEST_STDERROR)))
	            	outputFiles.add(diracJobDesc.get(DiracConstants.DIRAC_MANIFEST_STDERROR));
	            }
	            diracJobDesc.put(DiracConstants.DIRAC_MANIFEST_OUTPUTSANDBOX, outputFiles);
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
			result = new DiracRESTClient()
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
			// Get the jobname
			String jobName = this.getJobName(nativeJobId);
			
			// Read the corresponding local file
			BufferedReader in;
			try {
				in = new BufferedReader(
									new FileReader(
										new File(System.getProperty("java.io.tmpdir"),jobName)));
			} catch (Exception e) {
				// Nothing to do there was no Output files
				return new StagingTransfer[]{};
			}
			
			// Parse file
	    	ArrayList<StagingTransfer> transfers = new ArrayList<StagingTransfer>();
            JSONParser parser = new JSONParser();
            JSONArray files = (JSONArray) parser.parse(in.readLine());

			for (Object f: (JSONArray)files) {
				String source = ((JSONObject)f).get("Source").toString();
				String dest = ((JSONObject)f).get("Dest").toString();
				transfers.add(new StagingTransfer(
						this.buildOSBUrl(nativeJobId, source).toString(),
						dest,
						false));
			}
			in.close();
	    	StagingTransfer[] st = new StagingTransfer[]{};
	    	return (StagingTransfer[]) transfers.toArray(st);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	private String buildOSBUrl(String nativeJobId, String filename) throws MalformedURLException {
		return new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId + "/outputsandbox/" + filename)
						.toString()
						.replaceAll("https://", "dirac-osb://") 
						+ "?" + DiracConstants.DIRAC_GET_PARAM_ACCESS_TOKEN + "=" + this.m_accessToken;
	}

	public void clean(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		try {

			String jobName = this.getJobName(nativeJobId);
			
			// Read the corresponding local file
			try {
				new File(System.getProperty("java.io.tmpdir"),jobName).delete();
			} catch (Exception e) {
				// if file does not exist, means that there was no output files
			}
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
}
