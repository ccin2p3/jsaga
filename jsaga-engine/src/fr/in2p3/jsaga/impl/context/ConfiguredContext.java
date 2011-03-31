package fr.in2p3.jsaga.impl.context;

public class ConfiguredContext {
	private String m_urlPrefix;
	private String m_type;
	public ConfiguredContext(String url, String type) {
		m_urlPrefix = url;
		m_type = type;
	}
	
	public String getUrlPrefix() {
		return m_urlPrefix;
	}
	
	public String getType() {
		return m_type;
	}
}
