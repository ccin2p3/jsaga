package fr.in2p3.jsaga.adaptor.dirac.job;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracRESTClient;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DiracJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
* ***************************************************/
public class DiracJobMonitorAdaptor extends DiracJobAdaptorAbstract implements
		QueryIndividualJob, QueryListJob, ListableJobAdaptor, JobInfoAdaptor {

	private static final String DIRAC_TIME = "yyyy-MM-dd HH:mm:ss z";
	
	/*----------------- QueryIndividualJob -----------------*/
	public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
		try {
			JSONObject jobInfo = this.getJob(nativeJobId);
			return new DiracJobStatus(jobInfo);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	/*----------------- JobInfoAdaptor -------------------*/
	public Integer getExitCode(String nativeJobId)
			throws NotImplementedException, NoSuccessException {
		throw new NotImplementedException();
	}

	public Date getCreated(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
		try {
			return this.getTime(nativeJobId, "submission");
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
		try {
			return this.getTime(nativeJobId, "startExecution");
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
		try {
			return this.getTime(nativeJobId, "endExecution");
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public String[] getExecutionHosts(String nativeJobId)
			throws NotImplementedException, NoSuccessException {
		throw new NotImplementedException();
	}

	/*--------------- QueryListJob ------------------*/
	public JobStatus[] getStatusList(String[] nativeJobIdArray)
			throws TimeoutException, NoSuccessException {
		// build JSONObject that contains all arguments to be passed to the GET jid=xxxx&jid=yyy...
		JSONObject statuses = new JSONObject();
		for (String jobId: nativeJobIdArray) {
			statuses.put(DiracConstants.DIRAC_GET_RETURN_JID, jobId);
		}
		try {
			// do the request: we get a JSONArray of all JSONObject representing jobs
			JSONArray jobs = this.getJobs(statuses);
			// build the Array to return
			JobStatus[] statusArray = new JobStatus[jobs.size()];
			for (int i=0; i<jobs.size(); i++) {
				statusArray[i] = new DiracJobStatus((JSONObject)jobs.get(i));
			}
			return statusArray;
		} catch (AuthenticationFailedException e) {
			throw new NoSuccessException(e);
		} catch (IncorrectURLException e) {
			throw new NoSuccessException(e);
		} catch (MalformedURLException e) {
			throw new NoSuccessException(e);
		}
	}

	/*------------- ListableJobAdaptor ----------------*/
	public String[] list() throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		try {
			// do the request: we get a JSONArray of all JSONObject representing jobs
			JSONArray jobs = this.getJobs();
			// build the Array to return
			String[] jobIdArray = new String[jobs.size()];
			for (int i=0; i<jobs.size(); i++) {
				JSONObject job = (JSONObject)jobs.get(i);
				jobIdArray[i] = ((Long)job.get(DiracConstants.DIRAC_GET_RETURN_JID)).toString();
			}
			return jobIdArray;
		} catch (AuthenticationFailedException e) {
			throw new NoSuccessException(e);
		} catch (IncorrectURLException e) {
			throw new NoSuccessException(e);
		} catch (MalformedURLException e) {
			throw new NoSuccessException(e);
		}
	}
	
	/*------------- Private methods ------------------*/
	private Date getTime(String nativeJobId, String whichTime) throws	NoSuccessException, AuthenticationFailedException, 
															IncorrectURLException, MalformedURLException {
		DateFormat df = new SimpleDateFormat(DIRAC_TIME);
		try {
			JSONObject jobTimes = (JSONObject)this.getJob(nativeJobId).get("times");
			String time = (String)jobTimes.get(whichTime);
			// add UTC because Dirac times are in UTC
			time += " UTC";
			return df.parse(time);
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}		
	}

}
