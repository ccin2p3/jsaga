package fr.in2p3.jsaga.adaptor.u6;

import com.intel.gpe.clients.api.TargetSystemClient;

public class TargetSystemInfo {

	private String applicationName;
	private String applicationVersion;
	private TargetSystemClient targetSystem;
	
	
	public TargetSystemInfo(String applicationName, String applicationVersion,
			TargetSystemClient targetSystem) {
		this.applicationName = applicationName;
		this.applicationVersion = applicationVersion;
		this.targetSystem = targetSystem;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getApplicationVersion() {
		return applicationVersion;
	}
	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	public TargetSystemClient getTargetSystem() {
		return targetSystem;
	}
	public void setTargetSystem(TargetSystemClient targetSystem) {
		this.targetSystem = targetSystem;
	}
	
	
	
}
