package fr.in2p3.jsaga.adaptor.batchssh.job;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3FileHandle;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.HoldableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.SuspendableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import org.ogf.saga.error.*;

/******************************************************
 * File:   BatchSSHAdaptor
 * Author: Taha BENYEZZA & Yassine BACHAR
 * Date:   07 December 2010
 * ***************************************************/
public class BatchSSHJobAdaptor extends BatchSSHAdaptorAbstract implements JobControlAdaptor, SuspendableJobAdaptor, HoldableJobAdaptor {

	// TODO : implement CleanableJobAdaptor
	
    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BatchSSHMonitorAdaptor();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorXSLT("xsl/job/batchSSH.xsl");
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        // Creating the pbs script file's name using the randomUUID
        String FileName = UUID.randomUUID().toString() + ".pbs";
        // the scpript's contenent
        StringBuilder sb = new StringBuilder("#!/bin/bash\n");
        sb.append(jobDesc);
        // the submission command
        String SubmitCommand = "qsub " + FileName;
        String JobId = null;
        Session session = null;
        InputStream stdout;
        BufferedReader br;

        SFTPv3Client sftp;
        try {
			sftp = new SFTPv3Client(connexion);
			SFTPv3FileHandle script = sftp.createFile(FileName);
			sftp.write(script, 0, sb.toString().getBytes(), 0, sb.length());
			sftp.closeFile(script);
		} catch (IOException e1) {
			throw new NoSuccessException("Unable to send script via SFTP", e1);
		}

		// Openning a new session
        try {
        	session = this.sendCommand(SubmitCommand);
            // Retrieving the standard output
            stdout = new StreamGobbler(session.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            
            JobId = br.readLine();
            if (JobId == null) {
            	throw new IOException("qsub did not return a JobID");
            }
        } catch (IOException ex) {
			throw new NoSuccessException("Unable to submit job", ex);
        } finally {
            if (session != null) session.close();
            // remove the script and close the SFTP
            try {
				sftp.rm(FileName);
			} catch (IOException e) {
				// Ignore
			}
            sftp.close();
        }

        return JobId;
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        Session session = null;
        // the cancel command
        String CancelCommand = "qdel " + nativeJobId;

        // Openning a new session
        try {
            session = this.sendCommand(CancelCommand);
        } catch (IOException ex) {
			throw new NoSuccessException("Unable to cancel job", ex);
        } finally {
            if (session != null) session.close();
        }
    }

    public boolean suspend(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        Session session = null;
        // ok = true if the command is executed successfuly and false if not
        Boolean ok = true;
        // the holding command
        String HoldCommand = "qhold " + nativeJobId;

        // Openning a new session
        try {
        	session = this.sendCommand(HoldCommand);
        } catch (IOException ex) {
        	if (ex.getMessage().contains("errno=168")) { // qhold on a finished job
        		ok=false;
        	} else {
        		throw new NoSuccessException("Unable to suspend/hold job", ex);
        	}
        } finally {
            if (session != null) session.close();
        }

        return ok;
    }

    public boolean resume(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        Session session = null;
        Boolean ok = true;
        // the releasing command
        String ReleaseCommand = "qrls " + nativeJobId;

        // Openning a new session
        try {
        	session = this.sendCommand(ReleaseCommand);
        } catch (IOException ex) {
        	if (ex.getMessage().contains("errno=168")) { // qrls on a finished job
        		ok=false;
        	} else {
        		throw new NoSuccessException("Unable to resume/release job", ex);
        	}
        } finally {
            if (session != null) session.close();
        }

        return ok;
    }

	public boolean hold(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		return this.suspend(nativeJobId);
	}

	public boolean release(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		return this.release(nativeJobId);
	}
}
