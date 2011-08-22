package fr.in2p3.jsaga.adaptor.unicore;

import java.util.Iterator;
import java.util.Map;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.RegistryClient;
import de.fzj.unicore.uas.client.StorageFactoryClient;
import de.fzj.unicore.uas.security.IUASSecurityProperties;
import de.fzj.unicore.uas.security.UASSecurityProperties;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   19 aout 2011
* ***************************************************/


public class UnicoreAbstract {

	protected static final String SERVICE_NAME = "ServiceName";
	protected static final String RES = "Res";
	protected static final String TARGET = "Target";
	protected String m_serviceName;
	protected String m_res;
	protected String m_serverUrl ;
	protected JKSSecurityCredential m_credential;
	protected UASSecurityProperties m_uassecprop = null;
	protected EndpointReferenceType m_epr = null;
    
    public Class[] getSupportedSecurityCredentialClasses() {
    	return new Class[]{JKSSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
    	 m_credential = (JKSSecurityCredential) credential;
    }

    public int getDefaultPort() {
        return 8080;
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        

		m_serviceName = (String) attributes.get(SERVICE_NAME);
    	m_res = (String) attributes.get(RES);
    	
    	// build URL like https://<HOST>:<PORT>/<TARGET>/services/<SERVICE_NAME>?res=<RES>
    	m_serverUrl = "https://"+host+":"+port+"/"+(String) attributes.get(TARGET)+"/services/"+m_serviceName+"?res="+m_res;
    	
    	m_uassecprop = new UASSecurityProperties();
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL, "true");
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_CLIENTAUTH, "true");
		
        //keystore and truststore locations
		// TODO get JKS filename
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYSTORE, "/home/schwarz/.jsaga/contexts/unicore6/demouser.jks");
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYPASS, "the!user");
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYALIAS, "demo user");
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTSTORE, "/home/schwarz/.jsaga/contexts/unicore6/demouser.jks");
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTPASS, "the!user");

	    m_epr = EndpointReferenceType.Factory.newInstance();
	    m_epr.addNewAddress().setStringValue(m_serverUrl);
    }

	public void disconnect() throws NoSuccessException {
        m_serverUrl = null;
        m_credential = null;
        m_res = null;
        m_epr = null;
        m_uassecprop = null;
    }    
	
}
