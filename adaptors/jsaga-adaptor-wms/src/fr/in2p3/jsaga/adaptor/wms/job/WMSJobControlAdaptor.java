package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
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
import org.globus.common.CoGProperties;
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


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobControlAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
/**
 *
 */
public class WMSJobControlAdaptor extends WMSJobAdaptorAbstract implements JobControlAdaptor {
    
    private Map m_parameters;
    private WMProxyAPI m_client;
    private String m_delegationId = "myId";
    
    public String getType() {
        return "wms";
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	return null;    // no default
    }

    public String[] getSupportedSandboxProtocols() {
        return null;    // no sandbox management
    }

    public String getTranslator() {
        return "xsl/job/jdl.xsl";
    }

    public Map getTranslatorParameters() {
        return m_parameters;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new WMSJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        super.connect(userInfo, host, port, basePath, attributes);
        m_parameters = attributes;
        
        // get certificate directory
    	System.out.println("Ca Cert location :"+(String)attributes.get(Context.CERTREPOSITORY));
    	String caLoc = CoGProperties.getDefault().getCaCertLocations();
    	
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
        
    	// save client-config.wsdd on JSAGA_VAR from jar 
        try  {
        	String clientConfigFile = Base.JSAGA_VAR+ File.separator+ "client-config-wms.wsdd";
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
	        // create WMP Client
        	AxisProperties.setProperty(EngineConfigurationFactoryDefault.OPTION_CLIENT_CONFIG_FILE,clientConfigFile);			
	    	m_client = new WMProxyAPI (m_serverUrl, m_tmpProxyFile.getAbsolutePath(), caLoc);
	    	
	    	// put proxy
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
        // ping service
        if(false) {
            try {
            	//System.out.println("WMS Version :"+m_client.getVersion());
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
        m_parameters = null;
        super.disconnect();        
    }

    public String submit(String jobDesc) throws PermissionDenied, Timeout, NoSuccess {
    	try {
    		
    		//System.out.println("WMS Job description:"+jobDesc);
            // parse
    		if(true) {
	    		try {
					AdParser.parseJdl(jobDesc);
				} catch (JobAdException e) {
					throw new NoSuccess(e);
				}
    		}
    		// verify that at least one CE is matching
    		if(true) {
            	StringAndLongList result = m_client.jobListMatch(jobDesc, m_delegationId);            
            	if ( result != null ) {
    				// list of CE's+their ranks
    				StringAndLongType[] list = (StringAndLongType[]) result.getFile ();
    				if (list != null) {
                        System.out.println(list.length +" Computing Elements matching your job requirements had been found :");
    				} 
    				else 
    					throw new NoSuccess("No Computing Element matching your job requirements has been found!");
    			}
            	else 
            		throw new NoSuccess("No Computing Element matching your job requirements has been found!");
    		}
    		// submit
	    	String jobId = m_client.jobSubmit(jobDesc, m_delegationId).getId();
	    	System.out.println("WMS ID:"+jobId);
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
}
