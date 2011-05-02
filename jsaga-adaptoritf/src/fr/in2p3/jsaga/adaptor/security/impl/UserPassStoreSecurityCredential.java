package fr.in2p3.jsaga.adaptor.security.impl;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

public class UserPassStoreSecurityCredential implements SecurityCredential {

	private String m_host = null;
	private Hashtable m_creds = null;
	
	public UserPassStoreSecurityCredential() {
		m_creds = new Hashtable();
	}
	
	public String getUserID() throws Exception {
		if (m_host == null) throw new Exception("Host is not defined yet, UserID is not available");
		if (m_creds == null) throw new Exception("Store is not initialized");
		if (!m_creds.containsKey(m_host)) throw new Exception("Not found in store:" + m_host);
		UserPassSecurityCredential upsc = (UserPassSecurityCredential)m_creds.get(m_host);
		return upsc.getUserID();
	}

	public String getAttribute(String key) throws NotImplementedException,
			NoSuccessException {
        if (Context.USERPASS.equals(key)) {
    		if (m_host == null) throw new NoSuccessException("Host is not defined yet, UserID is not available");
    		if (m_creds == null) throw new NoSuccessException("Store is not initialized");
    		if (!m_creds.containsKey(m_host)) throw new NoSuccessException("Not found in store:" + m_host);
    		UserPassSecurityCredential upsc = (UserPassSecurityCredential)m_creds.get(m_host);
    		return upsc.getUserPass();
        } else {
            throw new NotImplementedException("Attribute not supported: "+key);
        }
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
}
