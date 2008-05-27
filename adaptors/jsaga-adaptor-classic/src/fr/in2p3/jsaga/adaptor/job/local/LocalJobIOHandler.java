package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

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

	private Process p;
	private String jobId;

	public LocalJobIOHandler(Process p, String jobId) {
		this.p = p;
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess {
		return p.getErrorStream();
	}

	public OutputStream getStdin() throws PermissionDenied, Timeout, NoSuccess {
		return p.getOutputStream();
	}

	public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess {
		return p.getInputStream();
	}
}