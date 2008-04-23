package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.apache.axis.AxisProperties;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.glite.jdl.AdParser;
import org.glite.jdl.JobAdException;
import org.glite.wms.wmproxy.AuthenticationFaultException;
import org.glite.wms.wmproxy.AuthorizationFaultException;
import org.glite.wms.wmproxy.CredentialException;
import org.glite.wms.wmproxy.InvalidArgumentFaultException;
import org.glite.wms.wmproxy.JobUnknownFaultException;
import org.glite.wms.wmproxy.NoSuitableResourcesFaultException;
import org.glite.wms.wmproxy.OperationNotAllowedFaultException;
import org.glite.wms.wmproxy.ServiceException;
import org.glite.wms.wmproxy.ServiceURLException;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyAPI;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobControlAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************/
/**
 * TODO : Support of jsdl:TotalCPUTime, jsdl:OperatingSystemType, jsdl:TotalCPUCount
 * TODO : Test MPI jobs
 */
public class WMSJobControlAdaptor extends WMSJobAdaptorAbstract implements JobControlAdaptor, CleanableJobAdaptor{
    
	private String clientConfigFile = Base.JSAGA_VAR+ File.separator+ "client-config-wms.wsdd";
	private File m_tmpProxyFile;
	private WMProxyAPI m_client;
    private String m_delegationId = "myId";
    private String m_wmsServerUrl;
    private String m_lbServerHost;
    private int m_lbPort;
    
    public String getType() {
        return "wms";
    }

    public int getDefaultPort() {
        return 7443;
    }
    
