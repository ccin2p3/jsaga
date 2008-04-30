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
import fr.in2p3.jsaga.adaptor.job.control.interactive.InteractiveJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetter;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

import org.apache.axis.AxisProperties;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.apache.log4j.Logger;
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
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyException;
import org.globus.util.GlobusURL;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobControlAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************/
/**
 * TODO : Support of jsdl:TotalCPUTime, jsdl:OperatingSystemType, jsdl:TotalCPUCount, jsdl:CPUArchitecture
 * TODO : Test MPI jobs
 */
public class WMSJobControlAdaptor extends WMSJobAdaptorAbstract 
		implements JobControlAdaptor, CleanableJobAdaptor{
    //, InteractiveJobAdaptor, JobIOGetter 
	private String clientConfigFile = Base.JSAGA_VAR+ File.separator+ "client-config-wms.wsdd";
	private File m_tmpProxyFile;
	private WMProxyAPI m_client;
    private String m_delegationId = "myId";
    private String m_wmsServerUrl;
    private String m_lbServerHost;
    private int m_lbPort;
    private String jobId;
    
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
    		jobId = m_client.jobSubmit(jobDesc, m_delegationId).getId();
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

	/*public JobIOHandler submitInteractive(String jobDesc, boolean checkMatch)
			throws PermissionDenied, Timeout, NoSuccess {
		// Add Stdout/Stderr/OutputSandbox attributes to JDL
		jobDesc += "StdOutput=\"stdout.txt\";";
		jobDesc += "StdError=\"stderr.txt\";";
		jobDesc += "OutputSandbox={\"stdout.txt\",\"stderr.txt\"};";		
		jobId = submit(jobDesc,checkMatch);
		return this;
	}*/

	public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess {
		try {
			return new FileInputStream(getSandboxFile("stderr.txt"));
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
	}

	public OutputStream getStdin() throws PermissionDenied, Timeout, NoSuccess {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess {
		try {
			return new FileInputStream(getSandboxFile("stdout.txt"));
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
	}

	public String getJobId() {
		return jobId;
	}
	
	private File getSandboxFile(String filename) throws NoSuccess, PermissionDenied {
		
		try {
			// create tmp file
			File resultFile = File.createTempFile("sandbox",".txt");
			resultFile.deleteOnExit();
			
			//Use the "gsiftp" transfer protocols to retrieve the list of files produced by the jobs.
	        StringAndLongList result = m_client.getOutputFileList(jobId, "gsiftp");
	        if ( result != null ) 
	        {
	            // list of files+their size
	            StringAndLongType[] list = (StringAndLongType[]) result.getFile();            
	            if (list != null) {
	                for (int i=0; i<list.length ; i++){
	                	//TODO
	                	System.out.println("Get file:"+list[i].getName());
	                    if(filename.equals(list[i].getName())) {
		                    // Creation of the "fromURL" link from where download the file(s).
		                    String port = list[i].getName().substring(list[i].getName().lastIndexOf(":")+1,list[i].getName().length());
		                    port = port.substring(0, port.indexOf("/"));                        
		                    int pos = (list[i].getName()).indexOf(port);
		                    int length = (list[i].getName()).length();                      
		                    String front = (list[i].getName()).substring(0 , pos);
		                    String rear = (list[i].getName()).substring(pos + 4 , length);                      
		                    String fromURL = front + port + "/" + rear;
		                    
		                    String toURL = "file:///" + resultFile.getAbsolutePath();	                        
	                        //Retrieve the file(s) from the WMProxy Server.
	                        GlobusURL from = new GlobusURL(fromURL);
	                        GlobusURL to = new GlobusURL(toURL);
	                        
	                        UrlCopy uCopy = new UrlCopy();
	                        uCopy.setDestinationCredentials(m_credential);
	                        uCopy.setSourceCredentials(m_credential);
	                        uCopy.setDestinationUrl(to);
	                        uCopy.setSourceUrl(from);
	                        uCopy.setUseThirdPartyCopy(true);
	                        uCopy.copy();
	                    }
	                }
	            }
	        }
	        return resultFile;
		} catch( UrlCopyException e) {
			throw new NoSuccess(e);
		} catch (MalformedURLException e) {
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
		} catch (ServiceException e) {
			throw new NoSuccess(e);
		} catch (IOException e) {
			throw new NoSuccess(e);
		}
	}	
}
