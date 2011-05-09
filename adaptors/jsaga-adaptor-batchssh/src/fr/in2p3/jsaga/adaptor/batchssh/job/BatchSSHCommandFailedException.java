package fr.in2p3.jsaga.adaptor.batchssh.job;

public class BatchSSHCommandFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static int PBS_E_UNKNOWN_JOBID = 153;
	public static int PBS_E_UNKNOWN_QUEUE = 170;
	public static int PBS_QHOLD_E_JOB_INVALID_STATE = 168;
	public static int PBS_QSUB_E_EXCEED_QUEUE_LIMITS = 188;
	
	private int m_errno;
	private String m_errmsg;
	private String m_command;
	
	public BatchSSHCommandFailedException(String command, int errno, String msg) {
		m_command = command;
		m_errno = errno;
		m_errmsg = msg;
	}
	
	public String getMessage() {
		return "Command '" + m_command + "' returned " + m_errno + " (" + m_errmsg + ")";
	}
	
	public int getErrno() {
		return m_errno;
	}
	
	public boolean isErrorTypeOfBadResource() {
		return (m_errno == PBS_QSUB_E_EXCEED_QUEUE_LIMITS);
	}
}
