package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.data.GsiftpClient;
import fr.in2p3.jsaga.adaptor.data.GsiftpDataAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.data.GsiftpInputStream;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.HoldableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.apache.axis2.databinding.types.URI;
import org.apache.log4j.Logger;
import org.glite.ce.creamapi.ws.cream2.Authorization_Fault;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfoRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobStartRequest;
import org.glite.ce.creamapi.ws.cream2.Generic_Fault;
import org.glite.ce.creamapi.ws.cream2.InvalidArgument_Fault;
import org.glite.ce.creamapi.ws.cream2.JobSubmissionDisabled_Fault;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobCancelRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobDescription;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobFilter;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobId;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfo;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfoResult;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobRegisterRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobRegisterResponse;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobRegisterResult;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.Result;
import org.ogf.saga.error.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamJobControlAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamJobControlAdaptor extends CreamJobAdaptorAbstract implements StagingJobAdaptorTwoPhase, CleanableJobAdaptor,
	HoldableJobAdaptor {

    // parameters extracted from URI
    private static final String BATCH_SYSTEM = "BatchSystem";
    private static final String QUEUE_NAME = "QueueName";
    private String m_batchSystem;
    private String m_queueName;

    private String m_delegProxy;
    private Boolean m_hasOutputSandboxBug = null;
    
//    private DelegationServiceStub m_delegationServiceStub;
    
    public JobMonitorAdaptor getDefaultJobMonitor() {
        // use CREAM portType as default monitoring service (instead of CEMon portType)
        return new CreamJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // set delegationId and create stub for CREAM service
        super.connect(userInfo, host, port, basePath, attributes);

        // extract parameters from basePath
        Matcher m = Pattern.compile("/cream-(.*)-(.*)").matcher(basePath);
        if (m.matches()) {
            m_batchSystem = m.group(1);
            m_queueName = m.group(2);
        } else {
            throw new BadParameterException("Path must be on the form: /cream-<lrms>-<queue>");
        }

        // renew/create delegated proxy
        DelegationStub delegationStub = new DelegationStub(host, port, m_vo);
        m_delegProxy = delegationStub.renewDelegation(m_delegationId, m_credential);
        // put new delegated proxy for multiple jobs
        if (m_delegProxy != null) {
            delegationStub.putProxy(m_delegationId, m_delegProxy);
        }

    }

    public void disconnect() throws NoSuccessException {
        m_delegProxy = null;
        super.disconnect();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/cream-jdl.xsl");
        translator.setAttribute(BATCH_SYSTEM, m_batchSystem);
        translator.setAttribute(QUEUE_NAME, m_queueName);
        return translator;
    }
    
    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	
        // create job description
        JobDescription jd = new JobDescription();
        jd.setJDL(jobDesc);
        jd.setAutoStart(false);
        jd.setDelegationId(m_delegationId);
//        if (m_delegProxy != null) {
//            jd.setDelegationProxy(m_delegProxy);
//        }
        
        // submit job
    	JobRegisterRequest request = new JobRegisterRequest();
    	request.setJobDescriptionList(new JobDescription[]{jd});
        JobRegisterResponse response;
		try {
			response = m_creamStub.jobRegister(request, null);
		} catch (Authorization_Fault e) {
			throw new PermissionDeniedException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new BadResource(e);
		} catch (JobSubmissionDisabled_Fault e) {
			throw new PermissionDeniedException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
        // rethrow exception if any fault in result
        JobRegisterResult[] resultArray = response.getResult();
        
        // return jobid
        if (resultArray.length == 1) {
//        	resultArray[0].getDelegationProxyFault()
            JobId jobid = resultArray[0].getJobId();
            if (jobid == null) {
                throw new NoSuccessException("Null job identifier");
            }
            return jobid.getId();
        } else {
            throw new NoSuccessException("Unexpected content of response message ["+resultArray.length+"]");
        }
    }
    
    public String getStagingDirectory(String nativeJobId) throws TimeoutException, NoSuccessException {
        return null;    // use the CREAM default staging directory
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws TimeoutException, NoSuccessException {
        JobInfo jobInfo = this.getJobInfo(nativeJobId);
        String jdl = jobInfo.getJDL();
        StagingJDL parsedJdl = new StagingJDL(jdl);
        return parsedJdl.getInputStagingTransfer(jobInfo.getCREAMInputSandboxURI()+"/");
    }
    
    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws TimeoutException, NoSuccessException {
    	StagingTransfer[] st;
        JobInfo jobInfo = this.getJobInfo(nativeJobId);
        String jdl = jobInfo.getJDL();
        StagingJDL parsedJdl = new StagingJDL(jdl);
        
        // If bug has already been checked build the appropriate list
        if (m_hasOutputSandboxBug != null) {
	        if (m_hasOutputSandboxBug) {
	        	return parsedJdl.getOutputStagingTransfers(jobInfo.getCREAMOutputSandboxURI());
	        } else if (!m_hasOutputSandboxBug) {
	        	return parsedJdl.getOutputStagingTransfers(jobInfo.getCREAMOutputSandboxURI()+"/");
	        }
        }
        
        // First build list of transfers with OSB/...
        st = parsedJdl.getOutputStagingTransfers(jobInfo.getCREAMOutputSandboxURI()+"/");
        
        // If list is empty return
        if (st.length == 0) return st;
        
        // Otherwise, check if CREAM CE has the bug on OSB
		try {
	        // get the job working directory
	        String outputSandboxURI = jobInfo.getCREAMOutputSandboxURI();
	        // build the job wrapper URI
	        URI jobWrapper_uri = new URI(outputSandboxURI.substring(0, outputSandboxURI.length()-4) + "/" + jobInfo.getJobId().getId() + "_jobWrapper.sh");
	        // connect to GridFTP
	        GsiftpClient client = GsiftpDataAdaptorAbstract.createConnection(m_credential, jobWrapper_uri.getHost(), 2811, 1024*16, false);
	        // Read the job wrapper
	        BufferedReader in = new BufferedReader(new InputStreamReader(new GsiftpInputStream(client, jobWrapper_uri.getPath())));
	        String line = null;

	        while((line = in.readLine()) != null) {
	        	// Search for line starting with "__output_file_dest[0]"
	            if (line.startsWith("__output_file_dest[0]=")) {
	            	int lastSlashIndex = line.lastIndexOf("/");
	            	// if there is OSB before last /, return st
	            	if (line.substring(lastSlashIndex-3, lastSlashIndex).equals("OSB")) {
	        	        client.close();
	                	m_hasOutputSandboxBug = false;
	            		return st;
	            	} else {
	        	        client.close();
	                	m_hasOutputSandboxBug = true;
	            		return parsedJdl.getOutputStagingTransfers(jobInfo.getCREAMOutputSandboxURI());
	            	}
	            }
	        }
	        client.close();
		} catch (Exception e) {
			e.printStackTrace();
    		Logger.getLogger(CreamJobAdaptorAbstract.class).info("Could not check if CREAM CE has the OSB bug");
		}
		return st;
    }
    
    private JobInfo getJobInfo(String nativeJobId) throws TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // get job info
        JobInfoResult resultArray[];

        JobInfoRequest request = new JobInfoRequest();
        request.setJobInfoRequest(filter);
        try {
			resultArray = m_creamStub.jobInfo(request).getResult();
		} catch (Authorization_Fault e) {
			throw new NoSuccessException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new TimeoutException(e);
		}

        // return job info
        if (resultArray.length == 1) {
            JobInfo jobInfo = resultArray[0].getJobInfo();
            if (jobInfo == null) {
                throw new NoSuccessException("Null job information");
            }
            return jobInfo;
        } else {
            throw new NoSuccessException("Unexpected content of response message ["+resultArray.length+"]");
        }
    }

    public void start(String nativeJobId) throws TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        JobStartRequest request = new JobStartRequest();
        request.setJobStartRequest(filter);
        
        // cancel job
        Result[] resultArray;
        try {
			resultArray = m_creamStub.jobStart(request).getJobStartResponse().getResult();
		} catch (Authorization_Fault e) {
			throw new NoSuccessException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}

    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        JobCancelRequest request = new JobCancelRequest();
        request.setJobCancelRequest(filter);
        
        // cancel job
        Result[] resultArray;
        try {
            resultArray = m_creamStub.jobCancel(request).getJobCancelResponse().getResult();
		} catch (Authorization_Fault e) {
			throw new NoSuccessException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}

    }

    // TODO: clean
    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // purge job
