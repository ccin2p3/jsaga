package fr.in2p3.jsaga.adaptor.bes_unicore.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlStagingOnePhaseAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.data.http_socket.HttpRequest;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.ogf.saga.error.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
* Date:   9 déc. 2010
* ***************************************************/
public class BesUnicoreJobControlAdaptor extends BesJobControlStagingOnePhaseAdaptorAbstract implements JobControlAdaptor {

	private static final String DATA_STAGING_PATH = "/DataStaging";
    private static final String XSLTPARAM_TARGET = "Target";
    private static final String XSLTPARAM_RES = "Res";
	
    public String getType() {
        return "bes-unicore";
    }
    
	public int getDefaultPort() {
		return 8080;
	}

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default("res", "default_bes_factory")};
    }
    
    protected String getJobDescriptionTranslatorFilename() throws NoSuccessException {
    	return "xsl/job/bes-unicore-jsdl.xsl";
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	JobDescriptionTranslator translator =  super.getJobDescriptionTranslator();
    	// extract Target from _ds_url
    	if (_ds_url.getQuery() != null) {
    		String _target = _ds_url.getQuery().split("=")[1];
    		translator.setAttribute(XSLTPARAM_TARGET, _target);
    	}
    	translator.setAttribute(XSLTPARAM_RES, "default_storage");
    	return translator;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new BesUnicoreJobMonitorAdaptor();
    }

	public Class getJobClass() {
		return BesUnicoreJob.class;
	}

	/*
	 * bypass BES stubs for submission as URI for datastaging are changed (scheme in lowercase) by the Axis URI class
	 * 
	 */
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
    
    public URI getBESUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
    	URI url = super.getBESUrl(host, port, basePath, attributes);
		return new URI(url.toString()+"?res=" + (String)attributes.get("res"));
    }

	public URI getDataStagingUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
		// This URL is used by JSAGA to choose the appropriate data plugin: the new UNICORE plugin
		// extract Target 'DEMO-SITE' from basePath = '/DEMO-SITE/services/BESFactory'
		String _target = basePath.split("/")[1];
		return new URI("unicore://" + host + ":" + port + DATA_STAGING_PATH + "?Target=" + _target);
	}
	
}