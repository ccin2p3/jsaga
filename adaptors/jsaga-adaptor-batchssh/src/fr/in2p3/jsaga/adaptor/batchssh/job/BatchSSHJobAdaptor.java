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
import java.util.List;
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
	SuspendableJobAdaptor, HoldableJobAdaptor/*, StagingJobAdaptorOnePhase*/ {

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BatchSSHMonitorAdaptor();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorXSLT("xsl/job/batchSSH.xsl");
    }

	/*public JobIOGetterInteractive submitInteractive(String jobDesc,
			boolean checkMatch) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
        String SubmitCommand = "qsub -I ";

		// Openning a new session
        try {
            Session session = connexion.openSession();
        	//session = this.sendCommand(SubmitCommand);
        	session.execCommand(SubmitCommand);
            return new BatchSSHJobIOHandler(session);
        } catch (IOException ex) {
			throw new NoSuccessException("Unable to submit job", ex);
        }

	}*/

	public JobIOHandler submit(String jobDesc, boolean checkMatch,
			String uniqId, InputStream stdin) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
        // Creating the pbs script file's name using the randomUUID
        //String FileName = uniqId + ".pbs";
        // the scpript's contenent
//System.out.println(jobDesc);        
        StringBuilder sb = new StringBuilder("#!/bin/bash\n");
        sb.append(jobDesc);
        // the submission command
        String SubmitCommand = "echo '" + sb + "' | qsub -I "; //;+ FileName;
        //String JobId = null;
        Session session = null;
        InputStream stdout;
        BufferedReader br;

        /*
        SFTPv3Client sftp;
        try {
			sftp = new SFTPv3Client(connexion);
			SFTPv3FileHandle script = sftp.createFile(FileName);
			sftp.write(script, 0, sb.toString().getBytes(), 0, sb.length());
			int n;
			int offset = sb.length();
			byte[] buffer = new byte[1024];
			while ((n = stdin.read(buffer)) != -1) {
				sftp.write(script, offset, buffer, 0, n);
				offset = offset+n;
			}
			sftp.closeFile(script);
		} catch (IOException e1) {
			throw new NoSuccessException("Unable to send script via SFTP", e1);
		}
		*/
		// Openning a new session
        try {
        	session = connexion.openSession();//this.sendCommand(SubmitCommand);
            // Retrieving the standard output
            //stdout = new StreamGobbler(session.getStdout());
            //br = new BufferedReader(new InputStreamReader(stdout));
            
            //JobId = br.readLine();
            //if (JobId == null) {
            //	throw new IOException("qsub did not return a JobID");
            //}
        	session.execCommand(SubmitCommand);
        } catch (IOException ex) {
			throw new NoSuccessException("Unable to submit job", ex);
        /*} catch (BatchSSHCommandFailedException e) {
        	if (e.isErrorTypeOfBadResource()) {
        		throw new BadResource("Error in Job description", e);
        	}
			throw new NoSuccessException("Unable to submit job", e);*/
        /*} finally {
            if (session != null) session.close();
            // remove the script and close the SFTP
            try {
				sftp.rm(FileName);
			} catch (IOException e) {
				// Ignore
			}
            sftp.close();*/
        }

        return new BatchSSHJobIOHandler(session);
		
	}

	public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        // Creating the pbs script file's name using the randomUUID
        String FileName = uniqId + ".pbs";
        // the scpript's contenent
//System.out.println(jobDesc);        
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
		String[][] path_pairs = bj.getStagingTransfers(input);
    	StagingTransfer[] st = new StagingTransfer[path_pairs.length];
		for (int i=0; i<path_pairs.length; i++) {
	    	String to = path_pairs[i][0]; // local 
	    	String from = this.makeTURL(path_pairs[i][1]); // remote sftp://
			st[i] = new StagingTransfer(from, to, false);
		}
		return st;
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
    	ArrayList transfers = new ArrayList();
    	// TODO put something in it!
//System.out.println(nativeJobDescription);
		Scanner sc = new Scanner(nativeJobDescription);
		String var_string=null;
		String line;
		try {
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				if (line.startsWith("#PBS -v")) {
					line = line.substring("#PBS -v".length());
					transfers = BatchSSHJob.getFilteredVars(line, BatchSSHJob.ATTR_VAR_JSAGA_STAGEIN);
					break;
				}
			}
		} catch (NoSuchElementException nsee) {
			return new StagingTransfer[]{};
		}
    	StagingTransfer[] st = new StagingTransfer[transfers.size()];
		for (int i=0; i<transfers.size(); i++) {
			String[] path_pair = (String[])transfers.get(i);
	    	String to = this.makeTURL(path_pair[0]); // remote sftp:// 
	    	String from = path_pair[1]; // local
System.out.println("TR " + from + " => " + to);	    	
			st[i] = new StagingTransfer(from, to, false);
		}
    	//return (StagingTransfer[]) transfers.toArray(st);		
		return st;
	}

	private String makeTURL(String filename) {
    	return "sftp://" + connexion.getHostname() + ":" + connexion.getPort() + "/" + filename;
	}
}
