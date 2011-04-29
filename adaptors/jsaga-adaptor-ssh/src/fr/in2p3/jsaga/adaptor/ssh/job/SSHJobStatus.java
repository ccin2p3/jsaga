package fr.in2p3.jsaga.adaptor.ssh.job;

import com.jcraft.jsch.Channel;
import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHJobStatus
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   11 avril 2008
* ***************************************************/

public class SSHJobStatus extends JobStatus {

	private int m_returnCode;
	public static final int PROCESS_CANCELED = 143;

    public SSHJobStatus(String jobId, Boolean isStillRunning, String state, int retCode) {
    	super(jobId, isStillRunning, state, retCode);
    }
    
	public SSHJobStatus(String jobId, int retCode) {
		this(jobId, null, "unknown", retCode);
		// retCode is not saved into m_nativeCause
		m_returnCode = retCode;
		if (retCode == SSHJobProcess.PROCESS_RUNNING) {
			this.m_nativeStateCode = true;
			this.m_nativeStateString = "Running";
		} else if (retCode >= SSHJobProcess.PROCESS_DONE_OK) {
			this.m_nativeStateCode = false;
			if (retCode == PROCESS_CANCELED) {
				this.m_nativeStateString = "Canceled";
			} else {
				this.m_nativeStateString = (retCode == SSHJobProcess.PROCESS_DONE_OK)?"Done":"Failed";
			}
		}
    }
    
    public SSHJobStatus(String jobId, Channel channel) {
    	this(jobId, channel.isConnected(), "unknown", channel.getExitStatus());
    }

	public String getModel() {
        return "ssh";
    }

    public SubState getSubState() {
        Boolean isConnected = (Boolean) m_nativeStateCode;
        if (isConnected) {
            return SubState.RUNNING_ACTIVE;
        } else if (m_nativeCause != null) {
        	if (m_returnCode == PROCESS_CANCELED) {
        		return SubState.CANCELED;
        	} else {
        		return SubState.FAILED_ERROR;
        	}
        } else {
            return SubState.DONE;
        }
    }
}
