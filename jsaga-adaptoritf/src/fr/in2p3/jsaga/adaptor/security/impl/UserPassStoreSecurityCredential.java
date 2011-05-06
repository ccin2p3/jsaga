package fr.in2p3.jsaga.adaptor.security.impl;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassStoreSecurityCredential
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   2 mai 2011
* ***************************************************/

public class UserPassStoreSecurityCredential implements SecurityCredential {

	private String m_host = null;
	private Hashtable m_creds = null;
	private final String DEFAULT_HOST_TOKEN = "default";
	
	public UserPassStoreSecurityCredential() {
		m_creds = new Hashtable();
	}
	
	public String getUserID() throws Exception {
		return this.getUPCred().getUserID();
	}

	public String getUserPass() throws Exception {
		return this.getUPCred().getUserPass();
	}
	
	public String getUserID(String host) throws Exception {
		this.setHost(host);
		return this.getUserID();
	}
	
	public String getUserPass(String host) throws Exception {
		this.setHost(host);
		return this.getUserPass();
	}
	
	public String getAttribute(String key) throws NotImplementedException,
			NoSuccessException {
        if (Context.USERPASS.equals(key)) {
    		return this.getUPCred().getUserPass();
        } else {
            throw new NotImplementedException("Attribute not supported: "+key);
        }
	}

	private UserPassSecurityCredential getUPCred() throws NoSuccessException {
		if (m_host == null) throw new NoSuccessException("Host is not defined yet, UserPass is not available");
		if (m_creds == null) throw new NoSuccessException("Store is not initialized");
		if (m_creds.containsKey(m_host)) return (UserPassSecurityCredential)m_creds.get(m_host);
		if (m_creds.containsKey(this.DEFAULT_HOST_TOKEN)) return (UserPassSecurityCredential)m_creds.get(this.DEFAULT_HOST_TOKEN);
		throw new NoSuccessException("Not found in store:" + m_host);
	}
	
	public void close() throws Exception {
		// nothing
	}

	public void dump(PrintStream out) throws Exception {
		Enumeration e = m_creds.keys();
        while (e.hasMoreElements()){
            String host = (String)e.nextElement();
            out.println("On Host   : "+ host);
            ((UserPassSecurityCredential)m_creds.get(host)).dump(out);
        }
	}

	public void addUserPassCredential(String host, String user, String pass) {
		m_creds.put(host, new UserPassSecurityCredential(user,pass));
	}
	
	public void setHost(String host) {
		m_host = host;
	}
}
