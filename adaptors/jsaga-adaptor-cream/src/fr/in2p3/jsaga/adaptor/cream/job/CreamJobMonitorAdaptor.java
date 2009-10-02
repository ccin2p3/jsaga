package fr.in2p3.jsaga.adaptor.cream.job;

import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryListJob;
import org.apache.axis.types.URI;
import org.glite.ce.creamapi.ws.cream2.CREAMPort;
import org.glite.ce.creamapi.ws.cream2.types.*;
import org.ogf.saga.error.*;

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
public class CreamJobMonitorAdaptor extends CreamJobAdaptorAbstract implements QueryListJob, ListableJobAdaptor {
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
            try {
                if (resultArray[i].getDateMismatchFault()!=null) {
                    throw resultArray[i].getDateMismatchFault();
                } else if (resultArray[i].getDelegationIdMismatchFault()!=null) {
                    throw resultArray[i].getDelegationIdMismatchFault();
                } else if (resultArray[i].getGenericFault()!=null) {
                    throw resultArray[i].getGenericFault();
                } else if (resultArray[i].getJobStatusInvalidFault()!=null) {
                    throw resultArray[i].getJobStatusInvalidFault();
                } else if (resultArray[i].getJobUnknownFault()!=null) {
                    throw resultArray[i].getJobUnknownFault();
                } else if (resultArray[i].getLeaseIdMismatchFault()!=null) {
                    throw resultArray[i].getLeaseIdMismatchFault();
                }
            } catch (DelegationIdMismatchFault fault) {
                throw new NoSuccessException(new PermissionDeniedException(getMessage(fault), fault));
            } catch (BaseFaultType fault) {
                throw new NoSuccessException(getMessage(fault), fault);
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
    private static String getMessage(BaseFaultType fault) {
        if (fault.getDescription()!=null && !fault.getDescription().equals("")) {
            return fault.getDescription();
        } else if (fault.getFaultCause()!=null && !fault.getFaultCause().equals("N/A")) {
            return fault.getFaultCause();
        } else {
            return fault.getClass().getName();
        }
    }

    public String[] list() throws PermissionDeniedException, TimeoutException, NoSuccessException {
        JobId[] resultArray;
        try {
            resultArray = m_creamStub.getStub().jobList();
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }
        if (resultArray != null) {
            String[] jobIds = new String[resultArray.length];
            for (int i=0; i<resultArray.length; i++) {
                jobIds[i] = resultArray[i].getId();
            }
            return jobIds;
        } else {
            throw new NoSuccessException("Failed to list jobs");
        }
    }
}
