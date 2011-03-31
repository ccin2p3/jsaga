package fr.in2p3.jsaga.impl.context;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ConfiguredContext
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   31/03/2011
* ***************************************************
* Description:                                      */

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
