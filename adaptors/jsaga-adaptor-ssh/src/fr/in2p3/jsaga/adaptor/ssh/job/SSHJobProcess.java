package fr.in2p3.jsaga.adaptor.ssh.job;

import java.util.Date;

import org.ogf.saga.error.NoSuccessException;

import com.jcraft.jsch.Channel;

import fr.in2p3.jsaga.adaptor.job.local.LocalJobProcess;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

public class SSHJobProcess extends LocalJobProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3723657591636633186L;

	private Channel m_channel;
	
	public SSHJobProcess(String jobId, Channel channel) {
		super(jobId);
		m_channel = channel;
	}

	// TODO: getPid
	public String getPid() throws NoSuccessException {
    	return null;
    }
	
	//TODO: getReturnCode
	public int getReturnCode() throws NoSuccessException {
		return -1;
	}
	
	public JobStatus getJobStatus() throws NoSuccessException {
		int status = getReturnCode();
		if (status <0) { // either running or suspended
			status = getProcessStatus();
		}
		return new SSHJobStatus(m_jobId, m_channel);						
	}
	
	// TODO: getProcessStatus()
	public int getProcessStatus() throws NoSuccessException {
		return -1;
	}

    /*
    public void kill() {
    	this.kill(LocalJobControlAdaptor.SIGNAL_TERM);
    }
    
    // TODO sent kill via SSH
    public void kill(int signum) {
    	
    }
    */
}
