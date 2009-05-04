package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.axis.AxisProperties;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.apache.log4j.Logger;
import org.glite.jdl.AdParser;
import org.glite.jdl.JobAdException;
import org.glite.wms.wmproxy.*;
import org.globus.ftp.GridFTPClient;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.GlobusURL;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.io.*;
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
 * TODO : Support of space in environment value
 * TODO : Test MPI jobs
 */
public class WMSJobControlAdaptor extends WMSJobAdaptorAbstract 
		implements JobControlAdaptor, CleanableJobAdaptor, StreamableJobBatch {

	private Logger logger = Logger.getLogger(WMSJobControlAdaptor.class);
	
	private String clientConfigFile = Base.JSAGA_VAR+ File.separator+ "client-config-wms.wsdd";
	private File m_tmpProxyFile,  stdoutFile, stderrFile;
	
	private WMProxyAPI m_client;
    private String m_delegationId = "myId";
    private String m_wmsServerUrl;
    private String m_lbServerUrl;
    
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
    
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
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

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {

    	m_wmsServerUrl = "https://"+host+":"+port+basePath;
    	if(attributes.containsKey(MONITOR_SERVICE_URL)) {
    		// LB server name get in config
    		URL lbUrl = (URL) attributes.get(MONITOR_SERVICE_URL);
            m_lbServerUrl = lbUrl.getHost() + ":" + (lbUrl.getPort()>0 ? lbUrl.getPort() : attributes.get(MONITOR_PORT));
    	} else {
            // LB server will be extracted from jobid
            m_lbServerUrl = null;
        }

    	// get certificate directory : This solution is temporary
    	String caLoc = (String)attributes.get(Context.CERTREPOSITORY);
    	
        // save proxy file
    	try {
            // save GSSCredential to tmpFile
            m_tmpProxyFile = File.createTempFile("proxy",".proxy");
            m_tmpProxyFile.deleteOnExit();
            FileOutputStream out = new FileOutputStream(m_tmpProxyFile.getAbsolutePath());
            GlobusCredential globusCred = ((GlobusGSSCredentialImpl)m_credential).getGlobusCredential();        
            globusCred.save(out);
            out.close();
        }
        catch (IOException e){
        	throw new AuthenticationFailedException(e);
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
    				throw new NoSuccessException(e);
    			} catch (IOException e) {
    				throw new NoSuccessException(e);
    			}
        	} 
        	AxisProperties.setProperty(EngineConfigurationFactoryDefault.OPTION_CLIENT_CONFIG_FILE,clientConfigFile);

	        // create WMP Client
	    	m_client = new WMProxyAPI (m_wmsServerUrl, m_tmpProxyFile.getAbsolutePath(), caLoc);
	    	String proxy = m_client.getProxyReq (m_delegationId);
            m_client.grstPutProxy(m_delegationId, proxy);
        } catch (ServerOverloadedFaultException e) {
            disconnect();
            throw new NoSuccessException(e);
        } catch (ServiceException e) {
        	disconnect();
        	throw new NoSuccessException(e);
        } catch (ServiceURLException e) {
        	disconnect();
			throw new NoSuccessException(e);
		} catch (CredentialException e) {
			disconnect();
			throw new AuthenticationFailedException(e);
		} catch (AuthenticationFaultException e) {
			disconnect();
			throw new AuthenticationFailedException(e);
		} catch (AuthorizationFaultException e) {
			disconnect();
			throw new AuthenticationFailedException(e);
		} 
		
        // ping service
		if("true".equalsIgnoreCase((String) attributes.get(JobAdaptor.CHECK_AVAILABILITY))) {
            try {
            	m_client.getVersion();
            } catch (ServerOverloadedFaultException e) {
                disconnect();
                throw new NoSuccessException(e);
			} catch (AuthenticationFaultException e) {
				disconnect();
				throw new AuthenticationFailedException(e);
			} catch (ServiceException e) {
				disconnect();
				throw new NoSuccessException(e);
			}
        }

    }	

	public void disconnect() throws NoSuccessException {
        m_wmsServerUrl = null;
        m_credential = null;
        m_client = null;
    }
    
    public String submit(String jobDesc, boolean checkMatch, String uniqId)
    	throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	try {
            // put lbServerUrl in JDL
    		if (m_lbServerUrl != null) {
			    jobDesc += "LBAddress=\""+m_lbServerUrl+"\";";
            }
			
			// parse JDL and Check Matching
			checkJDLAndMAtch(jobDesc, checkMatch, m_client);
			
    		// submit
    		String jobId = m_client.jobSubmit(jobDesc, m_delegationId).getId();
            if(logger.isDebugEnabled())
            	logger.debug("Id for job:"+jobId);

            // set lbServerUrl from jobId
            if (m_lbServerUrl == null) {
                WMSJobMonitorAdaptor.setLBServerUrl(m_wmsServerUrl, jobId);
            }
	    	return jobId;
        } catch (ServerOverloadedFaultException e) {
            throw new NoSuccessException(e);
    	} catch (ServiceException e) {
			throw new NoSuccessException(e);
		} catch (AuthorizationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (AuthenticationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (InvalidArgumentFaultException e) {
			throw new NoSuccessException(e);
		} catch (NoSuitableResourcesFaultException e) {
			throw new NoSuccessException(e);
		}
    }
    


	public JobIOHandler submit(String jobDesc, boolean checkMatch,
                               String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		
		try {
            // put lbServerUrl in JDL
			if (m_lbServerUrl != null) {
			    jobDesc += "LBAddress=\""+m_lbServerUrl+"\";";
            }
						
			// add stdout/stderr
			stdoutFile = File.createTempFile("stdout", ".txt");
			stderrFile = File.createTempFile("stderr", ".txt");
			
			jobDesc += "StdOutput=\""+stdoutFile.getName()+"\";";
			jobDesc += "StdError=\""+stderrFile.getName()+"\";";
			jobDesc += "OutputSandbox={\""+stdoutFile.getName()+"\",\""+stderrFile.getName()+"\"};";

			String jobId = null;
			
			if(stdin != null ) {
				// add stdin
				File stdinFile = File.createTempFile("stdin", ".in");
				jobDesc += "InputSandbox={\""+stdinFile.getName()+"\"};";
				jobDesc += "StdInput=\""+stdinFile.getName()+"\";";			
			
				// parse JDL and Check Matching
				checkJDLAndMAtch(jobDesc, checkMatch, m_client);

				// register job
				jobId = m_client.jobRegister(jobDesc, m_delegationId).getId();

				// create stdin tmp file
				FileOutputStream fos = new FileOutputStream(stdinFile);
				byte buf[]=new byte[1024];
			    int len;
			    while((len=stdin.read(buf))>0)
			    	fos.write(buf,0,len);
			    fos.close();
			    
			    // upload input file to the sandbox associated to the registered job 
				StringList list = m_client.getSandboxDestURI(jobId, "gsiftp");
				if(list == null  || list.getItem() == null || list.getItem().length < 1) {
					throw new NoSuccessException("Unable to find a input sandbox uri to put stdin file");
				}				
				
				try {
					String[] uriList = list.getItem();
					GlobusURL to = new GlobusURL(uriList[0]+"/"+stdinFile.getName());
		            GridFTPClient test = new GridFTPClient(to.getHost(), to.getPort());
		            test.authenticate(m_credential);
		            test.put(stdinFile, "/"+to.getPath(), true);
				}
				catch(Exception e) {
					throw new NoSuccessException("Unable to upload stdin file to input sandbox",e);
				}
				finally {
					// clean stdin tmp file
					stdinFile.delete();
				}

			}
			else {
				// parse JDL and Check Matching
				checkJDLAndMAtch(jobDesc, checkMatch, m_client);

				// register job
				jobId = m_client.jobRegister(jobDesc, m_delegationId).getId();
			}

            // set lbServerUrl from jobId
            if (m_lbServerUrl == null) {
                WMSJobMonitorAdaptor.setLBServerUrl(m_wmsServerUrl, jobId);
            }
			
			// start job
            m_client.jobStart(jobId);
            if(logger.isDebugEnabled())
            	logger.debug("Id for job:"+jobId);
			return new WMSJobIOHandler(jobId, m_client, m_credential, stdoutFile.getAbsolutePath(), stderrFile.getAbsolutePath());
        } catch (NoSuccessException e) {
            throw e;
		} catch (AuthorizationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (AuthenticationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch(Exception e) {
			throw new NoSuccessException(e);
		}
	}

    private void checkJDLAndMAtch(String jobDesc, boolean checkMatch, WMProxyAPI m_client2)
            throws NoSuccessException, AuthorizationFaultException, AuthenticationFaultException, InvalidArgumentFaultException, NoSuitableResourcesFaultException, ServiceException, ServerOverloadedFaultException
    {
		// parse JDL
		try {
			AdParser.parseJdl(jobDesc);
		} catch (JobAdException e) {
			throw new NoSuccessException("The job description is not valid", e);
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

	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	try {
	    	// cancel
	    	m_client.jobCancel(nativeJobId);
        } catch (ServerOverloadedFaultException e) {
            throw new NoSuccessException(e);
    	} catch (ServiceException e) {
    		throw new NoSuccessException(e);
		} catch (AuthorizationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (AuthenticationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (OperationNotAllowedFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (InvalidArgumentFaultException e) {
			throw new NoSuccessException(e);
		} catch (JobUnknownFaultException e) {
			throw new NoSuccessException(e);
		}
    }

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException,
            NoSuccessException {
        try  {
	    	// purge
        	try {
        		m_client.jobPurge(nativeJobId);
        	}
        	catch(Exception  e) {
        	}
	    	if(stdoutFile != null &&
	    			stdoutFile.exists()) {
	    		stdoutFile.delete();
				// fixme: file not deleted!
	    	}
	    	if(stderrFile != null &&
	    			stderrFile.exists()) {
	    		stderrFile.delete();
				// fixme: file not deleted!
			}
			if(m_tmpProxyFile != null &&
					m_tmpProxyFile.exists()) {
				m_tmpProxyFile.delete();
				// fixme: file not deleted!
			}
        } catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
}
