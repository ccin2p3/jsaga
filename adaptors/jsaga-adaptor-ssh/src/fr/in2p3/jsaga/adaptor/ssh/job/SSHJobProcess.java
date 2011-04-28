package fr.in2p3.jsaga.adaptor.ssh.job;

import org.ogf.saga.error.NoSuccessException;

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
	private static final long serialVersionUID = 3723657591636633186L;
	
	public SSHJobProcess(String jobId) {
		super(jobId);
	}

    public static String getRootDir() {
    	return ".jsaga/var/adaptor/ssh";
    }

	public int getReturnCode() throws NoSuccessException {
		return m_returnCode;
	}
}
