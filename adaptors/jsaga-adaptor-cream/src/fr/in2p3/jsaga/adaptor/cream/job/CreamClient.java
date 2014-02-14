package fr.in2p3.jsaga.adaptor.cream.job;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.glite.ce.creamapi.ws.cream2.Authorization_Fault;
import org.glite.ce.creamapi.ws.cream2.CREAMStub;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobCancelRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobDescription;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobFilter;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobId;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfoRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfoResult;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobPurgeRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobRegisterRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobRegisterResponse;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobResumeRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobStartRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobSuspendRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.Result;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.ServiceInfo;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.ServiceInfoRequest;
import org.glite.ce.creamapi.ws.cream2.Generic_Fault;
import org.glite.ce.creamapi.ws.cream2.InvalidArgument_Fault;
import org.glite.ce.creamapi.ws.cream2.JobSubmissionDisabled_Fault;
import org.glite.ce.creamapi.ws.cream2.OperationNotSupported_Fault;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

import fr.in2p3.jsaga.adaptor.cream.CreamSocketFactory;

public class CreamClient {

    protected CREAMStub m_creamStub;
    // TODO: check if URL can be retrieved from Stub
    protected URL m_creamUrl;
    private ProtocolSocketFactory m_socketFactory;
    private int m_port;
    private String m_host;
    private GSSCredential m_credential;
    private String m_delegationId;
    private Logger m_logger;

    public CreamClient(String host, int port, GSSCredential cred, File certs, String delegId) throws MalformedURLException, AxisFault, AuthenticationFailedException {
        m_creamUrl = new URL("https", host, port, "/ce-cream/services/CREAM2");
        m_creamStub = new CREAMStub(m_creamUrl.toString());
        m_socketFactory = new CreamSocketFactory(cred, certs);
        m_port = port;
        m_host = host;
        m_credential = cred;
        m_delegationId = delegId;
        m_logger = Logger.getLogger(CreamClient.class);
    }

    public URI getServiceURI() throws MalformedURIException {
        return new URI(m_creamUrl.toString());
    }
    public ServiceInfo getServiceInfo() throws RemoteException, Authorization_Fault, Generic_Fault {
        this.registerProtocol();
        ServiceInfo r = m_creamStub.getServiceInfo(new ServiceInfoRequest()).getServiceInfoResponse();
        return r;
    }

    public JobRegisterResponse jobRegister(String jobDesc) throws RemoteException, Authorization_Fault, Generic_Fault, InvalidArgument_Fault, JobSubmissionDisabled_Fault {
        // create job description
        JobDescription jd = new JobDescription();
        jd.setJDL(jobDesc);
        jd.setAutoStart(false);
        jd.setDelegationId(m_delegationId);
        // submit job
        JobRegisterRequest request = new JobRegisterRequest();
        request.setJobDescriptionList(new JobDescription[]{jd});
        this.registerProtocol();
        JobRegisterResponse r = m_creamStub.jobRegister(request, null);
        return r;
    }

    public JobInfoResult[] jobInfo(String nativeJobId) throws NoSuccessException, RemoteException, Authorization_Fault, Generic_Fault, InvalidArgument_Fault {
        return this.jobInfo(new String[]{nativeJobId});
    }

    public JobInfoResult[] jobInfo(String[] nativeJobIdArray) throws NoSuccessException, RemoteException, Authorization_Fault, Generic_Fault, InvalidArgument_Fault {
        JobId[] jobIdList = new JobId[nativeJobIdArray.length];
        for (int i = 0; i < nativeJobIdArray.length; i++) {
            jobIdList[i] = new JobId();
            jobIdList[i].setId(nativeJobIdArray[i]);
            try {
                jobIdList[i].setCreamURL(new URI(m_creamUrl.toString()));
            } catch (MalformedURIException e) {
                throw new NoSuccessException(e);
            }
        }
        JobFilter filter = new JobFilter();
        filter.setDelegationId(m_delegationId);
        filter.setJobId(jobIdList);
        JobInfoRequest request = new JobInfoRequest();
        request.setJobInfoRequest(filter);
        
        this.registerProtocol();
        JobInfoResult[] r = m_creamStub.jobInfo(request).getResult();
        return r;
        
    }
    
