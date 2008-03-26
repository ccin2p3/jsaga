package fr.in2p3.jsaga.adaptor.u6;

import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.NoSuccess;

import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.clients.api.RegistryClient;
import com.intel.gpe.clients.api.TargetSystemClient;
import com.intel.gpe.clients.api.TargetSystemFactoryClient;
import com.intel.gpe.clients.impl.tss.ApplicationImpl;

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

	protected static final String SERVICE_NAME = "ServiceName";
	protected static final String APPLICATION_NAME = "ApplicationName";
	protected String m_serviceName;
	protected String m_applicationName;
	protected String m_serverUrl ;
	protected GPESecurityManager m_securityManager;
    
	public GPESecurityManager setSecurity(JKSSecurityAdaptor jksSecurityAdaptor) throws AuthenticationFailed {
    	   
        // set security
        try {
            
            // disable logs to console for KeyStoreManager
            Logger logger = Logger.getLogger("com.intel.gpe");
            logger.setLevel(Level.OFF);

            // now initialize security manager        
            U6SecurityManagerImpl securityManager = new U6SecurityManagerImpl();
            X509Certificate[] certs = jksSecurityAdaptor.getCaCertificates();
            Vector<X509Certificate> caCertificateVector = new Vector<X509Certificate>();
            for (int i = 0; i < certs.length; i++) {
            	caCertificateVector.add(certs[i]);
			}
            securityManager.init(caCertificateVector, jksSecurityAdaptor.getCertificate(), jksSecurityAdaptor.getPrivateKey());
            
	    	if(securityManager == null) {
                throw new AuthenticationFailed("Unable to initialize security manager");
            }
        	return securityManager;
        	
		} catch (Exception e) {
			throw new AuthenticationFailed(e);
		}
	}
    
    /**
     * Loop over all target systems and return one that supports the application
     * that should be executed
     * @throws Exception 
     * @throws Exception 
     */
    public TargetSystemInfo findTargetSystem() throws Exception {

       RegistryClient registry = m_securityManager.getRegistryClient(m_serverUrl);    	
        try { 
        
	        List<TargetSystemFactoryClient> targetSystemFactories = registry.getTargetSystemFactories();
	
	        // Loop through all the available target system factories and
	        // ask for available target system resources
	        for (TargetSystemFactoryClient targetSystemFactory : targetSystemFactories) {
	            List<TargetSystemClient> targetSystems = targetSystemFactory.getTargetSystems();
	            for(TargetSystemClient targetSystem : targetSystems) {
	                // If the target system resource supports the requested application
	            	// get application (the first version is used)
                    List<ApplicationImpl> listApplication = targetSystem.getApplications(m_applicationName);
                    for (ApplicationImpl applicationImpl : listApplication) {
                        return new TargetSystemInfo(m_applicationName, applicationImpl.getApplicationVersion(), targetSystem);
                    } 
	            }
	        }
	        // no target system found, try to create a new one...
	        // first find a target system factory with the requested application 
	        for (TargetSystemFactoryClient targetSystemFactory : targetSystemFactories) {
	        	 List<ApplicationImpl> listApplication = targetSystemFactory.getApplications(m_applicationName);
	             for (ApplicationImpl applicationImpl : listApplication) {
	                 // create a target system resource with lifetime 1 day
                     Calendar cal = Calendar.getInstance();
                     cal.add(Calendar.DAY_OF_MONTH, 1);
                     TargetSystemClient newTargetSystem = targetSystemFactory.createTargetSystem(cal);
                     // if new target system has been successfully created, return it.
                     return new TargetSystemInfo(m_applicationName, applicationImpl.getApplicationVersion(), newTargetSystem);
	             }
	        }
	        
        } catch (NullPointerException e)  {
        	e.printStackTrace();
    		throw new NoSuccess("Unable to find regitry in "+m_serverUrl);        	
        }
        
        // no target system factory available that supports requested application.
        throw new NoSuccess("No factory available for '"+m_applicationName+ "'.");
    }
	
}
