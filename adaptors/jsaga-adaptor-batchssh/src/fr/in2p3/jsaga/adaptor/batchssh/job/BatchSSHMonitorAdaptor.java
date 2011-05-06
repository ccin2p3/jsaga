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

	// TODO: implement listableJobAdaptor
	
    //private static Map<String, SubState> StatusMap = new HashMap<String, SubState>();

	// TODO: move this to Abstract
    public String getType() {
        return "pbs-ssh";
    }

	// TODO: move this to Abstract
    public int getDefaultPort() {
        return 22;
    }

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        // initialisinf the StatusMap
        //initMap();
        //String JobState = null;
        Session session = null;
        // the qstat Command
        // TODO : set back to "qstat" and change path on server side
        String StatusCommand = "qstat -f " + nativeJobId;
        InputStream stdout;
        BufferedReader br;
        String qstatOutput = "";
        //String outKey = "";
        String exit_code = null;
        String job_state = null;
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
            Scanner s = new Scanner(qstatOutput);
	        s.findInLine(".*JOB_STATE\\s*=\\s*([CEHQSWR]).*");
	        MatchResult result = s.match();
	        job_state = result.group(1);
        } catch (IllegalStateException ise) {
        	throw new NoSuccessException("Unable to get status", ise);
        }
        try {
            Scanner s = new Scanner(qstatOutput);
	        s.findInLine(".*EXIT_STATUS\\s*=\\s*(\\d+).*");
	        MatchResult result = s.match();
	        exit_code = result.group(1);
            return new BatchSSHJobStatus(nativeJobId, job_state, new Integer(exit_code).intValue());
        } catch (IllegalStateException ise) {
            return new BatchSSHJobStatus(nativeJobId, job_state);
        }
        

    }

}
