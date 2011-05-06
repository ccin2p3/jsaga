/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.in2p3.jsaga.adaptor.batchssh.job;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;

/******************************************************
 * File:   BatchSSHMonitorAdaptor
 * Author: Taha BENYEZZA & Yassine BACHAR
 * Date:   07 December 2010
 * ***************************************************/
public class BatchSSHMonitorAdaptor extends BatchSSHAdaptorAbstract implements JobMonitorAdaptor, QueryIndividualJob {

    private static Map<String, SubState> StatusMap = new HashMap<String, SubState>();

    public String getType() {
        return "pbs+ssh";
    }

    public int getDefaultPort() {
        return 22;
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        // initialisinf the StatusMap
        initMap();
        String JobState = null;
        Session session = null;
        // the qstat Command
        String StatusCommand = "qstat -f " + nativeJobId;
        InputStream stdout;
        BufferedReader br;
        String qstatOutput = "";
        String outKey = "";

        try {
            session = connexion.openSession();

            //executing the qstat command
            try {
                session.execCommand(StatusCommand);
            } catch (IOException ex) {
                System.out.println("Executing command error :" + ex.getMessage());
            }

            try {
                stdout = new StreamGobbler(session.getStdout());
                br = new BufferedReader(new InputStreamReader(stdout));
                String line;
                while ((line = br.readLine()) != null) {
                    //System.out.println(ligne);
                    qstatOutput += line;
                }
                br.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }


            qstatOutput = qstatOutput.toUpperCase();
            // waiting for the delete command to end
            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
                    | ChannelCondition.EOF | ChannelCondition.EXIT_SIGNAL, 0);
        } catch (IOException ex) {
            System.out.println("Oppening session error :" + ex.getMessage());
        } finally {
            // clossing the second session
            session.close();
        }

        try {
            session = connexion.openSession();

            try {
                session.execCommand("echo $?");
            } catch (IOException ex) {
                System.out.println("Executing command error :" + ex.getMessage());
            }

            try {
                stdout = new StreamGobbler(session.getStdout());
                br = new BufferedReader(new InputStreamReader(stdout));
                outKey = br.readLine();
                br.close();

            } catch (Exception ex) {
                System.out.println(ex.toString());
            }

            if (!outKey.equals("0")) {
                if (outKey.equals("127")) {
                    throw new NoSuccessException("Command not found");
                } else {
                    JobState = "E";
                }
            } else {
                try {
                    Scanner s = new Scanner(qstatOutput);
                    s.findInLine(".*JOB_STATE\\s*=\\s*([CEHQSWR]).*");
                    MatchResult result = s.match();
                    JobState = result.group(1);
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        } catch (IOException ex) {
            System.out.println("Oppening session error :" + ex.getMessage());
        } finally {
            //closing the session
            session.close();
        }

        // a new Substate instance
        SubState status;
        // getting the appropriate state
        status = StatusMap.get(JobState);
        return new BatchSSHJobStatus(nativeJobId, status);

    }

    public static void initMap() {

        // the mapping between the caractere and the appropriate state.
        StatusMap.put("C", SubState.DONE);
        StatusMap.put("E", SubState.DONE);
        StatusMap.put("H", SubState.SUSPENDED_ACTIVE);
        StatusMap.put("Q", SubState.RUNNING_QUEUED);
        StatusMap.put("S", SubState.SUSPENDED_QUEUED);
        StatusMap.put("W", SubState.SUSPENDED_QUEUED);
        StatusMap.put("R", SubState.RUNNING_ACTIVE);

    }
}
