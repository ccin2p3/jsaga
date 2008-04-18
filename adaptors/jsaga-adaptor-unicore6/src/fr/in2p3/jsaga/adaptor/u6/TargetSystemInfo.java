package fr.in2p3.jsaga.adaptor.u6;

import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.clients.api.TargetSystemClient;

public class TargetSystemInfo {

	private String applicationName;
	private String applicationVersion;
	private TargetSystemClient targetSystem;
	private GPESecurityManager securityManager;
	
	public TargetSystemInfo(String applicationName, String applicationVersion,
			TargetSystemClient targetSystem, GPESecurityManager securityManager) {
		this.applicationName = applicationName;
		this.applicationVersion = applicationVersion;
		this.targetSystem = targetSystem;
		this.securityManager = securityManager;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	public String getApplicationVersion() {
		return applicationVersion;
	}
	public TargetSystemClient getTargetSystem() {
		return targetSystem;
	}
	public GPESecurityManager getSecurityManager() {
		return securityManager;
	}
}
