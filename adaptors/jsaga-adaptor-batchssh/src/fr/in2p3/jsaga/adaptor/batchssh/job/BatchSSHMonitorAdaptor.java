package fr.in2p3.jsaga.adaptor.batchssh.job;

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
import java.util.Date;
import java.util.List;

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
		
}
