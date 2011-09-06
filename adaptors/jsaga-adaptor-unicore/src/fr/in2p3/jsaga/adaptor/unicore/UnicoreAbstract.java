package fr.in2p3.jsaga.adaptor.unicore;

import java.util.Map;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.security.IUASSecurityProperties;
import de.fzj.unicore.uas.security.UASSecurityProperties;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
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


public abstract class UnicoreAbstract implements ClientAdaptor {

	protected static final String SERVICE_NAME = "ServiceName";
	protected static final String RES = "Res";
	protected static final String TARGET = "Target";
	protected String m_target;
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

    public String getType() {
        return "unicore";
    }

    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(TARGET),
    								new U(SERVICE_NAME), 
    								new U(RES),
    								});
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        
    	m_target = (String) attributes.get(TARGET);
    	String serverUrl = "https://"+host+":"+port+"/"+m_target+"/services/"+(String) attributes.get(SERVICE_NAME)+"?res="+(String) attributes.get(RES);
    	
    	m_uassecprop = new UASSecurityProperties();
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL, "true");
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_CLIENTAUTH, "true");
		
        //keystore and truststore locations
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYSTORE, m_credential.getKeyStorePath());
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYPASS, m_credential.getKeyStorePass());
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYALIAS, m_credential.getKeyStoreAlias());
    	m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTSTORE, m_credential.getTrustStorePath());
    	if (m_credential.getTrustStorePass() != null) {
    		m_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTPASS, m_credential.getTrustStorePass());
    	}

		m_epr = EndpointReferenceType.Factory.newInstance();
	    m_epr.addNewAddress().setStringValue(serverUrl);
    }

	public void disconnect() throws NoSuccessException {
        m_credential = null;
        m_target = null;
        m_epr = null;
        m_uassecprop = null;
    }    
	
}
