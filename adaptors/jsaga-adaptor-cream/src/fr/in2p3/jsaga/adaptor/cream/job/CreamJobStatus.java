package fr.in2p3.jsaga.adaptor.cream.job;

import java.util.Calendar;

import org.glite.ce.creamapi.ws.cream2.CREAMStub.Status;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamJobStatus
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 dec. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamJobStatus extends JobStatus {
    public static final String REGISTERED = "REGISTERED";  //the job has been registered (but not started yet)
    public static final String PENDING = "PENDING";        //the job has been started, but it has still to be submitted to BLAH
    public static final String IDLE = "IDLE";              //the job is idle in the Local Resource Management System (LRMS)
    public static final String RUNNING = "RUNNING";        //the job wrapper which "encompasses" the user job is running in the LRMS.
    public static final String REALLY_RUNNING = "REALLY-RUNNING";  //the actual user job (the one specified as Executable in the job JDL) is running in the LRMS
    public static final String HELD = "HELD";              //the job is held (suspended) in the LRMS
    public static final String CANCELLED = "CANCELLED";    //the job has been cancelled
    public static final String DONE_OK = "DONE-OK";        //the job has successfully been executed
    public static final String DONE_FAILED = "DONE-FAILED";//the job has been executed, but some errors occurred
    public static final String ABORTED = "ABORTED";        //errors occurred during the ``management'' of the job, e.g. the submission to the LRMS abstraction layer software (BLAH) failed.
    //private static final String UNKNOWN = "UNKNOWN";        //the job is an unknown status

    private Calendar m_timestamp;
    
    public CreamJobStatus(Status status, int returnCode) {
        super(status.getJobId().getId(), status.getName(), status.getName(), returnCode);
        m_timestamp = status.getTimestamp();
    }

    public CreamJobStatus(Status status, String cause) {
        super(status.getJobId().getId(), status.getName(), status.getName(), cause);
        m_timestamp = status.getTimestamp();
    }

    public CreamJobStatus(Status status) {
        super(status.getJobId().getId(), status.getName(), status.getName());
        m_timestamp = status.getTimestamp();
    }

    public String getModel() {
        return "cream";
    }

    public String getStatus() {
    	return (String) m_nativeStateCode;
    }
    
    public SubState getSubState() {
        String name = (String) m_nativeStateCode;
        if (PENDING.equals(name) || REGISTERED.equals(name)) {
            return SubState.RUNNING_SUBMITTED;
        } else if (IDLE.equals(name) || RUNNING.equals(name)) {
            return SubState.RUNNING_QUEUED;
        } else if (REALLY_RUNNING.equals(name)) {
            return SubState.RUNNING_ACTIVE;
        } else if (HELD.equals(name)) {
            return SubState.SUSPENDED_ACTIVE;
        } else if (CANCELLED.equals(name)) {
            return SubState.CANCELED;
        } else if (DONE_OK.equals(name)) {
            return SubState.DONE;
        } else if (DONE_FAILED.equals(name)) {
            return SubState.FAILED_ERROR;
        } else if (ABORTED.equals(name)) {
            return SubState.FAILED_ABORTED;
        } else {
            return null;
        }
    }
    
    public Calendar getTimestamp() {
    	return m_timestamp;
    }
}
