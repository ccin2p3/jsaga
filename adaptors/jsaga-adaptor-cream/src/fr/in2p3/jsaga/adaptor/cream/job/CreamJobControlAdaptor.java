package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.HoldableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.apache.log4j.Logger;
import org.glite.ce.creamapi.ws.cream2.CREAMPort;
import org.glite.ce.creamapi.ws.cream2.types.*;
import org.ogf.saga.error.*;

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
    // parameters configured
    private static final String SSL_CA_FILES = "sslCAFiles";

    // parameters extracted from URI
    private static final String BATCH_SYSTEM = "BatchSystem";
    private static final String QUEUE_NAME = "QueueName";
    private String m_batchSystem;
    private String m_queueName;

    private String m_delegProxy;

    public JobMonitorAdaptor getDefaultJobMonitor() {
        // use CREAM portType as default monitoring service (instead of CEMon portType)
        return new CreamJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // set delegationId and create stub for CREAM service
        super.connect(userInfo, host, port, basePath, attributes);

        // set SSL_CA_FILES
        System.setProperty(SSL_CA_FILES, m_certRepository.getPath() + "/*.0");
        System.setProperty("crlEnabled", "false");
        
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
        try {
			ServiceInfo service_info = m_creamStub.getStub().getServiceInfo(0);
			String cream_desc = host + " (interface version=" + 
								service_info.getInterfaceVersion() + ",service version=" + 
								service_info.getServiceVersion() + ")";
    		Logger.getLogger(CreamJobAdaptorAbstract.class).info("Connecting to "+cream_desc);
    		m_creamVersion = service_info.getServiceVersion();
		} catch (Exception e) {
    		Logger.getLogger(CreamJobAdaptorAbstract.class).info("Could not get service version");
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
/*
        // put new delegated proxy for current job
        if (m_delegProxy != null) {
            jd.setDelegationProxy(m_delegProxy);
        }
*/

        // submit job
        CREAMPort stub = m_creamStub.getStub();
        JobRegisterResult[] resultArray;
        try {
            resultArray = stub.jobRegister(new JobDescription[]{jd});
        } catch (RemoteException e) {
            throw new NoSuccessException(e);
        }

        // rethrow exception if any fault in result
        CreamExceptionFactory.rethrow(resultArray);

        // return jobid
        if (resultArray.length == 1) {
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
        JobInfo jobInfo = this.getJobInfo(nativeJobId);
        String jdl = jobInfo.getJDL();
        StagingJDL parsedJdl = new StagingJDL(jdl);
        // Do not add '/' in case of Cream 1.14 EMI version 2.4.0-1
        return parsedJdl.getOutputStagingTransfers(jobInfo.getCREAMOutputSandboxURI()+
        		((m_creamVersion.contains("2.4.0-1"))?"":"/"));
    }
    
    private JobInfo getJobInfo(String nativeJobId) throws TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // get job info
        CREAMPort stub = m_creamStub.getStub();
        JobInfoResult resultArray[];
        try {
            resultArray = stub.jobInfo(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }

        // rethrow exception if any fault in result
        CreamExceptionFactory.rethrow(resultArray);

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

        // cancel job
        CREAMPort stub = m_creamStub.getStub();
        Result[] resultArray;
        try {
            resultArray = stub.jobStart(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }

        // rethrow exception if any fault in result
        CreamExceptionFactory.rethrow(resultArray);
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // cancel job
        CREAMPort stub = m_creamStub.getStub();
        Result[] resultArray;
        try {
            resultArray = stub.jobCancel(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }

        // rethrow exception if any fault in result
        CreamExceptionFactory.rethrow(resultArray);
    }

    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // purge job
        CREAMPort stub = m_creamStub.getStub();
        Result[] resultArray;
        try {
            resultArray = stub.jobPurge(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }

        // rethrow exception if any fault in result
        CreamExceptionFactory.rethrow(resultArray);
    }

	public boolean hold(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // purge job
        CREAMPort stub = m_creamStub.getStub();
        Result[] resultArray;
        try {
            resultArray = stub.jobSuspend(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }
        if (resultArray[0].getJobStatusInvalidFault() != null) return false;
        // Not sure why we get this exception sometimes:
        if (resultArray[0].getJobUnknownFault() != null) return false;
        // rethrow exception if any fault in result
        CreamExceptionFactory.rethrow(resultArray);
        return true;
	}

	public boolean release(String nativeJobId) throws PermissionDeniedException, TimeoutException,	NoSuccessException {
        JobFilter filter = this.getJobFilter(nativeJobId);

        // purge job
        CREAMPort stub = m_creamStub.getStub();
        Result[] resultArray;
        try {
            resultArray = stub.jobResume(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }

        if (resultArray[0].getJobStatusInvalidFault() != null) return false;
        // Not sure why we get this exception sometimes:
        if (resultArray[0].getJobUnknownFault() != null) return false;
        // rethrow exception if any fault in result
        CreamExceptionFactory.rethrow(resultArray);
        return true;
	}

	private JobFilter getJobFilter(String nativeJobId) throws NoSuccessException {
        JobId jobId = new JobId();
        jobId.setCreamURL(m_creamStub.getURI());
        jobId.setId(nativeJobId);
        JobFilter filter = new JobFilter();
        filter.setDelegationId(m_delegationId);
        filter.setJobId(new JobId[]{jobId});
        return filter;
    }

}
