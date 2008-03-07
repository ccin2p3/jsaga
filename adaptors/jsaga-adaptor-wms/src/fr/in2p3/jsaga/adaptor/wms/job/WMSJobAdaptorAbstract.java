package fr.in2p3.jsaga.adaptor.wms.job;

import java.io.File;
import java.util.Map;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;

import org.ietf.jgss.GSSCredential;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
/**
 *
 */
public abstract class WMSJobAdaptorAbstract implements SagaSecureAdaptor {

	protected File m_tmpProxyFile;
    protected GSSCredential m_credential;
    protected String m_serverUrl;
    protected String rootLogDir = System.getProperty("user.home") + File.separator;
    
    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_credential = ((GSSCredentialSecurityAdaptor) securityAdaptor).getGSSCredential();
    }

    public int getDefaultPort() {
        return 7443;
    }
    
    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	return null;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    	m_serverUrl = "https://"+host+":"+port+basePath;
    }

    public void disconnect() throws NoSuccess {
    	System.out.println("Disconnect in WMSJobAdaptorAbstract");
        m_serverUrl = null;
        m_credential = null;
    }
}
