package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import org.globus.common.CoGProperties;
import org.globus.gram.*;
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

public abstract class GatekeeperJobAdaptorAbstract implements ClientAdaptor {
    protected GSSCredential m_credential;
    protected String m_serverUrl;
    protected static final String IP_ADDRESS = "IPAddress";
    protected static final String TCP_PORT_RANGE = "TcpPortRange";

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        m_credential = ((GSSCredentialSecurityCredential) credential).getGSSCredential();
    }

    public int getDefaultPort() {
        return 2119;
    }
   
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
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
    	// Overload cog properties
    	if (attributes!=null && attributes.containsKey(TCP_PORT_RANGE)) {
            String value = ((String) attributes.get(TCP_PORT_RANGE));
        	CoGProperties loadedCogProperties= CoGProperties.getDefault();
    		loadedCogProperties.setProperty("tcp.port.range", value);
    		CoGProperties.setDefault(loadedCogProperties);
    	}
        if("true".equalsIgnoreCase((String) attributes.get(JobAdaptor.CHECK_AVAILABILITY))) {
	        try {
	            Gram.ping(m_credential, m_serverUrl);
	        } catch (GramException e) {
	            switch(e.getErrorCode()) {
	                case GRAMProtocolErrorConstants.ERROR_PROTOCOL_FAILED:
                        Throwable cause = e.getException();
                        if (cause!=null && cause.getMessage()!=null && cause.getMessage().startsWith("Authentication failed")) {
                            throw new AuthenticationFailedException(cause);
                        } else {
                            throw new NoSuccessException(e);
                        }
                    case GRAMProtocolErrorConstants.ERROR_AUTHORIZATION:
	                    throw new AuthorizationFailedException(e);
	                case GRAMProtocolErrorConstants.INVALID_JOB_CONTACT:
	                case GRAMProtocolErrorConstants.ERROR_CONNECTION_FAILED:
	                    throw new TimeoutException(e);
	                default:
	                    throw new NoSuccessException(e);
	            }
	        } catch (GSSException e) {
	            throw new AuthenticationFailedException(e);
	        }
    	}
    }

    public void disconnect() throws NoSuccessException {
        m_serverUrl = null;
    }
    

    protected GramJob getGramJobById(String nativeJobId) throws NoSuccessException {
        GramJob job = new GramJob(m_credential, null);
        try {
            job.setID(nativeJobId);
        } catch (MalformedURLException e) {
            throw new NoSuccessException(e);
        }
        return job;
    }
}
