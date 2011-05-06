package fr.in2p3.jsaga.adaptor.batchssh.job;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
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
public class BatchSSHJobAdaptor extends BatchSSHAdaptorAbstract implements JobControlAdaptor, SuspendableJobAdaptor {

    public String getType() {
        return "pbs+ssh";
    }

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
        String CreateCommand = "echo '" + sb.toString() + "' > " + FileName;
        // the submission command
        String SubmitCommand = "qsub " + FileName;
        // the file deleting command
        String DeleteCommand = "rm " + FileName;
        String JobId = null;
        // a new ganymed session instance
        Session session = null;
        InputStream stdout;
        BufferedReader br;

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

        // Openning a new session
        try {
            session = connexion.openSession();

            //submitting the job
            try {
                session.execCommand(SubmitCommand);
            } catch (IOException ex) {
                System.out.println("Executing command error :" + ex.getMessage());
            }
            // waiting for the qsub command to end
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);

            // Retrieving the standard output
            try {
                stdout = new StreamGobbler(session.getStdout());
                br = new BufferedReader(new InputStreamReader(stdout));

                JobId = br.readLine();
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }

        } catch (IOException ex) {
            System.out.println("Opening session error :" + ex.getMessage());
        } finally {
            // closing the second session
            session.close();
        }
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
            try {
                session.execCommand(CancelCommand);
            } catch (IOException ex) {
                System.out.println("Exuctuting command error :" + ex.getMessage());
            }
            // waiting for the delete command to end
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);
        } catch (IOException ex) {
            System.out.println("Opening session error :" + ex.getMessage());
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
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);

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
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);
        } catch (IOException ex) {
            System.out.println("Opening session error :" + ex.getMessage());
            ok = false;
        } finally {
            // clossing the second session
            session.close();
        }

        return ok;
    }

    public int getDefaultPort() {
        return 22;
    }
}
