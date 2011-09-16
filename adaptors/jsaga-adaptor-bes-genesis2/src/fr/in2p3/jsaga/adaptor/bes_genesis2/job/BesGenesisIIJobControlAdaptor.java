package fr.in2p3.jsaga.adaptor.bes_genesis2.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlStagingOnePhaseAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.data.http_socket.HttpRequest;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ogf.saga.error.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesUnicoreJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 sept 2011
* ***************************************************/
public class BesGenesisIIJobControlAdaptor extends /*BesJobControlStagingOnePhaseAdaptorAbstract*/BesJobControlAdaptorAbstract implements JobControlAdaptor {

	//private static final String DATA_STAGING_PATH = "/DataStaging";
    private static final String XSLTPARAM_CONTAINER = "genii-container-id";
	
    //private String m_target;
    
    public String getType() {
        return "bes-genesis2";
    }
    
	public int getDefaultPort() {
		return 18443;
	}

    /*public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default("res", "default_bes_factory")};
    }*/
    
    /*protected String getJobDescriptionTranslatorFilename() throws NoSuccessException {
    	return "xsl/job/bes-unicore-jsdl.xsl";
    }*/

    /*public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	JobDescriptionTranslator translator =  super.getJobDescriptionTranslator();
   		translator.setAttribute(XSLTPARAM_TARGET, m_target);
    	translator.setAttribute(XSLTPARAM_RES, "default_storage");
    	// TODO: passer un paramètre pour indiquer si "Custom executable" est supporté
    	return translator;
    }*/

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BesGenesisIIJobMonitorAdaptor();
    }

	public Class getJobClass() {
		return BesGenesisIIJob.class;
	}

	/*
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	// extract Target from _ds_url
    	m_target = _ds_url.getQuery().split("=")[1];
    	String _serverUrl = "https://"+host+":"+port+"/"+m_target+"/services/StorageManagement?res=default_storage";
    	
    	UASSecurityProperties _uassecprop = new UASSecurityProperties();
    	_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL, "true");
    	_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_CLIENTAUTH, "true");
		
        //keystore and truststore locations
    	_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYSTORE, m_credential.getKeyStorePath());
    	_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYPASS, m_credential.getKeyStorePass());
    	_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_KEYALIAS, m_credential.getKeyStoreAlias());
    	_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTSTORE, m_credential.getTrustStorePath());
    	if (m_credential.getTrustStorePass() != null) {
    		_uassecprop.setProperty(IUASSecurityProperties.WSRF_SSL_TRUSTPASS, m_credential.getTrustStorePass());
    	}

    	EndpointReferenceType _epr = EndpointReferenceType.Factory.newInstance();
	    _epr.addNewAddress().setStringValue(_serverUrl);
	    // Create data staging directory
	    try {
			StorageClient _client = new StorageClient(_epr,_uassecprop);
			try {
				_client.listProperties(DATA_STAGING_PATH);
			} catch (FileNotFoundException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Creating data staging directory");
				_client.createDirectory(DATA_STAGING_PATH);
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARN, "Error while creating data staging directory. Data staging may not work", e);
		}
	}
	*/
	
	/*public void disconnect() throws NoSuccessException {
		m_target = null;
		super.disconnect();
	}*/

	/*
    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	
		try {
			// Remove <?xml version="1.0" encoding="UTF-8"?> from jobDesc
			jobDesc = jobDesc.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length());
			//System.out.println(jobDesc);
            
			
            String xmlSoap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
            xmlSoap += "<soapenv:Body>";
            xmlSoap += "<CreateActivity xmlns=\"http://schemas.ggf.org/bes/2006/08/bes-factory\">";
            xmlSoap += "<ActivityDocument>";
            xmlSoap += jobDesc;
            xmlSoap += "</ActivityDocument>";
            xmlSoap += "</CreateActivity>";
            xmlSoap += "</soapenv:Body>";
            xmlSoap += "</soapenv:Envelope>";
            
            String dataToSend = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + xmlSoap;

            
            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) socketFactory.createSocket(_bes_url.getHost(), _bes_url.getPort());
            socket.startHandshake();
            
            // Prepare request
			HttpRequest req = new HttpRequest(HttpRequest.TYPE_POST, _bes_url.getPath() + "?" + _bes_url.getQuery(), socket, false);
			req.setVersion("1.0");
			req.addHeader("Accept", "application/soap+xml, application/dime, multipart/related, text/*");
			req.addHeader("Content-Type", "text/xml; charset=utf-8");
			req.addHeader("Pragma","no-cache");
			req.addHeader("Cache-Control","no-cache");
			req.addHeader("SOAPAction", "\"http://schemas.ggf.org/bes/2006/08/bes-factory/BESFactoryPortType/CreateActivity\"");
			req.write(dataToSend);
			
			// send request
			req.send();
			
			// get response status
			String status = req.getStatus();
			if (! status.endsWith("200 OK")) {
				throw new NoSuccessException("HTTP status: " + status);
			}
            //Parse response
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(req.getInputStream());
            Element ident = (Element) document.getElementsByTagName("bes:ActivityIdentifier").item(0);
            if (ident == null)
            	throw new NoSuccessException("<bes:ActivityIdentifier> tag not found");
            Element addr = (Element) ident.getElementsByTagName("add:Address").item(0);
            if (addr == null)
            	throw new NoSuccessException("<add:Address> tag not found");
            return addr.getTextContent();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ParserConfigurationException e) {
			throw new NoSuccessException(e);
		} catch (SAXException e) {
			throw new NoSuccessException(e);
		}
    }
    */
	
    public URI getBESUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
    	URI url = super.getBESUrl(host, port, basePath, attributes);
		return new URI(url.toString() + "?" + XSLTPARAM_CONTAINER + "=" + (String)attributes.get(XSLTPARAM_CONTAINER));
    }

	/*public URI getDataStagingUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
		// This URL is used by JSAGA to choose the appropriate data plugin: the new UNICORE plugin
		// extract Target 'DEMO-SITE' from basePath = '/DEMO-SITE/services/BESFactory'
		String _target = basePath.split("/")[1];
		return new URI("unicore://" + host + ":" + port + DATA_STAGING_PATH + "?Target=" + _target);
	}*/
	
}