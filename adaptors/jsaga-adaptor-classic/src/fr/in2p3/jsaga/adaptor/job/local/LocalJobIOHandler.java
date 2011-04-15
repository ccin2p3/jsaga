package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalJobIOHandler
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   29 avril 2008
* ***************************************************/
public class LocalJobIOHandler implements JobIOGetterInteractive {

	//private Process p;
	private String jobId;

	public LocalJobIOHandler(String jobId) {
		//this.p = p;
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public InputStream getStderr() throws PermissionDeniedException, TimeoutException, NoSuccessException {
		//return p.getErrorStream();
		try {
			return new FileInputStream(new File(/*LocalAdaptorAbstract.getStderrFile(jobId)*/""));
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	public OutputStream getStdin() throws PermissionDeniedException, TimeoutException, NoSuccessException {
		//return p.getOutputStream();
		try {
			return new FileOutputStream(new File(/*LocalAdaptorAbstract.getStdinFile(jobId)*/""));
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}

	public InputStream getStdout() throws PermissionDeniedException, TimeoutException, NoSuccessException {
		//return p.getInputStream();
		try {
			return new FileInputStream(new File(/*LocalAdaptorAbstract.getStdoutFile(jobId)*/""));
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		}
	}
}