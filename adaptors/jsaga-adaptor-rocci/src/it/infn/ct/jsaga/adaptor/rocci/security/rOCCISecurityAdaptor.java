package it.infn.ct.jsaga.adaptor.rocci.security;

import java.util.Map;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.orionssh.security.SSHSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.VOMSSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.VOMSSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential;

public class rOCCISecurityAdaptor extends VOMSSecurityAdaptor {

//	private SSHSecurityAdaptor m_sshAdaptor;
	
//	public rOCCISecurityAdaptor() {
//		this.m_sshAdaptor = new SSHSecurityAdaptor();
//	}
	public String getType() {
		return "rocci";
	}

	public Usage getUsage() {
		return new UAnd(
			new Usage[]{
					super.getUsage(),
					new SSHSecurityAdaptor().getUsage()}
			);
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
		// TODO Auto-generated method stub
		return null;
	}

	public Class getSecurityCredentialClass() {
		return rOCCISecurityCredential.class;
	}

	public SecurityCredential createSecurityCredential(int usage,
			Map attributes, String contextId) throws IncorrectStateException,
			TimeoutException, NoSuccessException {
		VOMSSecurityCredential proxy = (VOMSSecurityCredential)super.createSecurityCredential(usage, attributes, contextId);
		SSHSecurityCredential sshCred = (SSHSecurityCredential)new SSHSecurityAdaptor().createSecurityCredential(usage, attributes, contextId);
		return new rOCCISecurityCredential(proxy, sshCred);
	}

}
