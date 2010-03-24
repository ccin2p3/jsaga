package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.*;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;

import java.io.*;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorJobControlAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorJobControlAdaptor extends EmulatorJobAdaptorAbstract implements StagingJobAdaptorOnePhase, CleanableJobAdaptor, StreamableJobBatch {
    public int getDefaultPort() {
        return 1234;
    }

    public String getTranslator() {
        return "xsl/job/saga.xsl";
    }

    public Map getTranslatorParameters() {
        return null;    // no parameter
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new EmulatorJobMonitorAdaptor();
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // create id
        String nativeJobId = UUID.randomUUID().toString();

        // parse job description
        Properties prop = new Properties();
        try {
            prop.load(new ByteArrayInputStream(jobDesc.getBytes()));
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }

        // set end time
        long endTime;
        try {
            String totalCPUTime = prop.getProperty(JobDescription.TOTALCPUTIME);
            long duration = Long.parseLong(totalCPUTime) * 1000;
            endTime = System.currentTimeMillis() + duration;
        } catch (NumberFormatException e) {
            throw new NoSuccessException(e);
        }

        // create job
        File job = super.getJob(nativeJobId);
        try {
            OutputStream out = new FileOutputStream(job);
            out.write(Long.toString(endTime).getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            throw new PermissionDeniedException(e);
        } catch (IOException e) {
            throw new TimeoutException(e);
        }

        // returns id
        return nativeJobId;
    }

    public JobIOHandler submit(String jobDesc, boolean checkMatch, String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        final String nativeJobId = this.submit(jobDesc, checkMatch, uniqId);
        return new JobIOGetter() {
            private String m_nativeJobId = nativeJobId;
            private InputStream m_stdout = new ByteArrayInputStream("output\n".getBytes());
            private InputStream m_stderr = new ByteArrayInputStream("error\n".getBytes());
            public String getJobId() {return m_nativeJobId;}
            public InputStream getStdout() throws PermissionDeniedException, TimeoutException, NoSuccessException {return m_stdout;}
            public InputStream getStderr() throws PermissionDeniedException, TimeoutException, NoSuccessException {return m_stderr;}
        };
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        File job = super.getJob(nativeJobId);
        try {
            OutputStream out = new FileOutputStream(job);
            out.write('0');
            out.close();
        } catch (FileNotFoundException e) {
            throw new PermissionDeniedException(e);
        } catch (IOException e) {
            throw new TimeoutException(e);
        }
    }

    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        File job = super.getJob(nativeJobId);
        if (! job.delete()) {
            throw new PermissionDeniedException("Failed to cleanup job: "+nativeJobId);
        }
    }

    public String getStagingDirectory(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return null;
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobDescription, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return new StagingTransfer[]{};
    }

    public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return null;
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws TimeoutException, NoSuccessException {
        return new StagingTransfer[]{};
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws TimeoutException, NoSuccessException {
        return new StagingTransfer[]{};
    }
}
