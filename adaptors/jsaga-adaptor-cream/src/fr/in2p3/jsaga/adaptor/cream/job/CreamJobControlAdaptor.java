package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.glite.ce.creamapi.ws.cream2.CREAMPort;
import org.glite.ce.creamapi.ws.cream2.types.*;
import org.globus.ftp.GridFTPClient;
import org.ogf.saga.error.*;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
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
public class CreamJobControlAdaptor extends CreamJobAdaptorAbstract implements StagingJobAdaptorTwoPhase, StreamableJobBatch, CleanableJobAdaptor {
    // constant
    private static final String SANDBOX_BASE_URI = "file:///sandbox/";

    // parameters configured
    private static final String SSL_CA_FILES = "sslCAFiles";

    // parameters extracted from URI
    private static final String BATCH_SYSTEM = "BatchSystem";
    private static final String QUEUE_NAME = "QueueName";
    private String m_batchSystem;
    private String m_queueName;

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
            m_batchSystem = m.group(1);
            m_queueName = m.group(2);
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
        m_delegProxy = null;
        super.disconnect();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        JobDescriptionTranslator translator = new JobDescriptionTranslatorXSLT("xsl/job/cream-jdl.xsl");
        translator.setAttribute(BATCH_SYSTEM, m_batchSystem);
        translator.setAttribute(QUEUE_NAME, m_queueName);
        return translator;
    }

    private String m_stagingPrefix;
    public JobIOHandler submit(String jobDesc, boolean checkMatch, String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        m_stagingPrefix = "/tmp/"+uniqId;

        // connect to gsiftp
        GridFTPClient stagingClient;
        try {
            stagingClient = new GridFTPClient(m_creamStub.getURI().getHost(), 2811);
            stagingClient.authenticate(m_credential);
        } catch (Exception e) {
            throw new NoSuccessException("Failed to connect to GridFTP server: "+m_creamStub.getURI().getHost(), e);
        }

        // submit
        String jobId = this.submit(jobDesc, checkMatch, uniqId);
        return new CreamJobIOHandler(stagingClient, m_stagingPrefix, jobId);
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
        return null;    // use the WMS default staging directory
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws TimeoutException, NoSuccessException {
        JobInfo jobInfo = this.getJobInfo(nativeJobId);
        String baseUri = jobInfo.getCREAMInputSandboxURI();
        Properties jobDesc = parseJobDescription(jobInfo.getJDL());
        int transfersLength = getIntValue_IfExists(jobDesc, "InputSandboxPreStaging");
        StagingTransfer[] transfers = new StagingTransfer[transfersLength];
        for (int i=0; i<transfersLength; i++) {
            String from = getStringValue(jobDesc, "InputSandboxPreStaging_"+i+"_From");
            String to = getStringValue(jobDesc, "InputSandboxPreStaging_"+i+"_To");
            if (to.startsWith(SANDBOX_BASE_URI)) {
                to = baseUri+"/"+to.replace(SANDBOX_BASE_URI, "");
            }
            boolean append = getBooleanValue(jobDesc, "InputSandboxPreStaging_"+i+"_Append");
            transfers[i] = new StagingTransfer(from, to, append);
        }
        return transfers;
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws TimeoutException, NoSuccessException {
        JobInfo jobInfo = this.getJobInfo(nativeJobId);
        String baseUri = jobInfo.getCREAMOutputSandboxURI();
        Properties jobDesc = parseJobDescription(jobInfo.getJDL());
        int transfersLength = getIntValue_IfExists(jobDesc, "OutputSandboxPostStaging");
        StagingTransfer[] transfers = new StagingTransfer[transfersLength];
        for (int i=0; i<transfersLength; i++) {
            String from = getStringValue(jobDesc, "OutputSandboxPostStaging_"+i+"_From");
            if (from.startsWith(SANDBOX_BASE_URI)) {
                from = baseUri+"/"+from.replace(SANDBOX_BASE_URI, "");
            }
            String to = getStringValue(jobDesc, "OutputSandboxPostStaging_"+i+"_To");
            boolean append = getBooleanValue(jobDesc, "OutputSandboxPostStaging_"+i+"_Append");
            transfers[i] = new StagingTransfer(from, to, append);
        }
        return transfers;
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
        if (m_stagingPrefix != null) {
            try {
                GridFTPClient client = new GridFTPClient(m_creamStub.getURI().getHost(), 2811);
                client.authenticate(m_credential);
                client.deleteFile(m_stagingPrefix+"-"+CreamJobIOHandler.OUTPUT_SUFFIX);
                client.deleteFile(m_stagingPrefix+"-"+CreamJobIOHandler.ERROR_SUFFIX);
                client.close();
            } catch (Exception e) {
                throw new NoSuccessException("Failed to cleanup job: "+nativeJobId, e);
            }
        }

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

    private JobFilter getJobFilter(String nativeJobId) throws NoSuccessException {
        JobId jobId = new JobId();
        jobId.setCreamURL(m_creamStub.getURI());
        jobId.setId(nativeJobId);
        JobFilter filter = new JobFilter();
        filter.setDelegationId(m_delegationId);
        filter.setJobId(new JobId[]{jobId});
        return filter;
    }

    private static Properties parseJobDescription(String jdl) throws NoSuccessException {
        Properties jobDesc = new Properties();
        try {
            jobDesc.load(new ByteArrayInputStream(jdl.getBytes()));
        } catch (IOException e) {
            throw new NoSuccessException("Failed to retrieve JDL", e);
        }
        return jobDesc;
    }
    private static String getValue(Properties jobDesc, String key) throws NoSuccessException {
        String value = jobDesc.getProperty(key);
        if (value!=null) {
            String trimmed = value.trim();
            if (trimmed.endsWith(";")) {
                return trimmed.substring(0, trimmed.length()-1);
            } else {
                throw new NoSuccessException("Failed to parse JDL attribute: "+value);
            }
        } else {
            return null;
        }
    }
    private static String getStringValue(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        } else {
            throw new NoSuccessException("Failed to parse JDL attribute: "+value);
        }
    }
    private static String getStringValue_IfExists(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length()-1);
            } else {
                throw new NoSuccessException("Failed to parse JDL attribute: "+value);
            }
        } else {
            return null;
        }
    }
    private static int getIntValue_IfExists(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new NoSuccessException("Failed to parse JDL attribute: "+value, e);
            }
        } else {
            return 0;
        }
    }
    private static boolean getBooleanValue(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null) {
            return Boolean.parseBoolean(value);
        } else {
            throw new NoSuccessException("Failed to parse JDL attribute: "+value);
        }
    }
}
