package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.util.HashMap;
import java.util.Map;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   29 avril 2008
* ***************************************************/

public abstract class LocalAdaptorAbstract implements ClientAdaptor {
	
	protected static Map sessionMap = new HashMap();

    public Class[] getSupportedSecurityAdaptorClasses() {
        return null;
    }

    public String getType() {
		return "local";
	}
	
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
    } 
	
    public int getDefaultPort() {
        return 0;
    }
        
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    }

    public void disconnect() throws NoSuccessException {
    } 
}
