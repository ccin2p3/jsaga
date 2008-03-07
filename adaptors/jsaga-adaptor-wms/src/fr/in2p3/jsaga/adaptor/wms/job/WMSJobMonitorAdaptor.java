package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

import org.apache.axis.AxisProperties;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.glite.lb.LoggingAndBookkeepingLocatorClient;
import org.glite.wms.wmproxy.AuthenticationFaultException;
import org.glite.wms.wmproxy.AuthorizationFaultException;
import org.glite.wms.wmproxy.CredentialException;
import org.glite.wms.wmproxy.ServiceURLException;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyAPI;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingLocator;
import org.glite.wsdl.services.lb.LoggingAndBookkeepingPortType;
import org.glite.wsdl.types.lb.GenericFault;
import org.glite.wsdl.types.lb.JobFlags;
import org.glite.wsdl.types.lb.JobFlagsValue;
import org.glite.wsdl.types.lb.StatName;
import org.globus.axis.transport.HTTPSSender;
import org.globus.axis.util.Util;
import org.globus.common.CoGProperties;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.util.GlobusURL;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.rpc.ServiceException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WMSJobMonitorAdaptor extends WMSJobAdaptorAbstract implements QueryIndividualJob {
		
    public Usage getUsage() {
        return null;    // no usage
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }
    
    public JobStatus getStatus(String nativeJobId) throws Timeout, NoSuccess {
    	
    	try {

    		URL jobUrl = new URL(nativeJobId);
	        URL lbURL = new URL(jobUrl.getProtocol(), jobUrl.getHost(), jobUrl.getPort() , "");  
	        
	        // Set provider
	        SimpleProvider provider = new SimpleProvider();
	        SimpleTargetedChain c = null;
	        c = new SimpleTargetedChain(new HTTPSSender());
	        provider.deployTransport("https",c);
	        c = new SimpleTargetedChain(new HTTPSender());
	        provider.deployTransport("http",c);
	        Util.registerTransport();
	        
	        // get LB Stub
	        LoggingAndBookkeepingLocator loc = new LoggingAndBookkeepingLocatorClient(provider, m_credential);
	        LoggingAndBookkeepingPortType stub = loc.getLoggingAndBookkeeping(lbURL);
	        
	        // get LB Version
	        //System.out.println("LB Version :"+stub.getVersion(null));
	        
	        // get job Status
	        JobFlagsValue[] jobFlagsValue = new JobFlagsValue[1];
	        jobFlagsValue[0] = JobFlagsValue.CLASSADS;
	        JobFlags jobFlags = new JobFlags(jobFlagsValue);
	        org.glite.wsdl.types.lb.JobStatus jobState = stub.jobStatus(nativeJobId,jobFlags );
	        if(jobState == null) {
	            throw new NoSuccess("Unable to get status for job:"+nativeJobId);
	        }
	        System.out.println("Status:"+jobState.getState().getValue());
	        
	        // TODO : move to cleanup step
	        if(jobState.getState().getValue().equals(StatName._DONE) || 
	        		jobState.getState().getValue().equals(StatName._ABORTED)) {
	        	try {
	        		
	        		//get Stdout/stderr and purge	        		
					cleanUpJob(nativeJobId);
					
			        /*try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
					
					// must return cleanUp state
			        jobState = stub.jobStatus(nativeJobId,jobFlags );
			        System.out.println("Status after clean:"+jobState.getState().getValue());
			        
			        // clean proxy
			        if(m_tmpProxyFile != null &&
			        		m_tmpProxyFile.exists()) {
			        	m_tmpProxyFile.delete();
			        }
				} catch (AuthenticationFailed e) {
					System.err.println("Unable to clean job :"+e.getMessage());
					e.printStackTrace();
				} catch (NoSuccess e) {
					System.err.println("Unable to clean job :"+e.getMessage());
					e.printStackTrace();
				}
	        }
	        
	        return new WMSJobStatus(nativeJobId,jobState.getState(), jobState.getState().getValue());
    	}
    	catch (MalformedURLException e) {
    		throw new NoSuccess(e);
    	} catch (ServiceException e) {
    		throw new NoSuccess(e);
		} catch (GenericFault e) {
			throw new NoSuccess(e);
		} catch (RemoteException e) {
			throw new NoSuccess(e);
		}
    }

	private void cleanUpJob(String jobId) throws AuthenticationFailed, NoSuccess {
		String caLoc = CoGProperties.getDefault().getCaCertLocations();
    	File m_tmpProxyFile;
    	String m_delegationId = "idToClean";
    	
        // save proxy file
        try {
            // save GSSCredential to tmpFile
            m_tmpProxyFile = File.createTempFile("proxyBis",".proxy");
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
        	WMProxyAPI m_client = new WMProxyAPI (m_serverUrl, m_tmpProxyFile.getAbsolutePath(), caLoc);
	    	
	    	// put proxy
	    	String proxy = m_client.getProxyReq (m_delegationId);
            m_client.grstPutProxy(m_delegationId, proxy);
            
            try {
				String gliteLogDir = rootLogDir + new URL(jobId).getPath() + File.separator;
				
	            //Use the "gsiftp" transfer protocols to retrieve the list of files produced by the jobs.
	            StringAndLongList result = m_client.getOutputFileList(jobId, "gsiftp");        
	            if ( result != null ) 
	            {
	                // list of files+their size
	                StringAndLongType[ ] list = (StringAndLongType[ ]) result.getFile();
	                
	                if (list != null)
	                {
	                    int size = list.length;                    
	                    for (int i=0; i<size ; i++){
	                        String filename = new File(list[i].getName()).getName();
	                        
	                        // Creation of the "fromURL" link from where download the file(s).
	                        String port = list[i].getName().substring(list[i].getName().lastIndexOf(":")+1,list[i].getName().length());
	                        port = port.substring(0, port.indexOf("/"));                        
	                        int pos = (list[i].getName()).indexOf(port);
	                        int length = (list[i].getName()).length();                      
	                        String front = (list[i].getName()).substring(0 , pos);
	                        String rear = (list[i].getName()).substring(pos + 4 , length);                      
	                        String fromURL = front + port + "/" + rear;
	                        
	                        try {                       
	    
	                            // Initialize the toURL string.
	                            if(!new File(gliteLogDir).exists()) {
	                                File logDir = new File(gliteLogDir);
	                                logDir.mkdirs();
	                            }
	                            String toURL = "file:///" + gliteLogDir + filename ;
	                            
	                            /***********************************************            
	                                Retrieve the file(s) from the 
	                                    WMProxy Server.
	                            *************************************************/                       
	                            GlobusURL from = new GlobusURL(fromURL);
	                            GlobusURL to = new GlobusURL(toURL);
	                            
	                            UrlCopy uCopy = new UrlCopy();
	                            uCopy.setDestinationCredentials(m_credential);
	                            uCopy.setSourceCredentials(m_credential);
	                            uCopy.setDestinationUrl(to);
	                            uCopy.setSourceUrl(from);
	                            uCopy.setUseThirdPartyCopy(true);
	                            uCopy.copy();
	                                                    
	                        } catch (Exception e) {
	                        	 System.err.println("Unable to get file "+filename + ":"+ e.getMessage());
	                        }                     
	                    }
	                }
	            }
	            // purge        
	            m_client.jobPurge(jobId);
			}
			catch (Exception e) {
				throw new NoSuccess(e);
			}
        } catch (ServiceURLException e) {
        	throw new NoSuccess(e);
		} catch (CredentialException e) {
			throw new AuthenticationFailed(e);
		} catch (AuthenticationFaultException e) {
			throw new AuthenticationFailed(e);
		} catch (AuthorizationFaultException e) {
			throw new AuthenticationFailed(e);
		} catch (org.glite.wms.wmproxy.ServiceException e) {
			throw new NoSuccess(e);
		} 
		finally {
			System.out.println("Clean proxy file");
			// clean proxy
			if(m_tmpProxyFile != null && 
					m_tmpProxyFile.exists()) {
				m_tmpProxyFile.delete();
			}
		}
	}

}
