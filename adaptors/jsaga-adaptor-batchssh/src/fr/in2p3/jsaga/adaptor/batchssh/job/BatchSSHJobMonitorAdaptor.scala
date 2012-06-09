package fr.in2p3.jsaga.adaptor.batchssh.job

import fr.in2p3.jsaga.adaptor.job.monitor.{JobMonitorAdaptor, QueryIndividualJob, QueryListJob, JobInfoAdaptor}
import org.ogf.saga.error.NoSuccessException

class BatchSSHMonitorAdaptor extends BatchSSHAdaptorAbstract
//with JobMonitorAdaptor
                                with QueryIndividualJob
//with QueryListJob
//with ListableJobAdaptor
/*with JobInfoAdaptor*/  {

  def getStatus(nativeJobId: String) = withConnection {
    connection =>
    val ssh = connection.openSession
    try BatchSSHJob.status(nativeJobId, ssh)
    finally ssh.close
  }
  
  /*def list = allJobs.map{_.id}.toArray

   def getSatusList =
   allJobs.map{_.jobStatus}.toArray

   def getSatusList(nativeJobId: Array[String]) =
   jobs(nativeJobId).map{_.jobStatus}.toArray*/
  //def getExitCode(nativeJobId: String) = jobs(Seq(nativeJobId)).head.
}
//
//	 QueryIndividualJob
//    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
//        return bj.get(0).getJobStatus();
//    }
//
//     ListableJobAdaptor
//	public String[] list() throws PermissionDeniedException, TimeoutException,
//			NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(new String[]{});
//        String[] list = new String[bj.size()];
//        for (int i=0; i<list.length; i++) {
//        	list[i] = bj.get(i).getId();
//        }
//        return list;
//	}
//
//	 QueryListJob
//	public JobStatus[] getStatusList(String[] nativeJobIdArray)
//			throws TimeoutException, NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(nativeJobIdArray);
//        JobStatus[] jb = new JobStatus[nativeJobIdArray.length];
//        for (int i=0; i<jb.length; i++) {
//        	jb[i] = bj.get(i).getJobStatus();
//        }
//        return jb;
//	}
//
//	 JobInfoAdaptor
//	public Integer getExitCode(String nativeJobId)
//			throws NotImplementedException, NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
//        return bj.get(0).getExitCode();
//	}
//
//	public Date getCreated(String nativeJobId) throws NotImplementedException,
//			NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
//        return bj.get(0).getDateAttribute(BatchSSHJob.ATTR_CREATE_TIME);
//	}
//
//	public Date getStarted(String nativeJobId) throws NotImplementedException,
//			NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
//        return bj.get(0).getDateAttribute(BatchSSHJob.ATTR_START_TIME);
//	}
//
//	public Date getFinished(String nativeJobId) throws NotImplementedException,
//			NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
//        return bj.get(0).getDateAttribute(BatchSSHJob.ATTR_END_TIME);
//	}
//
//	public String[] getExecutionHosts(String nativeJobId)
//			throws NotImplementedException, NoSuccessException {
//        List<BatchSSHJob> bj = this.getAttributes(new String[]{nativeJobId});
//		return new String[]{
//			bj.get(0).getAttribute(BatchSSHJob.ATTR_EXEC_HOST)
//		};
//	}
//
//}

