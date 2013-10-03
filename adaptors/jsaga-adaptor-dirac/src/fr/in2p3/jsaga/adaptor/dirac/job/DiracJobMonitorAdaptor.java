package fr.in2p3.jsaga.adaptor.dirac.job;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracRESTClient;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DiracJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
* ***************************************************/
public class DiracJobMonitorAdaptor extends DiracJobAdaptorAbstract implements
		QueryIndividualJob/*, QueryListJob, ListableJobAdaptor*/, JobInfoAdaptor {

	private static final String DIRAC_TIME = "yyyy-MM-dd HH:mm:ss";
	
	public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
		try {
			JSONObject jobInfo = this.getJob(nativeJobId);
			return new DiracJobStatus(nativeJobId, jobInfo);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

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
		throw new NotImplementedException();
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,
			NoSuccessException {
		throw new NotImplementedException();
	}

	public String[] getExecutionHosts(String nativeJobId)
			throws NotImplementedException, NoSuccessException {
		throw new NotImplementedException();
	}

	private Date getTime(String nativeJobId, String whichTime) throws	NoSuccessException, AuthenticationFailedException, 
																			IncorrectURLException, MalformedURLException {
		DateFormat df = new SimpleDateFormat(DIRAC_TIME);
		try {
			JSONObject jobTimes = (JSONObject)this.getJob(nativeJobId).get("times");
			String time = (String)jobTimes.get(whichTime);
			return df.parse(time);
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}		
	}
	
}
