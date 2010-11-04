package fr.in2p3.jsaga.adaptor.wsgram.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;

import org.apache.axis.AxisProperties;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.globus.axis.gsi.GSIConstants;
import org.globus.common.CoGProperties;
import org.globus.exec.client.GramJob;
import org.globus.wsrf.client.ServiceURL;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.ietf.jgss.GSSCredential;

import org.oasis.wsrf.properties.GetResourceProperty;
import org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType;
import org.oasis.wsrf.properties.WSResourcePropertiesServiceAddressingLocator;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WSGramJobAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   6 fev. 2008
* ***************************************************
/**
 *
 */
public abstract class WSGramJobAdaptorAbstract implements ClientAdaptor {
	
    protected GSSCredential m_credential;
    protected String m_host;
    protected String m_serverUrl, m_serverBatch = "Fork";
    private static final String IP_ADDRESS = "IPAddress";
    
    public String getType() {
        return "wsgram";
    }
    
    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        m_credential = ((GSSCredentialSecurityCredential) credential).getGSSCredential();
    }

    public int getDefaultPort() {
        return 8443;
    }
    
    public Usage getUsage() {
        return new UOptional(IP_ADDRESS);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	try {
			String defaultIp = InetAddress.getLocalHost().getHostAddress();
	    	return new Default[]{new Default(IP_ADDRESS, defaultIp)};
		} catch (UnknownHostException e) {
			return null;
		}
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_host = host;
        m_serverUrl = "https://"+host+":"+port;
    	if(basePath != null &&
    			!basePath.equals("") && 
    			!basePath.equals("/")) { 
    			if(basePath.indexOf("/") > -1)
    				m_serverBatch = basePath.replaceAll("/", "");
    			else
    				m_serverBatch = basePath;
    	}
    	// Overload cog properties
    	if (attributes!=null && attributes.containsKey(IP_ADDRESS)) {
            String value = ((String) attributes.get(IP_ADDRESS));
        	CoGProperties loadedCogProperties= CoGProperties.getDefault();
    		loadedCogProperties.setIPAddress(value);
    		CoGProperties.setDefault(loadedCogProperties);
    	}    	
    	// load axis configuration
    	String home = System.getProperty("user.home");
    	System.setProperty("GLOBUS_LOCATION", home);
		System.setProperty("org.globus.wsrf.container.webroot", home);
		ContainerConfig.getConfig().setOption(ContainerConfig.WSRF_LOCATION, home);
		ContainerConfig.getConfig().setOption(ContainerConfig.INTERNAL_WEB_ROOT_PROPERTY, home);    	
		if(CoGProperties.getDefault().getIPAddress() != null) {
			ContainerConfig.getConfig().setOption(ContainerConfig.LOGICAL_HOST,
				CoGProperties.getDefault().getIPAddress());
			CoGProperties.getDefault().setHostName(CoGProperties.getDefault().getIPAddress());
		}		
    	
    	String clientConfigFile = Base.JSAGA_VAR+ File.separator+ "client-config-wsgram.wsdd";
    	// save client-config.wsdd on JSAGA_VAR from jar 
    	if(!new File(clientConfigFile).exists()) {
    		// save client-config.wsdd
    		try {
    			InputStream is = this.getClass().getResourceAsStream("/"+new File(clientConfigFile).getName());
        		FileOutputStream fos = new FileOutputStream (new File(clientConfigFile));
	    		int n;
	    		while ((n = is.read()) != -1 ){
	    			fos.write(n);
	    		}
	    		fos.close();
			} catch (FileNotFoundException e) {
				throw new NoSuccessException(e);
			} catch (IOException e) {
				throw new NoSuccessException(e);
			}
    	}    	
    	AxisProperties.setProperty(EngineConfigurationFactoryDefault.OPTION_CLIENT_CONFIG_FILE, clientConfigFile);
        
    	// Ping service
    	if("true".equalsIgnoreCase((String) attributes.get(JobAdaptor.CHECK_AVAILABILITY))) {
	    	try {
	    		URL pingHandle = ServiceURL.getURL(host+":"+port, "ContainerRegistryService");
	
				// Get stub
		        WSResourcePropertiesServiceAddressingLocator locator = new WSResourcePropertiesServiceAddressingLocator();
		        GetResourceProperty stub = locator.getGetResourcePropertyPort(pingHandle);
	
	            // Set security
	            ((Stub)stub)._setProperty(Constants.AUTHORIZATION, NoAuthorization.getInstance());
	            ((Stub)stub)._setProperty(GSIConstants.GSI_TRANSPORT, Constants.SIGNATURE);
	            ((Stub)stub)._setProperty(GSIConstants.GSI_CREDENTIALS, m_credential);
	
	            // Invoke operation
	            QName qname = new QName("http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd", "MembershipContentRule");
	            stub.getResourceProperty(qname);
	
			} catch (MalformedURLException e) {
				throw new NoSuccessException(e);
			} catch (ServiceException e) {
				throw new NoSuccessException(e);
			} catch (InvalidResourcePropertyQNameFaultType e) {
				throw new NoSuccessException(e);
			} catch (RemoteException e) {
				throw new NoSuccessException(e);
			}
    	}
    }

    public void disconnect() throws NoSuccessException {
        m_serverUrl = null;
        m_serverBatch = null;
        m_credential = null;
    }
    
    protected GramJob getGramJobById(String nativeJobId) throws NoSuccessException {
    	GramJob job = new GramJob();
        try {
        	job.setCredentials(m_credential);
            job.setHandle(nativeJobId);
        }
        catch (Exception e) {
        	throw new NoSuccessException("could not find job with endpoint: " + nativeJobId);
        }
    	return job;
    }
}
