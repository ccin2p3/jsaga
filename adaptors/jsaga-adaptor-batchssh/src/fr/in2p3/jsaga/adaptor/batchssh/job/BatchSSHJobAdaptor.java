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
        // the script file creating command
        //String CreateCommand = "echo '" + sb.toString() + "' > " + FileName;
        //String CreateCommand = "cat << EOF  > /tmp/" + FileName + "\n";
        //CreateCommand += sb.toString();
        //CreateCommand += "EOF\n";
//System.out.println(CreateCommand);
        // the submission command
        String SubmitCommand = "qsub " + FileName;
        // the file deleting command
        //String DeleteCommand = "rm " + FileName;
        String JobId = null;
        // a new ganymed session instance
        Session session = null;
        InputStream stdout;
        BufferedReader br;

        /*
        try {
            session = connexion.openSession();
            // creating the pbs file
            try {
                session.execCommand(CreateCommand);
            } catch (IOException ex) {
                System.out.println("Executing command error :" + ex.getMessage());
            }
            // waiting for the creating command to end
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);

        } catch (IOException ex) {
            System.out.println("Opening session error :" + ex.getMessage());
        } finally {
            // closing the first session
            session.close();
        }
		*/
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
            session = connexion.openSession();

            session.execCommand(SubmitCommand);
            
            // waiting for the qsub command to end
            int conditions = session.waitForCondition( ChannelCondition.EXIT_STATUS, 0);

            int exitStatus = session.getExitStatus();
            if (exitStatus != 0) {
            	throw new IOException("qsub returned: " + exitStatus);
            }
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
            // closing the second session
            session.close();
            // remove the script and close the SFTP
            try {
				sftp.rm(FileName);
			} catch (IOException e) {
				// Ignore
			}
            sftp.close();
        }
        /*
        // Openning a new session
        try {
            session = connexion.openSession();
            //Deletting the pbs file
            try {
                session.execCommand(DeleteCommand);
            } catch (IOException ex) {
                System.out.println("Exuctuting command error :" + ex.getMessage());
            }
            // waiting for the delete command to end
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);
        } catch (IOException ex) {
            System.out.println("Opening session error :" + ex.getMessage());
        } finally {
            // closing the third session
            session.close();
        }
        */

        return JobId;
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        Session session = null;
        // the cancel command
        String CancelCommand = "qdel " + nativeJobId;

        // Openning a new session
        try {
            session = connexion.openSession();

            //Canceling the job
            session.execCommand(CancelCommand);
            // waiting for the delete command to end
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);
        } catch (IOException ex) {
			throw new NoSuccessException("Unable to cancel job", ex);
        } finally {
            // closing the session
            session.close();
        }
    }

    public boolean suspend(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        Session session = null;
        // ok = true if the command is executed successfuly and false if not
        Boolean ok = true;
        // the holding command
        String HoldCommand = "qhold " + nativeJobId;

        // TODO : remove included try catch
        // Openning a new session
        try {
            session = connexion.openSession();

            //holding the job
            try {
                session.execCommand(HoldCommand);
            } catch (IOException ex) {
                System.out.println("Exuctuting command error :" + ex.getMessage());
                ok = false;
            }
            // waiting for the delete command to end
            int conditions = session.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
            int exitStatus = session.getExitStatus();
            if (exitStatus != 0) {
            	ok = false;
            }
        } catch (IOException ex) {
            System.out.println("Opening session error :" + ex.getMessage());
            ok = false;
        } finally {
            // clossing the second session
            session.close();
        }

        return ok;
    }

    public boolean resume(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        Session session = null;
        // ok = true if the command is executed successfuly and false if not
        Boolean ok = true;
        // the releasing command
        String ReleaseCommand = "qrls " + nativeJobId;

        // TODO : remove included try catch
        // Openning a new session
        try {
            session = connexion.openSession();
            //Releasing the job
            try {
                session.execCommand(ReleaseCommand);
            } catch (IOException ex) {
                System.out.println("Exuctuting command error :" + ex.getMessage());
                ok = false;
            }
            // waiting for the delete command to end
            int conditions = session.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
            int exitStatus = session.getExitStatus();
            if (exitStatus != 0) {
            	ok = false;
            }
        } catch (IOException ex) {
            System.out.println("Opening session error :" + ex.getMessage());
            ok = false;
        } finally {
            // clossing the second session
            session.close();
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
