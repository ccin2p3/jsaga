package fr.in2p3.jsaga.adaptor.batchssh.job;

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
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.ogf.saga.error.*;

/******************************************************
 * File:   BatchSSHAdaptor
 * Author: Taha BENYEZZA & Yassine BACHAR
 * Author: Lionel Schwarz
 * Date:   07 December 2010
 * ***************************************************/
public class BatchSSHJobAdaptor extends BatchSSHAdaptorAbstract implements JobControlAdaptor, /*StreamableJobBatch,*/
	SuspendableJobAdaptor, HoldableJobAdaptor, StagingJobAdaptorOnePhase {

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BatchSSHMonitorAdaptor();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorXSLT("xsl/job/batchSSH.xsl");
    }

	public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        // Get executable name for chmod
    	ArrayList<String[]> transfers = getCustomizedParams(jobDesc, BatchSSHJob.ATTR_VAR_JSAGA_EXECUTABLE);
    	String executableFileName = (this.m_stagingDirectory != null)?this.m_stagingDirectory + "/":"";
    	executableFileName += transfers.get(0)[0];
		// Creating the pbs script file's name using the randomUUID
        String FileName = uniqId + ".pbs";
        // the script's content
System.out.println(jobDesc);        
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

		// chmod the executable
		try {
			session = this.sendCommand("/bin/chmod a+x " + executableFileName);
		} catch (Exception e) {
			// do nothing
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
        } catch (BatchSSHCommandFailedException e) {
        	if (e.isErrorTypeOfBadResource()) {
        		throw new BadResource("Error in Job description", e);
        	}
			throw new NoSuccessException("Unable to submit job", e);
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
        } catch (BatchSSHCommandFailedException e) {
			throw new NoSuccessException("Unable to cancel job", e);
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
    		throw new NoSuccessException("Unable to suspend/hold job", ex);
        } catch (BatchSSHCommandFailedException ex) {
        	if (ex.getErrno() == BatchSSHCommandFailedException.PBS_QHOLD_E_JOB_INVALID_STATE) { // qhold on a finished job
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
    		throw new NoSuccessException("Unable to resume/release job", ex);
        } catch (BatchSSHCommandFailedException ex) {
        	if (ex.getErrno() == BatchSSHCommandFailedException.PBS_QHOLD_E_JOB_INVALID_STATE) { // qrls on a finished job
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

	public String getStagingDirectory(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
        BatchSSHJob bj = this.getAttributes(new String[]{nativeJobId}).get(0);
        return makeTURL(bj.getAttribute(BatchSSHJob.ATTR_VAR_WORKDIR));
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		return this.getStagingTransfers(nativeJobId, true);
	}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		return this.getStagingTransfers(nativeJobId, false);
	}

	private StagingTransfer[] getStagingTransfers(String nativeJobId, boolean input) throws NoSuccessException {
        BatchSSHJob bj = this.getAttributes(new String[]{nativeJobId}).get(0);
    	ArrayList<String[]> transfers = bj.getStagingTransfers(input);
		return this.arrayListToStagingTransfers(transfers, input);
	}
	
	public String getStagingDirectory(String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		String dirUrl = "sftp://" + connexion.getHostname() + ":" + connexion.getPort(); 
        if (this.m_stagingDirectory != null)
        	// TODO: how to get the $HOME ???
        	dirUrl += "/" + this.m_stagingDirectory;
        return dirUrl;
	}

	public StagingTransfer[] getInputStagingTransfer(
			String nativeJobDescription, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
    	ArrayList<String[]> transfers = getCustomizedParams(nativeJobDescription, BatchSSHJob.ATTR_VAR_JSAGA_STAGEIN);
		return this.arrayListToStagingTransfers(transfers, true);
	}

	private StagingTransfer[] arrayListToStagingTransfers(ArrayList<String[]> transfers, boolean input) {
    	StagingTransfer[] st = new StagingTransfer[transfers.size()];
		for (int i=0; i<transfers.size(); i++) {
			String[] path_pair = (String[])transfers.get(i);
			String from, to;
			if (input) {
				to = this.makeTURL(path_pair[1]); // remote sftp:// 
				from = path_pair[0]; // local
			} else {
				from = this.makeTURL(path_pair[1]); // remote sftp:// 
				to = path_pair[0]; // local
			}
System.out.println("TR " + from + " => " + to);	    	
			st[i] = new StagingTransfer(from, to, false);
		}
		return st;
	}

	private ArrayList<String[]> getCustomizedParams(String nativeJobDescription, String filter) throws NoSuchElementException {
//    	ArrayList params = new ArrayList();
		Scanner sc = new Scanner(nativeJobDescription);
		String line;
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (line.startsWith("#PBS -v")) {
				line = line.substring("#PBS -v".length());
				return BatchSSHJob.getFilteredVars(line, filter);
			}
		}
		return new ArrayList<String[]>();
	}
	
	private String makeTURL(String filename) {
    	return "sftp://" + connexion.getHostname() + ":" + connexion.getPort() + "/" + filename;
	}
}
