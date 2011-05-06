/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.in2p3.jsaga.adaptor.batchssh.job;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

/******************************************************
 * File:   BatchSSHMonitorAdaptor
 * Author: Taha BENYEZZA & Yassine BACHAR
 * Date:   07 December 2010
 * ***************************************************/
public class BatchSSHMonitorAdaptor extends BatchSSHAdaptorAbstract implements JobMonitorAdaptor, QueryIndividualJob, ListableJobAdaptor {

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        Session session = null;
        String StatusCommand = "qstat -f " + nativeJobId;
        InputStream stdout;
        BufferedReader br;
        String qstatOutput = "";
        String exit_code = null;
        String job_state = null;
        try {
        	session = this.sendCommand(StatusCommand);
            stdout = new StreamGobbler(session.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br.readLine()) != null) {
                qstatOutput += line;
            }
            br.close();

            qstatOutput = qstatOutput.toUpperCase();
        } catch (IOException ex) {
			throw new NoSuccessException("Unable to query job status", ex);
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

	public String[] list() throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
        Session session = null;
        InputStream stdout;
        BufferedReader br;
		List<String> urls = new ArrayList<String>();
        try {
        	// there is no qstat option to get non-truncated hostname !!!
        	session = this.sendCommand("qstat -f -1 | grep 'Job Id:'");
            stdout = new StreamGobbler(session.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br.readLine()) != null) {
            	String jobid = line.split(":")[1].trim();
    			urls.add(jobid);
            }
            br.close();

        } catch (IOException ex) {
			throw new NoSuccessException("Unable to query job list", ex);
        } finally {
            session.close();
        }
		return (String[])urls.toArray(new String[urls.size()]);
	}
}
