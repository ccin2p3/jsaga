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

	protected static final String APPLICATION_NAME = "ApplicationName";
	protected static final String SERVICE_NAME = "ServiceName";
	protected static final String RES = "res";
	protected String m_serviceName;
	protected String m_applicationName;
	protected String m_res;
	protected String m_serverUrl ;
	protected JKSSecurityCredential m_credential;
	//protected U6SecurityManagerImpl m_securityManager = null;
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
        
    	// get APPLICATION_NAME
    	m_applicationName = (String) attributes.get(APPLICATION_NAME);
		m_serviceName = (String) attributes.get(SERVICE_NAME);
    	m_res = (String) attributes.get(RES);
    	
    	// build URL like https://<HOST>:<PORT>/<SITE>/services/<SERVICE_NAME>?res=<RES>
    	//m_serverUrl = "https://"+host+":"+port+"/DEMO-SITE/services/StorageFactory?res=default_storage_factory";
    	m_serverUrl = "https://"+host+":"+port+"/DEMO-SITE/services/StorageManagement?res=82ee2a6d-ed73-4d3b-b299-56b114f0919f";
    	
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
        m_applicationName = null;
        //m_registryClient = null;
        //m_securityManager = null;
    }    
	
    /**
     * Loop over all target systems and return one that supports the application
     * that should be executed
     * @throws Exception 
     * @throws Exception 
     */
	/*
    public TargetSystemInfo findTargetSystem() throws Exception {
    	try { 
    		// set security
        	try {            
                // disable logs to console for KeyStoreManager
                Logger logger = Logger.getLogger("com.intel.gpe");
                logger.setLevel(Level.OFF);

                // now initialize security manager        
                U6SecurityManagerImpl securityManager = new U6SecurityManagerImpl();
                X509Certificate[] certs = m_credential.getCaCertificates();
                Vector<X509Certificate> caCertificateVector = new Vector<X509Certificate>();
                for (int i = 0; i < certs.length; i++) {
                	caCertificateVector.add(certs[i]);
    			}
                securityManager.init(caCertificateVector, m_credential.getCertificate(), m_credential.getPrivateKey());
                
    	    	if(securityManager == null) {
                    throw new AuthenticationFailedException("Unable to initialize security manager");
                }
    	    	m_securityManager = securityManager;
    	        
    		} catch (Exception e) {
    			throw new AuthenticationFailedException(e);
    		}
    		
    		RegistryClient registry = m_securityManager.getRegistryClient(m_serverUrl);
			List<TargetSystemFactoryClient> targetSystemFactories;
            try {
                targetSystemFactories = registry.getTargetSystemFactories();
            } catch (NullPointerException e) {
                throw new NoSuccessException("Server not found: "+m_serverUrl, e);
            }
	        
        	// Loop through all the available target system factories and
	        for (TargetSystemFactoryClient targetSystemFactory : targetSystemFactories) {
	        	 // ask for available target system resources	 	       
	        	List<TargetSystemClient> targetSystems = targetSystemFactory.getTargetSystems();
	        	for(TargetSystemClient targetSystem : targetSystems) {
	                // If the target system resource supports the requested application
	            	// get application (the first version is used)
                    List<ApplicationImpl> listApplication = targetSystem.getApplications(m_applicationName);
                    for (ApplicationImpl applicationImpl : listApplication) {
                    	return new TargetSystemInfo(m_applicationName, applicationImpl.getApplicationVersion(), targetSystem, m_securityManager);
                    } 
	            }
	            // no target system found, try to create a new one...
		        // first find a target system factory with the requested application 
	            List<ApplicationImpl> listApplication = targetSystemFactory.getApplications(m_applicationName);
	            for (ApplicationImpl applicationImpl : listApplication) {
	                 // create a target system resource with lifetime 1 day
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    TargetSystemClient newTargetSystem = targetSystemFactory.createTargetSystem(cal);
                    // if new target system has been successfully created, return it.
                    return new TargetSystemInfo(m_applicationName, applicationImpl.getApplicationVersion(), newTargetSystem, m_securityManager);
	             }
	        }
        } catch (NullPointerException e)  {
    		throw new NoSuccessException("Unable to find regitry in "+m_serverUrl, e);
        }
        
        // no target system factory available that supports requested application.
        throw new NoSuccessException("No factory available for '"+m_applicationName+ "'.");
    }
	*/
}
