package fr.in2p3.jsaga.adaptor.batchssh.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;

public class BatchSSHJobIOHandler implements JobIOGetterInteractive {

	private Session m_session;
	private String m_jobId;
	
	public BatchSSHJobIOHandler(Session session) {
		m_session = session;
		m_jobId = null;
	}
	
	public InputStream getStdout() throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		return m_session.getStdout();
	}

	public InputStream getStderr() throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		return m_session.getStderr();
	}

	public String getJobId() {
		if (m_jobId != null) return m_jobId;
        InputStream stdout;
        BufferedReader br;
        // Retrieving the standard output
        stdout = new StreamGobbler(m_session.getStdout());
        br = new BufferedReader(new InputStreamReader(stdout));
        
        try {
			m_jobId = br.readLine();
		} catch (IOException e) {
			// what to do ???
		}
        return m_jobId;
	}

	public OutputStream getStdin() throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		return m_session.getStdin();
	}
	

}
