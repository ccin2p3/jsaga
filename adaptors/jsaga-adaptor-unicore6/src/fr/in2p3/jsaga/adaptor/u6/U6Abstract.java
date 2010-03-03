package fr.in2p3.jsaga.adaptor.u6;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import com.intel.gpe.clients.api.RegistryClient;
import com.intel.gpe.clients.api.TargetSystemClient;
import com.intel.gpe.clients.api.TargetSystemFactoryClient;
import com.intel.gpe.clients.impl.tss.ApplicationImpl;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6Abstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   3 mars 2008
* ***************************************************/


public class U6Abstract {

	protected static final String APPLICATION_NAME = "ApplicationName";
	protected String m_serviceName;
	protected String m_applicationName;
	protected String m_serverUrl ;
	protected JKSSecurityAdaptor m_credential;
	protected U6SecurityManagerImpl m_securityManager = null;
    
    public Class[] getSupportedSecurityAdaptorClasses() {
    	return new Class[]{JKSSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
    	 m_credential = (JKSSecurityAdaptor) securityAdaptor;
    }

    public int getDefaultPort() {
        return 8080;
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	m_serverUrl = "https://"+host+":"+port+basePath;
        
    	// get APPLICATION_NAME
    	m_applicationName = (String) attributes.get(APPLICATION_NAME);
    }

	public void disconnect() throws NoSuccessException {
        m_serverUrl = null;
        m_credential = null;
        m_applicationName = null;
        m_securityManager = null;
    }    
	
    /**
     * Loop over all target systems and return one that supports the application
     * that should be executed
     * @throws Exception 
     * @throws Exception 
     */
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
	
}
