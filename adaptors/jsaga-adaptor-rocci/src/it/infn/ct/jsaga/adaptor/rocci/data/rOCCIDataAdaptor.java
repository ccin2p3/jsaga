package it.infn.ct.jsaga.adaptor.rocci.data;

import it.infn.ct.jsaga.adaptor.rocci.security.rOCCISecurityCredential;

import java.util.Map;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import com.trilead.ssh2.Connection;
import fr.in2p3.jsaga.adaptor.orionssh.data.SFTPDataAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   rOCCIDataAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   22 oct 2013
* ***************************************************/

public class rOCCIDataAdaptor extends SFTPDataAdaptor {

	public String getType() {
		return "rocci";
	}

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{rOCCISecurityCredential.class};
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) 
    		throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, 
    		BadParameterException, TimeoutException, NoSuccessException {
    		
    	try {
    		m_conn = new Connection(host, port);
    		m_conn.connect(null);
    		String userId = ((rOCCISecurityCredential) credential).getSSHCredential().getUserId();
    		String passPhrase = ((rOCCISecurityCredential) credential).getSSHCredential().getUserPass();
    		// clone private key because the object will be reset
    		byte[] privateKey = ((rOCCISecurityCredential) credential).getSSHCredential().getPrivateKey().clone();
    		char[] pemPrivateKey = new String(privateKey).toCharArray();
    		if (!m_conn.authenticateWithPublicKey(userId, pemPrivateKey, passPhrase)) {
    			m_conn.close();
        		throw new AuthenticationFailedException("Auth fail");
    		}
    	} catch (Exception e) {
			m_conn.close();
    		if("Auth fail".equals(e.getMessage()) )
    			throw new AuthenticationFailedException(e);
    		throw new NoSuccessException("Unable to connect to server", e);
		}
    }
	
}
