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
@Deprecated
public class LocalJobIOHandler /*implements JobIOGetterInteractive*/ {

	private LocalJobProcess m_ljp;

	public LocalJobIOHandler(LocalJobProcess p) {
		this.m_ljp = p;
	}

	public String getJobId() {
		return m_ljp.getJobId();
	}

//	public InputStream getStderr() throws PermissionDeniedException, TimeoutException, NoSuccessException {
//		try {
//			return new FileInputStream(new File(m_ljp.getErrfile()));
//		} catch (FileNotFoundException e) {
//			throw new NoSuccessException(e);
//		}
//	}
//
//	public OutputStream getStdin() throws PermissionDeniedException, TimeoutException, NoSuccessException {
//		try {
//			return new FileOutputStream(new File(m_ljp.getInfile()));
//		} catch (FileNotFoundException e) {
//			throw new NoSuccessException(e);
//		}
//	}
//
//	public InputStream getStdout() throws PermissionDeniedException, TimeoutException, NoSuccessException {
//		try {
//			return new FileInputStream(new File(m_ljp.getOutfile()));
//		} catch (FileNotFoundException e) {
//			throw new NoSuccessException(e);
//		}
//	}
}