//        Result[] resultArray;
//        try {
//            resultArray = m_creamStub.jobPurge(filter).getResult();
//		} catch (AuthorizationFault e) {
//			throw new NoSuccessException(e);
//		} catch (GenericFault e) {
//			throw new NoSuccessException(e);
//		} catch (InvalidArgumentFault e) {
//			throw new NoSuccessException(e);
//		} catch (RemoteException e) {
//			throw new NoSuccessException(e);
//		}

        // rethrow exception if any fault in result
//        CreamExceptionFactory.rethrow(resultArray);
    }

    // TODO: hold
	public boolean hold(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // hold job
//        Result[] resultArray;
//        try {
//            resultArray = m_creamStub.jobSuspend(filter).getResult();
//		} catch (AuthorizationFault e) {
//			throw new NoSuccessException(e);
//		} catch (GenericFault e) {
//			throw new NoSuccessException(e);
//		} catch (InvalidArgumentFault e) {
//			throw new NoSuccessException(e);
//		} catch (RemoteException e) {
//			throw new NoSuccessException(e);
//		}
//        if (resultArray[0].getJobStatusInvalidFault() != null) return false;
//        // Not sure why we get this exception sometimes:
//        if (resultArray[0].getJobUnknownFault() != null) return false;
        return true;
	}

	// TODO: release
	public boolean release(String nativeJobId) throws PermissionDeniedException, TimeoutException,	NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // release job
//        Result[] resultArray;
//        try {
//            resultArray = m_creamStub.jobResume(filter).getResult();
//		} catch (AuthorizationFault e) {
//			throw new NoSuccessException(e);
//		} catch (GenericFault e) {
//			throw new NoSuccessException(e);
//		} catch (InvalidArgumentFault e) {
//			throw new NoSuccessException(e);
//		} catch (RemoteException e) {
//			throw new NoSuccessException(e);
//		}
//
//        if (resultArray[0].getJobStatusInvalidFault() != null) return false;
//        // Not sure why we get this exception sometimes:
//        if (resultArray[0].getJobUnknownFault() != null) return false;
        return true;
	}

}
