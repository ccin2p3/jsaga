package fr.in2p3.jsaga.adaptor.cream.job;

//import eu.emi.security.canl.axis2.CANLAXIS2SocketFactory;
import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.Command;
import org.glite.ce.creamapi.ws.cream2.Authorization_Fault;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobFilter;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobId;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfo;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfoRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobInfoResult;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.Status;
import org.glite.ce.creamapi.ws.cream2.Generic_Fault;
import org.glite.ce.creamapi.ws.cream2.InvalidArgument_Fault;
import org.ogf.saga.error.*;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamJobMonitorAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamJobMonitorAdaptor extends CreamJobAdaptorAbstract implements QueryListJob, ListableJobAdaptor, JobInfoAdaptor {
	
    public JobStatus[] getStatusList(String[] nativeJobIdArray) throws TimeoutException, NoSuccessException {

        JobInfo[] resultArray;
		resultArray = getJobInfoResult(nativeJobIdArray);
        // convert
        JobStatus[] jsArray = new JobStatus[resultArray.length];
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            Status[] statusArray = resultArray[i].getStatus();
            if (statusArray==null || statusArray.length==0) {
                throw new NoSuccessException("Empty status for job: "+resultArray[i].getJobId().getId());
            }
            Status lastStatus = statusArray[statusArray.length - 1];
            
            // convert last status
            if (lastStatus.getFailureReason()!=null) {
                jsArray[i] = new CreamJobStatus(lastStatus, lastStatus.getFailureReason());
            } else if (lastStatus.getExitCode()!=null && !lastStatus.getExitCode().equals("N/A")) {
                jsArray[i] = new CreamJobStatus(lastStatus, Integer.parseInt(lastStatus.getExitCode()));
            } else {
                jsArray[i] = new CreamJobStatus(lastStatus);
            }
        	
        }
        return jsArray;
    }

    public String[] list() throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobId[] resultArray;
        try {
            resultArray = m_client.jobList();
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        } catch (Authorization_Fault e) {
			throw new PermissionDeniedException(e);
		} catch (Generic_Fault e) {
			throw new NoSuccessException(e);
		}
        if (resultArray != null) {
            String[] jobIds = new String[resultArray.length];
            for (int i=0; i<resultArray.length; i++) {
                jobIds[i] = resultArray[i].getId();
            }
            return jobIds;
        } else {
            return new String[]{};
        }
    }

	public Integer getExitCode(String nativeJobId)	throws NotImplementedException, NoSuccessException {
        try {
        	Status[] stat = this.getJobInfoResult(new String[]{nativeJobId})[0].getStatus();
			return new Integer(stat[stat.length-1].getExitCode());
		} catch (TimeoutException e) {
			throw new NoSuccessException(e);
		} catch (NumberFormatException nfe) {
			// Not a number
			return null;
		}
	}

	public Date getCreated(String nativeJobId) throws NotImplementedException,	NoSuccessException {
        try {
    		return getStatus(nativeJobId, new String[]{CreamJobStatus.REGISTERED})
    				.getTimestamp()
    				.getTime();
		} catch (TimeoutException e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,	NoSuccessException {
        try {
    		return getStatus(nativeJobId, new String[]{CreamJobStatus.REALLY_RUNNING})
    				.getTimestamp()
    				.getTime();
		} catch (TimeoutException e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException, NoSuccessException {
        try {
    		return getStatus(nativeJobId, new String[]{CreamJobStatus.DONE_OK, CreamJobStatus.DONE_FAILED, CreamJobStatus.CANCELLED})
    				.getTimestamp()
    				.getTime();
		} catch (TimeoutException e) {
			throw new NoSuccessException(e);
		}
	}

	public String[] getExecutionHosts(String nativeJobId) throws NotImplementedException, NoSuccessException {
        try {
			return new String[]{this.getJobInfoResult(new String[]{nativeJobId})[0].getWorkerNode()};
		} catch (TimeoutException e) {
			throw new NoSuccessException(e);
		}
	}

	/*-----------------*/
	/* Private methods */
	/*-----------------*/	
	private CreamJobStatus getStatus(String nativeJobId, String[] requestedStatuses) throws NoSuccessException, TimeoutException {
    	Status[] stats = this.getJobInfoResult(new String[]{nativeJobId})[0].getStatus();
		for (Status stat: stats) {
			for (String requestedStatus: requestedStatuses) {
				if (stat.getName().equals(requestedStatus)) {
					return new CreamJobStatus(stat);
				}
			}
		}
		throw new NoSuccessException("Status not available");
	}
	
    private JobInfo[] getJobInfoResult(String[] nativeJobIdArray) throws TimeoutException, NoSuccessException {
        JobInfoResult[] resultArray;
        try {
            resultArray = m_client.jobInfo(nativeJobIdArray);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        } catch (Authorization_Fault e) {
        	throw new NoSuccessException(new PermissionDeniedException(e));
		} catch (Generic_Fault e) {
        	throw new NoSuccessException(e);
		} catch (InvalidArgument_Fault e) {
        	throw new NoSuccessException(e);
		}
        JobInfo[] infos = new JobInfo[resultArray.length];
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            // extract  job info
            JobInfo info = resultArray[i].getJobInfo();
            if (info == null) {
                throw new NoSuccessException("Empty info for job: "+nativeJobIdArray[i]);
            }
            infos[i] = info;
        }
        return infos;
    }

}
