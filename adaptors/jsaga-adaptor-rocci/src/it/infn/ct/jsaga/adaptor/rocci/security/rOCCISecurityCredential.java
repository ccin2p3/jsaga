package it.infn.ct.jsaga.adaptor.rocci.security;

import java.io.PrintStream;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.VOMSSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   rOCCISecurityCredential
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   22 oct 2013
* ***************************************************/

public class rOCCISecurityCredential implements SecurityCredential {

	private SSHSecurityCredential m_sshCred;
	private VOMSSecurityCredential m_proxy;
	
	public 	rOCCISecurityCredential(VOMSSecurityCredential p, SSHSecurityCredential s) {
		this.m_sshCred = s;
		this.m_proxy = p;
	}
	
	public String getUserID() throws Exception {
		return m_proxy.getUserID();
	}

	public String getAttribute(String key) throws NotImplementedException,
			NoSuccessException {
		return m_proxy.getAttribute(key);
	}

	public void close() throws Exception {
		m_proxy.close();
		m_sshCred.close();
	}

	public void dump(PrintStream out) throws Exception {
		out.println("VOMS Proxy to access Cloud");
		m_proxy.dump(out);
		out.flush();
		out.println("SSH Credential to access VM");
		m_sshCred.dump(out);
		out.flush();
	}

	public SSHSecurityCredential getSSHCredential() {
		return this.m_sshCred;
	}
	public VOMSSecurityCredential getProxy() {
		return this.m_proxy;
	}
}
