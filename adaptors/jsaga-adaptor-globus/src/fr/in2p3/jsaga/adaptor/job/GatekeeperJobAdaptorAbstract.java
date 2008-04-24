package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.globus.common.CoGProperties;
import org.globus.gram.Gram;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.internal.GRAMProtocolErrorConstants;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;

import java.net.MalformedURLException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GatekeeperJobAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 nov. 2007
* ***************************************************
* Description:                                      */

public abstract class GatekeeperJobAdaptorAbstract implements SagaSecureAdaptor {
    protected GSSCredential m_credential;
    protected String m_serverUrl;
    protected static final String IP_ADDRESS = "IPAddress";

    public String getType() {
        return "gatekeeper";
    }
    
    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_credential = ((GSSCredentialSecurityAdaptor) securityAdaptor).getGSSCredential();
    }

    public int getDefaultPort() {
        return 2119;
    }
   
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    	if(basePath.indexOf("=") > -1)
    		m_serverUrl = host+":"+port+":"+basePath;
    	else
    		m_serverUrl = host+":"+port+basePath;
    	// Overload cog properties
    	if (attributes!=null && attributes.containsKey(IP_ADDRESS)) {
            String value = ((String) attributes.get(IP_ADDRESS));
        	CoGProperties loadedCogProperties= CoGProperties.getDefault();
    		loadedCogProperties.setIPAddress(value);
    		CoGProperties.setDefault(loadedCogProperties);
    	}
    	
    	// TODO : user CheckAvailability parameter
    	if(true) {
	        try {
	            Gram.ping(m_credential, m_serverUrl);
	        } catch (GramException e) {
	            switch(e.getErrorCode()) {
	                case GRAMProtocolErrorConstants.ERROR_PROTOCOL_FAILED:
	                    throw new AuthenticationFailed("Proxy may be expired", e);
	                case GRAMProtocolErrorConstants.ERROR_AUTHORIZATION:
	                    throw new AuthorizationFailed(e);
	                case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
	                case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
	                    throw new Timeout(e);
	                default:
	                    throw new NoSuccess(e);
	            }
	        } catch (GSSException e) {
	            throw new AuthenticationFailed(e);
	        }
    	}
    }

    public void disconnect() throws NoSuccess {
        m_serverUrl = null;
    }
    

    protected GramJob getGramJobById(String nativeJobId) throws NoSuccess {
        GramJob job = new GramJob(m_credential, null);
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e) {
            throw new NoSuccess(e);
        }
        return job;
    }
}
