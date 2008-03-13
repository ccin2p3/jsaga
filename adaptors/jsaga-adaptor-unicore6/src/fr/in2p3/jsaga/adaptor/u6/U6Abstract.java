package fr.in2p3.jsaga.adaptor.u6;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.NoSuccess;

import com.intel.gpe.client2.StandaloneClient;
import com.intel.gpe.client2.adapters.MessageAdapter;
import com.intel.gpe.client2.common.configurators.SecurityManagerConfigurator;
import com.intel.gpe.client2.common.configurators.UserDefaultsConfigurator;
import com.intel.gpe.client2.defaults.IPreferences;
import com.intel.gpe.client2.panels.GPEPanel;
import com.intel.gpe.client2.providers.MessageProvider;
import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.clients.api.RegistryClient;
import com.intel.gpe.clients.api.TargetSystemClient;
import com.intel.gpe.clients.api.TargetSystemFactoryClient;
import com.intel.gpe.clients.impl.tss.ApplicationImpl;
import com.intel.gui.controls2.configurable.Key;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6Security
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   3 mars 2008
* ***************************************************
/**
 *
 */

public class U6Abstract implements StandaloneClient, MessageProvider {

	protected static final String SERVICE_NAME = "ServiceName";
	protected static final String APPLICATION_NAME = "ApplicationName";
	protected String m_serviceName;
	protected String m_applicationName;
	protected String m_serverUrl ;
    
    public void shutdown() {}

	public void startup() {}

	public void show(GPEPanel arg0) {}
	
	public GPESecurityManager setSecurity() throws AuthenticationFailed {
    	// set system
        System.setProperty("com.intel.gpe.client2.common.configurators.SecurityManagerConfigurator",
        		"com.intel.gpe.client.impl.configurators.SecurityManagerConfiguratorImpl");
        System.setProperty("com.intel.gpe.client2.common.configurators.FileProviderConfigurator",
        		"com.intel.gpe.client.impl.configurators.FileProviderConfiguratorImpl");
        System.setProperty("com.intel.gpe.client2.common.configurators.NetworkConfigurator",
        		"com.intel.gpe.client.impl.configurators.NetworkConfiguratorImpl");
        System.setProperty("com.intel.gpe.client2.common.configurators.UserDefaultsConfigurator",
        		"com.intel.gpe.client.impl.configurators.UserDefaultsConfiguratorImpl");
        System.setProperty("java.util.prefs.PreferencesFactory",
        		"com.intel.gpe.util.preferences.PropertiesPreferencesFactory");        
        
        // TODO: get value 
        String password = "nicolas6";
        System.setProperty("com.intel.gpe.tests.passwd",password);

        // set security
        try {
            // first load user defaults
            Map<String, String> defaults = new HashMap<String, String>();
            Properties clientProperties = new Properties();
            clientProperties.load(this.getClass().getClassLoader().getResourceAsStream("com/intel/gpe/client2/defaults/common.properties"));
            for (Map.Entry<Object, Object> entry : clientProperties.entrySet()) {
               defaults.put((String) entry.getKey(), (String) entry.getValue());
            }
            IPreferences userDefaults = UserDefaultsConfigurator.getConfigurator().getUserDefaults(defaults);

            // set default identity to credential Identity
            //String commonName = globusCred.getName().toString();
            //System.out.println("Identity :"+commonName);
            //userDefaults.set(CommonKeys.DEFAULTIDENTITY,commonName);        
            //System.out.println("Loaded identity:"+ userDefaults.get(CommonKeys.DEFAULTIDENTITY));
            
            // disable logs to console for KeyStoreManager
            Logger logger = Logger.getLogger("com.intel.gpe");
            logger.setLevel(Level.OFF);

            // now initialize security manager        
            GPESecurityManager securityManager = SecurityManagerConfigurator.getConfigurator().getSecurityManager();
            securityManager.init(new MessageAdapter(this, new Key(null,"simpleClient")), userDefaults, this, null);
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
    public static TargetSystemInfo findTargetSystem(String serverUrl, String applicationName, GPESecurityManager securityManager) throws Exception {

    	// Get the available target system factories
    	RegistryClient registry = securityManager.getRegistryClient(serverUrl);    	
        try { 
        
	        List<TargetSystemFactoryClient> targetSystemFactories = registry.getTargetSystemFactories();
	
	        // Loop through all the available target system factories and
	        // ask for available target system resources
	        for (TargetSystemFactoryClient targetSystemFactory : targetSystemFactories) {
	            List<TargetSystemClient> targetSystems = targetSystemFactory.getTargetSystems();
	            for(TargetSystemClient targetSystem : targetSystems) {
	                // If the target system resource supports the requested application
	            	// get application (the first version is used)
                    List<ApplicationImpl> listApplication = targetSystem.getApplications(applicationName);
                    for (ApplicationImpl applicationImpl : listApplication) {
                        return new TargetSystemInfo(applicationName, applicationImpl.getApplicationVersion(), targetSystem);
                    } 
	            }
	        }
	        // no target system found, try to create a new one...
	        // first find a target system factory with the requested application 
	        for (TargetSystemFactoryClient targetSystemFactory : targetSystemFactories) {
	        	 List<ApplicationImpl> listApplication = targetSystemFactory.getApplications(applicationName);
	             for (ApplicationImpl applicationImpl : listApplication) {
	                 // create a target system resource with lifetime 1 day
                     Calendar cal = Calendar.getInstance();
                     cal.add(Calendar.DAY_OF_MONTH, 1);
                     TargetSystemClient newTargetSystem = targetSystemFactory.createTargetSystem(cal);
                     // if new target system has been successfully created, return it.
                     return new TargetSystemInfo(applicationName, applicationImpl.getApplicationVersion(), newTargetSystem);
	             }
	        }
	        
        } catch (NullPointerException e)  {
    		throw new NoSuccess("Unable to find regitry in "+serverUrl);        	
        }
        
        // no target system factory available that supports requested application.
        throw new NoSuccess("No factory available for '"+applicationName+ "'.");
    }
	
}