    public Usage getUsage() {
        return new UAnd(new Usage[]{
        		new UFile(Context.CERTREPOSITORY),
        		new U(MONITOR_PORT)}); 
    }
    
    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
        		new Default(MONITOR_PORT, "9000"),
                new Default(Context.CERTREPOSITORY, new File[]{
                        new File(env.getProperty("X509_CERT_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/certificates/"),
                        new File("/etc/grid-security/certificates/")})
                };
    }

    public String[] getSupportedSandboxProtocols() {
        return null;    // no sandbox management
    }

    public String getTranslator() {
        return "xsl/job/jdl.xsl";
    }

    public Map getTranslatorParameters() {
        return null;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new WMSJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {

    	m_wmsServerUrl = "https://"+host+":"+port+basePath;
    	if(attributes.containsKey(MONITOR_SERVICE_URL)) {
    		// LB server name get in config
    		m_lbServerHost = ((org.ogf.saga.URL) attributes.get(MONITOR_SERVICE_URL)).getHost();
    	}
    	else {
    		// LB And WMS on the same server
    		m_lbServerHost = host;
    	}
    	
    	// get port
    	m_lbPort = Integer.parseInt((String) attributes.get(MONITOR_PORT));
    	
    	// get certificate directory : This solution is temporary
    	String caLoc = (String)attributes.get(Context.CERTREPOSITORY);
    	
        // save proxy file
    	try {
            // save GSSCredential to tmpFile
            m_tmpProxyFile = File.createTempFile("proxy",".proxy");
            FileOutputStream out = new FileOutputStream(m_tmpProxyFile.getAbsolutePath());
            GlobusCredential globusCred = ((GlobusGSSCredentialImpl)m_credential).getGlobusCredential();        
            globusCred.save(out);
            out.close();
        }
        catch (IOException e){
        	throw new AuthenticationFailed(e);        	
        }
        
        try  {
        	
        	// save client-config.wsdd on JSAGA_VAR from jar 
        	if(!new File(clientConfigFile).exists()) {
        		try {
        			InputStream is = this.getClass().getResourceAsStream("/"+new File(clientConfigFile).getName());
            		FileOutputStream fos = new FileOutputStream (new File(clientConfigFile));
    	    		int n;
    	    		while ((n = is.read()) != -1 ){
    	    			fos.write(n);
    	    		}
    	    		fos.close();
    			} catch (FileNotFoundException e) {
    				throw new NoSuccess(e);
    			} catch (IOException e) {
    				throw new NoSuccess(e);
    			}
        	} 
        	AxisProperties.setProperty(EngineConfigurationFactoryDefault.OPTION_CLIENT_CONFIG_FILE,clientConfigFile);

	        // create WMP Client
	    	m_client = new WMProxyAPI (m_wmsServerUrl, m_tmpProxyFile.getAbsolutePath(), caLoc);
	    	String proxy = m_client.getProxyReq (m_delegationId);
            m_client.grstPutProxy(m_delegationId, proxy);
	    	
        } catch (ServiceException e) {
        	disconnect();
        	throw new NoSuccess(e);
        } catch (ServiceURLException e) {
        	disconnect();
			throw new NoSuccess(e);
		} catch (CredentialException e) {
			disconnect();
			throw new AuthenticationFailed(e);
		} catch (AuthenticationFaultException e) {
			disconnect();
			throw new AuthenticationFailed(e);
		} catch (AuthorizationFaultException e) {
			disconnect();
			throw new AuthenticationFailed(e);
		} 
		
		// TODO : use CheckAvailability parameter
        // ping service
        if(false) {
            try {
            	m_client.getVersion();
			} catch (AuthenticationFaultException e) {
				disconnect();
				throw new AuthenticationFailed(e);
			} catch (ServiceException e) {
				disconnect();
				throw new NoSuccess(e);
			}
        }

    }	

	public void disconnect() throws NoSuccess {
		if(m_tmpProxyFile != null &&
				m_tmpProxyFile.exists()) {
			m_tmpProxyFile.delete();
		}
        m_wmsServerUrl = null;
        m_credential = null;
        m_client = null;
    }
    
    public String submit(String jobDesc, boolean checkMatch) 
    	throws PermissionDenied, Timeout, NoSuccess, BadResource {
    	try {
    		
    		//Add LB Address in JDL
			jobDesc += "LBAddress=\""+m_lbServerHost+":"+m_lbPort+"\";";
			
			// parse JDL
			try {
				AdParser.parseJdl(jobDesc);
			} catch (JobAdException e) {
				throw new NoSuccess("The job description is not valid", e);
			}
    		
    		if(checkMatch) {				
				// get available CE
            	StringAndLongList result = m_client.jobListMatch(jobDesc, m_delegationId);            
            	if ( result != null ) {
    				// list of CE
    				StringAndLongType[] list = (StringAndLongType[]) result.getFile ();
    				if (list == null)
                        throw new BadResource("No Computing Element matching your job requirements has been found!");
    			}
            	else 
            		throw new BadResource("No Computing Element matching your job requirements has been found!");
    		}
    		
    		// submit
    		String jobId = m_client.jobSubmit(jobDesc, m_delegationId).getId();
	    	return jobId;
    	} catch (ServiceException e) {
			throw new NoSuccess(e);
		} catch (AuthorizationFaultException e) {
			throw new PermissionDenied(e);
		} catch (AuthenticationFaultException e) {
			throw new PermissionDenied(e);
		} catch (InvalidArgumentFaultException e) {
			throw new NoSuccess(e);
		} catch (NoSuitableResourcesFaultException e) {
			throw new NoSuccess(e);
		}
    }

    public void cancel(String nativeJobId) throws PermissionDenied, Timeout, NoSuccess {
    	try {
	    	// cancel
	    	m_client.jobCancel(nativeJobId);
    	} catch (ServiceException e) {
    		throw new NoSuccess(e);
		} catch (AuthorizationFaultException e) {
			throw new PermissionDenied(e);
		} catch (AuthenticationFaultException e) {
			throw new PermissionDenied(e);
		} catch (OperationNotAllowedFaultException e) {
			throw new PermissionDenied(e);
		} catch (InvalidArgumentFaultException e) {
			throw new NoSuccess(e);
		} catch (JobUnknownFaultException e) {
			throw new NoSuccess(e);
		}
    }

	public void clean(String nativeJobId) throws PermissionDenied, Timeout,
			NoSuccess {
        try  {
	    	// purge        
	    	m_client.jobPurge(nativeJobId);
        } catch (AuthenticationFaultException e) {
			throw new PermissionDenied(e);
		} catch (AuthorizationFaultException e) {
			throw new PermissionDenied(e);
		} catch (org.glite.wms.wmproxy.ServiceException e) {
			throw new NoSuccess(e);
		} catch (OperationNotAllowedFaultException e) {
			throw new PermissionDenied(e);
		} catch (InvalidArgumentFaultException e) {
			throw new NoSuccess(e);
		} catch (JobUnknownFaultException e) {
			throw new NoSuccess(e);
		}
	}
}
