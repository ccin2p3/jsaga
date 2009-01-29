package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;
import org.apache.axis.types.URI;
import org.glite.ce.creamapi.ws.cream2.CREAMPort;
import org.glite.ce.creamapi.ws.cream2.types.*;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import java.rmi.RemoteException;

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
public class CreamJobMonitorAdaptor extends CreamJobAdaptorAbstract implements QueryListJob {
//        , QueryFilteredJob {
    public JobStatus[] getStatusList(String[] nativeJobIdArray) throws TimeoutException, NoSuccessException {
        URI creamUri = m_creamStub.getURI();
        JobId[] jobIdList = new JobId[nativeJobIdArray.length];
        for (int i = 0; i < nativeJobIdArray.length; i++) {
            jobIdList[i] = new JobId();
            jobIdList[i].setCreamURL(creamUri);
            jobIdList[i].setId(nativeJobIdArray[i]);
        }
        JobFilter filter = new JobFilter();
        filter.setDelegationId(m_delegationId);
        filter.setJobId(jobIdList);
        return this.getStatus(filter);
    }

    public JobStatus[] getFilteredStatus(Object[] filters) throws TimeoutException, NoSuccessException {
        JobFilter filter = new JobFilter();
        filter.setDelegationId(m_delegationId);
        //todo: implement method getFilteredStatus()
//        filter.setFromDate();
//        filter.setToDate();
        return this.getStatus(filter);
    }

    private JobStatus[] getStatus(JobFilter filter) throws TimeoutException, NoSuccessException {
        // get status
        CREAMPort stub = m_creamStub.getStub();
        JobInfoResult[] resultArray;
        try {
            resultArray = stub.jobInfo(filter);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }

        // convert
        JobStatus[] jsArray = new JobStatus[resultArray.length];
        for (int i=0; resultArray!=null && i<resultArray.length; i++) {
            // rethrow exception
            BaseFaultType fault = null;
            if (resultArray[i].getDateMismatchFault() != null) {
                fault = resultArray[i].getDateMismatchFault();
            } else if (resultArray[i].getDelegationIdMismatchFault() != null) {
                fault = resultArray[i].getDelegationIdMismatchFault();
            } else if (resultArray[i].getGenericFault() != null) {
                fault = resultArray[i].getGenericFault();
            } else if (resultArray[i].getJobStatusInvalidFault() != null) {
                fault = resultArray[i].getJobStatusInvalidFault();
            } else if (resultArray[i].getJobUnknownFault() != null) {
                fault = resultArray[i].getJobUnknownFault();
            } else if (resultArray[i].getLeaseIdMismatchFault() != null) {
                fault = resultArray[i].getLeaseIdMismatchFault();
            }
            if (fault != null) {
                String message = fault.getFaultCause()!=null && !fault.getFaultCause().equals("N/A")
                        ? fault.getFaultCause()
                        : fault.getClass().getName();
                throw new NoSuccessException(message, fault);
            }

            // extract last status from job info
            JobInfo info = resultArray[i].getJobInfo();
            if (info == null) {
                throw new NoSuccessException("Empty info for job: "+filter.getJobId(i));
            }
            Status[] statusArray = info.getStatus();
            if (statusArray==null || statusArray.length==0) {
                throw new NoSuccessException("Empty status for job: "+info.getJobId().getId());
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
}
