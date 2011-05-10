/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.in2p3.jsaga.adaptor.batchssh.job;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

/******************************************************
 * File:   BatchSSHMonitorAdaptor
 * Author: Taha BENYEZZA & Yassine BACHAR
 * Author: Lionel Schwarz
 * Date:   07 December 2010
 * ***************************************************/
public class BatchSSHMonitorAdaptor extends BatchSSHAdaptorAbstract implements JobMonitorAdaptor, QueryIndividualJob, 
	QueryListJob, ListableJobAdaptor, JobInfoAdaptor  {

    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
        return bj.get(0).getJobStatus();
    }

	public String[] list() throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(new String[]{});
        String[] list = new String[bj.size()];
        for (int i=0; i<list.length; i++) {
        	list[i] = bj.get(i).getId();
        }
        return list;
	}

	public JobStatus[] getStatusList(String[] nativeJobIdArray)
			throws TimeoutException, NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(nativeJobIdArray);
        JobStatus[] jb = new JobStatus[nativeJobIdArray.length];
        for (int i=0; i<jb.length; i++) {
        	jb[i] = bj.get(i).getJobStatus();
        }
        return jb;
	}

	public Integer getExitCode(String nativeJobId)
			throws NotImplementedException, NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
        return bj.get(0).getExitCode();
	}

	public Date getCreated(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
        return bj.get(0).getDateAttribute(BatchSSHJob.ATTR_CREATE_TIME);
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
        return bj.get(0).getDateAttribute(BatchSSHJob.ATTR_START_TIME);
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
        return bj.get(0).getDateAttribute(BatchSSHJob.ATTR_END_TIME);
	}

	public String[] getExecutionHosts(String nativeJobId)
			throws NotImplementedException, NoSuccessException {
        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
		return new String[]{
			bj.get(0).getAttribute(BatchSSHJob.ATTR_EXEC_HOST)
		};
	}
	
	/*********************************************************************
	 * Private methods
	 *********************************************************************/
	
    private List<BatchSSHJob> getAttributes(String nativeJobIdArray[]) throws NoSuccessException {
    	return this.getAttributes(nativeJobIdArray, null);
    }
    
    private List<BatchSSHJob> getAttributes(String nativeJobIdArray[], String[] keys) throws NoSuccessException {
        //BatchSSHJob[] bj = new BatchSSHJob[nativeJobIdArray.length];
		List<BatchSSHJob> bj = new ArrayList<BatchSSHJob>();

        Session session = null;
    	String command = "qstat -f -1 ";
    	for (String jobId: nativeJobIdArray) {
    		command += jobId + " ";
    	}
        InputStream stdout;
        BufferedReader br;
        BatchSSHJob job = null;
        int i=0;
        try {
        	session = this.sendCommand(command);
            stdout = new StreamGobbler(session.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br.readLine()) != null) {
            	line = line.trim();
            	if (line.startsWith("Job Id:")) {
                	String jobid = line.split(":")[1].trim();
                	job = new BatchSSHJob(jobid);
            	} else if (line.length() == 0) { // end of Job
            		//bj[i] = job;
            		bj.add(job);
            		i++;
            	} else { // attributes
            		String[] arr = line.split("=",2);
            		if (arr.length == 2) {
            			job.setAttribute(arr[0].trim().toUpperCase(), arr[1].trim());
            		}
            	}
            }
            br.close();

        } catch (IOException ex) {
			throw new NoSuccessException("Unable to query job status", ex);
        } catch (BatchSSHCommandFailedException e) {
			throw new NoSuccessException("Unable to query job status", e);
		} finally {
            if (session != null) session.close();
        }
		return bj;
		//return (BatchSSHJob[])bj.toArray(new BatchSSHJob[bj.size()]);

    }

}
