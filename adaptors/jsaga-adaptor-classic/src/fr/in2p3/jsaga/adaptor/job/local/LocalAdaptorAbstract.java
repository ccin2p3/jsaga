package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;

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

public abstract class LocalAdaptorAbstract implements SagaSecureAdaptor {
	
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
        
    public Default[] getDefaults(Map map) throws IncorrectState {
    	return null;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    }

    public void disconnect() throws NoSuccess {
    } 
}
