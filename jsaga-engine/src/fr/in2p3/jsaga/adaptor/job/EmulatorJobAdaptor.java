package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.adaptor.job.monitor.*;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorJobAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorJobAdaptor implements JobControlAdaptor, CleanableJobAdaptor, QueryIndividualJob, StreamableJobBatch {
    private static Map<String,EmulatorJobStatus> s_status = new HashMap<String,EmulatorJobStatus>();

    public String getType() {
        return "test";
    }

    public Usage getUsage() {return null;}
    public Default[] getDefaults(Map attributes) throws IncorrectState {return null;}
    public Class[] getSupportedSecurityAdaptorClasses() {return null;}
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {}
    public int getDefaultPort() {return 5678;}
    public String[] getSupportedSandboxProtocols() {return null;}
    public String getTranslator() {return null;}
    public Map getTranslatorParameters() {return null;}
    public JobMonitorAdaptor getDefaultJobMonitor() {return this;}

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        //todo: split JobControlAdaptor and JobMonitorAdaptor
        // => JobControlAdaptor: create new status map and register JobControlAdaptor
        // => JobMonitorAdaptor: get status map from JobControlAdaptor
    }

    public void disconnect() throws NoSuccess {
        //todo: split JobControlAdaptor and JobMonitorAdaptor
        // => JobControlAdaptor: unregister JobControlAdaptor
        // => JobMonitorAdaptor: do nothing
    }

    public String submit(String jobDesc, boolean checkMatch) throws PermissionDenied, Timeout, NoSuccess {
        String nativeJobId = UUID.randomUUID().toString();
        s_status.put(nativeJobId, new EmulatorJobStatus(nativeJobId, SubState.SUBMITTED));
        return nativeJobId;
    }

    public JobIOHandler submit(String jobDesc, boolean checkMatch, InputStream stdin) throws PermissionDenied, Timeout, NoSuccess {
        final String nativeJobId = this.submit(jobDesc, checkMatch);
        return new JobIOGetter() {
            private String m_nativeJobId = nativeJobId;
            private InputStream m_stdout = new ByteArrayInputStream("output\n".getBytes());
            private InputStream m_stderr = new ByteArrayInputStream("error\n".getBytes());
            public String getJobId() {return m_nativeJobId;}
            public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess {return m_stdout;}
            public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess {return m_stderr;}
        };
    }

    public void cancel(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        s_status.put(nativeJobId, new EmulatorJobStatus(nativeJobId, SubState.CANCELED));
    }

    public void clean(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
        s_status.remove(nativeJobId);
    }

    public JobStatus getStatus(String nativeJobId) throws Timeout, NoSuccess {
        EmulatorJobStatus currentStatus = s_status.get(nativeJobId);
        if (currentStatus.getSubState().getValue()==SubState.SUBMITTED.getValue() && currentStatus.getElapsedTime()>1000) {
            s_status.put(nativeJobId, new EmulatorJobStatus(nativeJobId, SubState.DONE));
        }
        return s_status.get(nativeJobId);
    }
}
