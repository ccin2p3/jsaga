package fr.in2p3.jsaga.adaptor.ssh3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.ServerHostKeyVerifier;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 juillet 2013
* ***************************************************/

public class SSHHostKeyVerifier implements ServerHostKeyVerifier {

	private KnownHosts m_knownHosts = null;
	
	public SSHHostKeyVerifier(File knownHostsFile) throws IOException {
        if (knownHostsFile.exists())
        {
        	m_knownHosts = new KnownHosts();
        	m_knownHosts.addHostkeys(knownHostsFile);
        } else {
        	throw new FileNotFoundException(knownHostsFile.toString());
        }
	}
	
	public boolean verifyServerHostKey(String hostname, int port,
			String serverHostKeyAlgorithm, byte[] serverHostKey)
			throws Exception {
        int result = m_knownHosts.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);

        switch (result) {
        	case KnownHosts.HOSTKEY_IS_OK:
        		return true;

        	case KnownHosts.HOSTKEY_IS_NEW:
        		return false;

        	case KnownHosts.HOSTKEY_HAS_CHANGED:
        		return false;

        	default:
        		throw new IllegalStateException();
        }
	}

}
