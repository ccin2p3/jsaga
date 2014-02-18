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
import org.glite.ce.creamapi.ws.cream2.CREAMStub.CommandResult;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfoRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobPurgeRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobResumeRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobResumeResponse;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobStartRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobSuspendRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobSuspendResponse;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.ResultChoice_type0;
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
import org.glite.ce.creamapi.ws.cream2.CREAMStub.Status;
import org.glite.ce.creamapi.ws.cream2.OperationNotSupported_Fault;
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

//    private String m_delegProxy;
    private Boolean m_hasOutputSandboxBug = null;
    
//    private DelegationServiceStub m_delegationServiceStub;
    private Logger m_logger = Logger.getLogger(CreamJobControlAdaptor.class);
    
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
        m_client.renewDelegation(m_delegationId, m_vo);

    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/cream-jdl.xsl");
        translator.setAttribute(BATCH_SYSTEM, m_batchSystem);
        translator.setAttribute(QUEUE_NAME, m_queueName);
        return translator;
    }
    
    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	
        m_logger.debug("Submitting job described as:\n" + jobDesc);
        JobRegisterResponse response;
		try {
			response = m_client.jobRegister(jobDesc);
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
        m_logger.debug("Job submitted");
        // return jobid
        if (resultArray.length == 1) {
            JobRegisterResult res = resultArray[0];
            if (res.isGenericFaultSpecified()) {
                throw new NoSuccessException(res.getGenericFault().getFaultCause());
            }
            if (res.isDelegationIdMismatchFaultSpecified()) {
                throw new PermissionDeniedException(res.getDelegationIdMismatchFault().getFaultCause());
            }
            if (res.isDelegationProxyFaultSpecified()) {
                throw new PermissionDeniedException(res.getDelegationProxyFault().getFaultCause());
            }
            if (res.isLeaseIdMismatchFaultSpecified()) {
                throw new NoSuccessException(res.getLeaseIdMismatchFault().getFaultCause());
            }
            JobId jobid = res.getJobId();
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
    
    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	StagingTransfer[] st;
        JobInfo jobInfo = this.getJobInfo(nativeJobId);
        // First check if the status is ABORTED because of Unauthorized Request BLAH Error, this would be useless to stage files
        for (Status stat: jobInfo.getStatus()) {
            if (stat.getName().equals(CreamJobStatus.ABORTED)) {
                if (stat.getFailureReason() != null && 
                        (stat.getFailureReason().contains("Unauthorized Request") ||
                         stat.getFailureReason().contains("Invalid credential")
                        )
                   ) 
                {
                    throw new PermissionDeniedException(stat.getFailureReason());
                } else {
                    throw new NoSuccessException(stat.getFailureReason());
                }
            } else if (stat.getName().equals(CreamJobStatus.CANCELLED)) {
                throw new NoSuccessException(stat.getDescription());
            }
        }
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
    		Logger.getLogger(CreamJobAdaptorAbstract.class).warn("Could not check if CREAM CE has the OSB bug");
		}
		return st;
    }
    
    private JobInfo getJobInfo(String nativeJobId) throws TimeoutException, NoSuccessException {
        JobInfoResult[] resultArray;
        try {
			resultArray = m_client.jobInfo(nativeJobId);
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
        // cancel job
        Result[] resultArray;
        try {
			resultArray = m_client.jobStart(nativeJobId);
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
        // cancel job
        Result[] resultArray;
        try {
            resultArray = m_client.jobCancel(nativeJobId);
		} catch (Authorization_Fault e) {
			throw new PermissionDeniedException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}

    }

    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // cancel job
        Result[] resultArray;
        try {
            resultArray = m_client.jobClean(nativeJobId);
		} catch (Authorization_Fault e) {
			throw new PermissionDeniedException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		}
    }

	public boolean hold(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // cancel job
        Result[] resultArray;
        try {
            resultArray = m_client.jobSuspend(nativeJobId);
		} catch (Authorization_Fault e) {
			throw new PermissionDeniedException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		} catch (OperationNotSupported_Fault e) {
			return false;
		}
        return getBooleanResult(resultArray[0]);
	}

	public boolean release(String nativeJobId) throws PermissionDeniedException, TimeoutException,	NoSuccessException {
        // cancel job
        Result[] resultArray;
        try {
            resultArray = m_client.jobResume(nativeJobId);
		} catch (Authorization_Fault e) {
			throw new PermissionDeniedException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		} catch (OperationNotSupported_Fault e) {
			return false;
		}
        return getBooleanResult(resultArray[0]);
	}

	private boolean getBooleanResult(Result res) throws NoSuccessException, PermissionDeniedException {
        ResultChoice_type0 err = res.getResultChoice_type0();
        if (err != null) {
        	if (err.isJobStatusInvalidFaultSpecified()) {
        		return false;
        	} else if (err.isJobUnknownFaultSpecified()) {
        		throw new NoSuccessException(err.getJobUnknownFault().getDescription());
        	} else if (err.isDateMismatchFaultSpecified()) {
        		throw new NoSuccessException(err.getDateMismatchFault().getDescription());
        	} else if (err.isDelegationIdMismatchFaultSpecified()) {
        		throw new PermissionDeniedException(err.getDelegationIdMismatchFault().getDescription());
        	} else if (err.isGenericFaultSpecified()) {
        		throw new NoSuccessException(err.getGenericFault().getDescription());
        	} else if (err.isLeaseIdMismatchFaultSpecified()) {
        		throw new NoSuccessException(err.getLeaseIdMismatchFault().getDescription());
        	} else {
        		throw new NoSuccessException("Unable to get Fault");
        	}
        }
        return true;
	}
}
