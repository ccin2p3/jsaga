package fr.in2p3.jsaga.adaptor.bes_genesis2.job;

import fr.in2p3.jsaga.adaptor.bes.job.BesJob;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesGenesisIIJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 sept 2011
* ***************************************************/
public class BesGenesisIIJobControlAdaptor extends /*BesJobControlStagingOnePhaseAdaptorAbstract*/BesJobControlAdaptor implements JobControlAdaptor {

	//private static final String DATA_STAGING_PATH = "/DataStaging";
    private static final String XSLTPARAM_CONTAINER = "genii-container-id";
	
    //private String m_target;
    
    public String getType() {
        return "bes-genesis2";
    }
    
	public int getDefaultPort() {
		return 18443;
	}

    /*protected String getJobDescriptionTranslatorFilename() throws NoSuccessException {
    	return "xsl/job/bes-unicore-jsdl.xsl";
    }*/

    /*public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	JobDescriptionTranslator translator =  super.getJobDescriptionTranslator();
   		translator.setAttribute(XSLTPARAM_TARGET, m_target);
    	translator.setAttribute(XSLTPARAM_RES, "default_storage");
    	return translator;
    }*/

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BesGenesisIIJobMonitorAdaptor();
    }

	/*
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
	*/
	
	/*public void disconnect() throws NoSuccessException {
		super.disconnect();
	}*/

    /*
    public URI getBESUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
    	URI url = super.getBESUrl(host, port, basePath, attributes);
		return new URI(url.toString() + "?" + XSLTPARAM_CONTAINER + "=" + (String)attributes.get(XSLTPARAM_CONTAINER));
    }
	*/
    
	/*public URI getDataStagingUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
		// This URL is used by JSAGA to choose the appropriate data plugin: the new UNICORE plugin
		// extract Target 'DEMO-SITE' from basePath = '/DEMO-SITE/services/BESFactory'
		String _target = basePath.split("/")[1];
		return new URI("unicore://" + host + ":" + port + DATA_STAGING_PATH + "?Target=" + _target);
	}*/
	
}