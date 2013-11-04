package it.infn.ct.jsaga.adaptor.rocci.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.orionssh.security.SSHSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.VOMSSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.VOMSSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   rOCCISecurityAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   22 oct 2013
* ***************************************************/

public class rOCCISecurityAdaptor extends VOMSSecurityAdaptor {

	private SSHSecurityAdaptor m_sshAdaptor;
	
	public rOCCISecurityAdaptor() {
		super();
		m_sshAdaptor = new SSHSecurityAdaptor();
	}
	
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
		Default[] vomsDefault = super.getDefaults(attributes);
		Default[] sshDefault = m_sshAdaptor.getDefaults(attributes);
		List<Default> both = new ArrayList<Default>(vomsDefault.length+sshDefault.length);
		Collections.addAll(both, vomsDefault);
		Collections.addAll(both, sshDefault);
		return both.toArray(new Default[both.size()]);
	}

	public Class getSecurityCredentialClass() {
		return rOCCISecurityCredential.class;
	}

	public SecurityCredential createSecurityCredential(int usage,
			Map attributes, String contextId) throws IncorrectStateException,
			TimeoutException, NoSuccessException {
		VOMSSecurityCredential proxy = (VOMSSecurityCredential)super.createSecurityCredential(usage, attributes, contextId);
		SSHSecurityCredential sshCred = (SSHSecurityCredential)m_sshAdaptor.createSecurityCredential(usage, attributes, contextId);
		return new rOCCISecurityCredential(proxy, sshCred);
	}

}
