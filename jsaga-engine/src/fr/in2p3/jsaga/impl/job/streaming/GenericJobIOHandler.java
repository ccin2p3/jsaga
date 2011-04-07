package fr.in2p3.jsaga.impl.job.streaming;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;

public class GenericJobIOHandler implements JobIOGetterInteractive {

	private String m_jobId;
	private String m_uniqId;
	
	public GenericJobIOHandler(String jobId, String uniqId) {
		m_jobId = jobId;
		m_uniqId = uniqId;
	}
	
	public String getJobId() {
        return m_jobId;
	}

	public InputStream getStdout() throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		try {
			return new FileInputStream(LocalFileFactory.getLocalOutputFile(m_uniqId));
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	public InputStream getStderr() throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		try {
			return new FileInputStream(LocalFileFactory.getLocalErrorFile(m_uniqId));
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	public OutputStream getStdin() throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		try {
			return new FileOutputStream(LocalFileFactory.getLocalInputFile(m_uniqId));
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

}
