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
    public SSHJobStatus(String jobId, Channel channel) {
        super(jobId, channel.isConnected(), "unknown", channel.getExitStatus());
    }

	public String getModel() {
        return "ssh";
    }

    public SubState getSubState() {
        Boolean isConnected = (Boolean) m_nativeStateCode;
        if (isConnected) {
            return SubState.RUNNING_ACTIVE;
        } else if (m_nativeCause != null) {
            return SubState.FAILED_ERROR;
        } else {
            return SubState.DONE;
        }
    }
}
