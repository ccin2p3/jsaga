package fr.in2p3.jsaga.adaptor.cream.job;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Calendar;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMReader;
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
import org.glite.ce.security.delegation.DelegationException_Fault;
import org.glite.ce.security.delegation.DelegationServiceStub;
import org.glite.ce.security.delegation.DelegationServiceStub.GetProxyReq;
import org.glite.ce.security.delegation.DelegationServiceStub.GetTerminationTime;
import org.glite.ce.security.delegation.DelegationServiceStub.PutProxy;
import org.glite.ce.security.delegation.DelegationServiceStub.RenewProxyReq;
import org.globus.gsi.CredentialException;
import org.globus.gsi.X509Credential;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.proxy.ProxyGenerator;
import eu.emi.security.authn.x509.proxy.ProxyRequestOptions;
import fr.in2p3.jsaga.adaptor.cream.CreamSocketFactory;

public class CreamClient {

    protected CREAMStub m_creamStub;
    private DelegationServiceStub m_delegationStub;
    protected URL m_creamUrl;
    private ProtocolSocketFactory m_socketFactory;
    private String m_delegationId;
    private Logger m_logger;

    public CreamClient(String host, int port, GSSCredential cred, File certs, String delegId) throws MalformedURLException, AxisFault, AuthenticationFailedException {
        m_creamUrl = new URL("https", host, port, "/ce-cream/services/CREAM2");
        m_creamStub = new CREAMStub(m_creamUrl.toString());
        m_delegationStub = new DelegationServiceStub(new URL("https", host, port, "/ce-cream/services/gridsite-delegation").toString());
        m_socketFactory = new CreamSocketFactory(cred, certs);
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
    
    public void renewDelegation(X509Credential globusProxy) 
            throws BadParameterException, NoSuccessException, AuthenticationFailedException {

        String pkcs10 = null;
        try {
            GetTerminationTime gtt = new GetTerminationTime();
            gtt.setDelegationID(m_delegationId);
            this.registerProtocol();
            Calendar cal = m_delegationStub.getTerminationTime(gtt).getGetTerminationTimeReturn();
            m_logger.debug("DelegationID " + m_delegationId + " termination time is: " + DateFormat.getTimeInstance().format(cal.getTime()));
            if (cal.after(Calendar.getInstance())) {
                return;
            }
            // renew delegation
            m_logger.info("Requesting a proxy delegation renewal");
            RenewProxyReq rpq = new RenewProxyReq();
            rpq.setDelegationID(m_delegationId);
            this.registerProtocol();
            pkcs10 = m_delegationStub.renewProxyReq(rpq).getRenewProxyReqReturn();
        } catch (Exception e) {
            // New CreamCE sends a RemoteException when delegationId not found
            if (e.getMessage()!=null && 
                    (e.getMessage().contains("not found") || e.getMessage().startsWith("Failed to find delegation ID"))
               )
            {
                // create a new delegation
                try {
                    GetProxyReq gpr = new GetProxyReq();
                    gpr.setDelegationID(m_delegationId);
                    m_logger.info("Requesting a proxy delegation creation");
                    this.registerProtocol();
                    pkcs10 = m_delegationStub.getProxyReq(gpr).getGetProxyReqReturn();
                } catch (RemoteException e1) {
                    throw new AuthenticationFailedException(e);
                } catch (DelegationException_Fault e1) {
                    throw new AuthenticationFailedException(e);
                }
            } else {
                // rethrow exception
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
        }
        // set delegation lifetime
        int hours = (int) (globusProxy.getTimeLeft() / 3600) - 1;
        if (hours < 0) {
            throw new AuthenticationFailedException("Proxy is expired or about to expire: "+globusProxy.getIdentity());
        }

        try {
            PrivateKey pKey = globusProxy.getPrivateKey();
            X509Certificate[] parentChain = globusProxy.getCertificateChain();
            
            PEMReader pemReader = new PEMReader(new StringReader(pkcs10));
            PKCS10CertificationRequest proxytReq = (PKCS10CertificationRequest) pemReader.readObject();
            pemReader.close();
            ProxyRequestOptions csrOpt = new ProxyRequestOptions(parentChain, proxytReq);
            csrOpt.setLifetime(hours*3600);
            
            X509Certificate[] certChain = ProxyGenerator.generate(csrOpt, pKey);
            
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            for (X509Certificate tmpcert : certChain) {
                CertificateUtils.saveCertificate(outStream, tmpcert, CertificateUtils.Encoding.PEM);
            }
            
            String delegProxy = outStream.toString();

            PutProxy pp = new PutProxy();
            pp.setDelegationID(m_delegationId);
            pp.setProxy(delegProxy);
            m_logger.debug("Sending the proxy to delegationID= " + m_delegationId);
            this.registerProtocol();
            m_delegationStub.putProxy(pp);
        } catch (InvalidKeyException e) {
            throw new AuthenticationFailedException(e);
        } catch (CertificateException e) {
            throw new AuthenticationFailedException(e);
        } catch (SignatureException e) {
            throw new AuthenticationFailedException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationFailedException(e);
        } catch (NoSuchProviderException e) {
            throw new AuthenticationFailedException(e);
        } catch (IOException e) {
            throw new AuthenticationFailedException(e);
        } catch (DelegationException_Fault e) {
            throw new AuthenticationFailedException(e);
        } catch (CredentialException e) {
            throw new AuthenticationFailedException(e);
        }
    }

    public void disconnect() {
        m_creamStub = null;
        this.m_delegationStub = null;
        m_creamUrl = null;
    }

    private void registerProtocol() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        Protocol.registerProtocol(this.m_creamUrl.getProtocol(), new Protocol(this.m_creamUrl.getProtocol(), m_socketFactory, this.m_creamUrl.getPort()));
        m_logger.debug(stackTraceElements[2].getMethodName() + "() has registered protocol " + this.m_creamUrl.getProtocol() + " on port " + this.m_creamUrl.getPort());
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
