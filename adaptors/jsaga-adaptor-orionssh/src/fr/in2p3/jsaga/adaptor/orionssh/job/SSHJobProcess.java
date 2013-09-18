package fr.in2p3.jsaga.adaptor.orionssh.job;

import java.io.IOException;
import org.ogf.saga.error.NoSuccessException;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.local.LocalJobProcess;
import fr.in2p3.jsaga.adaptor.orionssh.data.SFTPDataAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHJobProcess
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   16 juillet 2013
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

	public SSHJobProcess(String jobId) {
		super(jobId);
	}
	
    public static String getRootDir() {
    	return ".jsaga/var/adaptor/ssh";
    }

    @Override
    public String getGeneratedWorkingDirectory() {
		return SSHJobProcess.getRootDir() + "/" + m_jobId;
    }

    @Override
    public String getWorkingDirectory() throws IOException {
    	if (isUserWorkingDirectory()) {
			return getValue("_WorkingDirectory");
		} else {
			return m_home + "/" + getRootDir() + "/" + m_jobId;
		}
    }
    
    // Cannot create working directory here
    @Override
	public void createWorkingDirectory() throws IOException {
	}
    
    @Override
	public int getReturnCode() throws NoSuccessException {
		return m_returnCode;
	}

    @Override
	public void checkResources() throws BadResource, NoSuccessException {
	}

    @Override
	protected String toURL(String filename) throws NoSuccessException {
		try {
			return SFTPDataAdaptor.TYPE + "://" + m_host + ":" + m_port + "/" + getWorkingDirectory() + "/" + filename;
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
	}
	
	
}
