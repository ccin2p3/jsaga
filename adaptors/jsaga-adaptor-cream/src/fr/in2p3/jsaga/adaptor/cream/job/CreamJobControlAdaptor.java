package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.glite.ce.creamapi.ws.cream2.CREAMPort;
import org.glite.ce.creamapi.ws.cream2.types.*;
import org.globus.ftp.GridFTPClient;
import org.ogf.saga.error.*;

import java.io.File;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
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
public class CreamJobControlAdaptor extends CreamJobAdaptorAbstract implements StreamableJobBatch, CleanableJobAdaptor {
    // parameters configured
    private static final String SSL_CA_FILES = "sslCAFiles";

    // parameters extracted from URI
    private static final String HOST_NAME = "HostName";
    private static final String BATCH_SYSTEM = "BatchSystem";
    private static final String QUEUE_NAME = "QueueName";

    private Map m_parameters;
    private String m_delegProxy;

    /** override super.getUsage() */
    public Usage getUsage() {
        return new UAnd(new Usage[]{
                super.getUsage(),
                new U(SSL_CA_FILES),
        });
    }

    /** override super.getDefaults() */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                new Default(SSL_CA_FILES, new File(new File(new File(System.getProperty("user.home"),".globus"),"certificates"),"*.0").getAbsolutePath())
        };
    }

    public String[] getSupportedSandboxProtocols() {
        return new String[]{"gsiftp"};
    }

    public String getTranslator() {
        return "xsl/job/cream-jdl.xsl";
    }

    public Map getTranslatorParameters() {
        return m_parameters;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        // use CREAM portType as default monitoring service (instead of CEMon portType)
        return new CreamJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // set delegationId and create stub for CREAM service
        super.connect(userInfo, host, port, basePath, attributes);

        // set SSL_CA_FILES
        System.setProperty("sslCAFiles", (String) attributes.get(SSL_CA_FILES));

        // extract parameters from basePath
        Matcher m = Pattern.compile("/cream-(.*)-(.*)").matcher(basePath);
        if (m.matches()) {
            m_parameters = new HashMap(2);
            m_parameters.put(HOST_NAME, host);
            m_parameters.put(BATCH_SYSTEM, m.group(1));
            m_parameters.put(QUEUE_NAME, m.group(2));
        } else {
            throw new BadParameterException("Path must be on the form: /cream-<lrms>-<queue>");
        }

        // renew/create delegated proxy
        DelegationStub delegationStub = new DelegationStub(host, port);
        m_delegProxy = delegationStub.renewDelegation(m_delegationId, m_credential);
        // put new delegated proxy for multiple jobs
        if (m_delegProxy != null) {
            delegationStub.putProxy(m_delegationId, m_delegProxy);
        }
    }

    public void disconnect() throws NoSuccessException {
        m_parameters.clear();
        m_delegProxy = null;
        super.disconnect();
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        String stagingDir = "/tmp/"+uniqId;
        if (jobDesc.contains("StdOutput") || jobDesc.contains("StdError")) {
            GridFTPClient stagingClient = this.getStagingClient();
            try {
                stagingClient.makeDir(stagingDir);
            } catch (Exception e) {
                throw new NoSuccessException("Failed to create staging directory: "+stagingDir, e);
            }
        }
        return this.doSubmit(jobDesc);
    }

    private String m_stagingPrefix;
    public JobIOHandler submit(String jobDesc, boolean checkMatch, String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        m_stagingPrefix = "/tmp/"+uniqId;
        GridFTPClient stagingClient = this.getStagingClient();
        String jobId = this.doSubmit(jobDesc);
        return new CreamJobIOHandler(stagingClient, m_stagingPrefix, jobId);
    }

    private GridFTPClient getStagingClient() throws NoSuccessException {
        try {
            GridFTPClient client = new GridFTPClient(m_creamStub.getURI().getHost(), 2811);
            client.authenticate(m_credential);
            return client;
        } catch (Exception e) {
            throw new NoSuccessException("Failed to connect to GridFTP server: "+m_creamStub.getURI().getHost(), e);
        }
    }
    private String doSubmit(String jobDesc) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // create job description
        JobDescription jd = new JobDescription();
        jd.setJDL(jobDesc);
        jd.setAutoStart(true);
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

        // rethrow exception
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            BaseFaultType fault = null;
            if(resultArray[i].getDelegationProxyFault() != null) {
                fault = resultArray[i].getDelegationProxyFault();
            } else if(resultArray[i].getDelegationIdMismatchFault() != null) {
                fault = resultArray[i].getDelegationIdMismatchFault();
            } else if(resultArray[i].getGenericFault() != null) {
                fault = resultArray[i].getGenericFault();
            } else if(resultArray[i].getLeaseIdMismatchFault() != null) {
                fault = resultArray[i].getLeaseIdMismatchFault();
            }
            if (fault != null) {
                String message = fault.getFaultCause()!=null && !fault.getFaultCause().equals("N/A")
                        ? fault.getFaultCause()
                        : fault.getClass().getName();
                throw new NoSuccessException(message, fault);
            }
        }

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

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobId jobId = new JobId();
        jobId.setCreamURL(m_creamStub.getURI());
        jobId.setId(nativeJobId);
        JobFilter filter = new JobFilter();
        filter.setDelegationId(m_delegationId);
        filter.setJobId(new JobId[]{jobId});

        // cancel job
        CREAMPort stub = m_creamStub.getStub();
        Result[] resultArray;
        try {
            resultArray = stub.jobCancel(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }

        // rethrow exception
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            BaseFaultType fault = null;
            if (resultArray[i].getDateMismatchFault() != null) {
                fault = resultArray[i].getDateMismatchFault();
            } else if (resultArray[i].getDelegationIdMismatchFault() != null) {
                fault = resultArray[i].getDelegationIdMismatchFault();
            } else if (resultArray[i].getGenericFault() != null) {
                fault = resultArray[i].getGenericFault();
            } else if (resultArray[i].getJobStatusInvalidFault() != null) {
                fault = resultArray[i].getJobStatusInvalidFault();
            } else if (resultArray[i].getJobUnknownFault() != null) {
                fault = resultArray[i].getJobUnknownFault();
            } else if (resultArray[i].getLeaseIdMismatchFault() != null) {
                fault = resultArray[i].getLeaseIdMismatchFault();
            }
            if (fault != null) {
                String message = fault.getFaultCause()!=null && !fault.getFaultCause().equals("N/A")
                        ? fault.getFaultCause()
                        : fault.getClass().getName();
                throw new NoSuccessException(message, fault);
            }
        }
    }

    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        if (m_stagingPrefix != null) {
            try {
                GridFTPClient client = new GridFTPClient(m_creamStub.getURI().getHost(), 2811);
                client.authenticate(m_credential);
                client.deleteFile(m_stagingPrefix+"-"+CreamJobIOHandler.OUTPUT_SUFFIX);
                client.deleteFile(m_stagingPrefix+"-"+CreamJobIOHandler.ERROR_SUFFIX);
            } catch (Exception e) {
                throw new NoSuccessException("Failed to cleanup job: "+nativeJobId, e);
            }
        }
    }
}
