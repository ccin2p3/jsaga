package fr.in2p3.jsaga.adaptor.ssh.job;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.ogf.saga.error.NoSuccessException;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.local.LocalJobProcess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHJobProcess
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   22 avril 2011
* ***************************************************/

public class SSHJobProcess extends LocalJobProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private String m_host;
	private int m_port;
	private String m_home;
	
	public SSHJobProcess(String jobId, String jobDesc, String host, int port, String home) {
		super(jobId, jobDesc);
		m_host = host;
		m_port = port;
		m_home = home;
	}

    public static String getRootDir() {
    	return ".jsaga/var/adaptor/ssh";
    }

	public int getReturnCode() throws NoSuccessException {
		return m_returnCode;
	}

    public String getFile(String suffix) {
    	return getRootDir() + "/" + m_jobId + "." + suffix;
    }

    @Override
	public void checkResources() throws BadResource, NoSuccessException {
    	// TODO: check if working directory exists and is accessible
		try {
			String wd = getValue("_WorkingDirectory");
			if (wd.equals("/dummy")) {
				throw new BadResource("Directory does not exist:");
			}
		} catch (NoSuchElementException e) {
			// ignore
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
	}


	protected String toURL(String filename) {
//		if (filename.startsWith("file:")) {
//			return filename;
//		} else {
			return "sftp://" + m_host + ":" + m_port + "/" + m_home + "/" + getRootDir() + "/" + filename;
//		}
	}
}