    public Result[] jobStart(String nativeJobId) throws NoSuccessException, RemoteException, Authorization_Fault, Generic_Fault, InvalidArgument_Fault {
        JobFilter filter = this.getJobFilter(nativeJobId);

        JobStartRequest request = new JobStartRequest();
        request.setJobStartRequest(filter);

        this.registerProtocol();
        Result[] r = m_creamStub.jobStart(request).getJobStartResponse().getResult();
        return r;
    }
    
    public Result[] jobCancel(String nativeJobId) throws NoSuccessException, RemoteException, Authorization_Fault, Generic_Fault, InvalidArgument_Fault {
        JobFilter filter = this.getJobFilter(nativeJobId);

        JobCancelRequest request = new JobCancelRequest();
        request.setJobCancelRequest(filter);
        
        this.registerProtocol();
        Result[] r = m_creamStub.jobCancel(request).getJobCancelResponse().getResult();
        return r;
    }
    
    public Result[] jobClean(String nativeJobId) throws NoSuccessException, RemoteException, Authorization_Fault, Generic_Fault, InvalidArgument_Fault {
        JobFilter filter = this.getJobFilter(nativeJobId);

        JobPurgeRequest request = new JobPurgeRequest();
        request.setJobPurgeRequest(filter);
        
        this.registerProtocol();
        Result[] r = m_creamStub.jobPurge(request).getJobPurgeResponse().getResult();
        return r;
    }
    
    public Result[] jobSuspend(String nativeJobId) throws NoSuccessException, RemoteException, OperationNotSupported_Fault, Authorization_Fault, Generic_Fault, InvalidArgument_Fault {
        JobFilter filter = this.getJobFilter(nativeJobId);

        JobSuspendRequest request = new JobSuspendRequest();
        request.setJobSuspendRequest(filter);
        
        this.registerProtocol();
        Result[] r = m_creamStub.jobSuspend(request).getJobSuspendResponse().getResult();
        return r;
    }
    
    public Result[] jobResume(String nativeJobId) throws NoSuccessException, RemoteException, OperationNotSupported_Fault, Authorization_Fault, Generic_Fault, InvalidArgument_Fault {
        JobFilter filter = this.getJobFilter(nativeJobId);

        JobResumeRequest request = new JobResumeRequest();
        request.setJobResumeRequest(filter);
        
        this.registerProtocol();
        Result[] r = m_creamStub.jobResume(request).getJobResumeResponse().getResult();
        return r;
        
    }
    
    public JobId[] jobList() throws RemoteException, Authorization_Fault, Generic_Fault {
        this.registerProtocol();
        JobId[] r = m_creamStub.jobList().getResult();
        return r;
    }
    
    public void renewDelegation(String delegId, String vo) throws BadParameterException, NoSuccessException, AuthenticationFailedException {
        this.registerProtocol();
        DelegationStub delegationStub = new DelegationStub(m_host, m_port, vo);
        String m_delegProxy = delegationStub.renewDelegation(delegId, m_credential);
        // put new delegated proxy for multiple jobs
        if (m_delegProxy != null) {
            delegationStub.putProxy(delegId, m_delegProxy);
        }
    }

    public void disconnect() {
        m_creamStub = null;
        m_creamUrl = null;
    }

    private void registerProtocol() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        Protocol.registerProtocol("https", new Protocol("https", m_socketFactory, m_port));
        m_logger.debug(stackTraceElements[2].getMethodName() + "() has registered protocol https on port " + m_port);
    }
    
    protected JobFilter getJobFilter(String nativeJobId) throws NoSuccessException {
        JobId jobId = new JobId();
        jobId.setId(nativeJobId);
        try {
            jobId.setCreamURL(this.getServiceURI());
        } catch (MalformedURIException e) {
            throw new NoSuccessException(e);
        }
        JobFilter filter = new JobFilter();
        filter.setDelegationId(m_delegationId);
        filter.setJobId(new JobId[]{jobId});
        return filter;
    }

}
