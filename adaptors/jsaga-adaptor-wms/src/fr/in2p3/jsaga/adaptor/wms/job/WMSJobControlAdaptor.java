package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
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
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyException;
import org.globus.util.GlobusURL;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private static final String DEFAULT_JDL_FILE = "DefaultJdlFile";

	private Logger logger = Logger.getLogger(WMSJobControlAdaptor.class);
	
	private String clientConfigFile = Base.JSAGA_VAR+ File.separator+ "client-config-wms.wsdd";
	private File m_tmpProxyFile,  stdoutFile, stderrFile;
	
    private Map m_parameters;
	private WMProxyAPI m_client;
    private String m_delegationId = "myId";
    private String m_wmsServerUrl;
    private String m_lbServerUrl;
    private boolean m_isInteractive;

    public String getType() {
        return "wms";
    }

    public int getDefaultPort() {
        return 7443;
    }
    
    public Usage getUsage() {
        return new UOr(new U[]{
                new U(MONITOR_PORT),
                new UOptional(DEFAULT_JDL_FILE),
                // JDL attributes
                new UOptional("requirements"),
                new UOptional("rank"),
                new UOptional("virtualorganisation"),
                new UOptionalInteger("RetryCount"),
                new UOptionalInteger("ShallowRetryCount"),
                new UOptional("OutputStorage"),
                new UOptional("ErrorStorage"),
                new UOptionalBoolean("AllowZippedISB"),
                new UOptionalBoolean("PerusalFileEnable"),
                new UOptional("ListenerStorage"),
                new UOptional("MyProxyServer")
        });
    }
    
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                new Default(MONITOR_PORT, "9000"),
                // JDL attributes
                new Default("requirements", "(other.GlueCEStateStatus==\"Production\")"),
                new Default("rank", "(-other.GlueCEStateEstimatedResponseTime)")
        };
    }

    public String[] getSupportedSandboxProtocols() {
        return new String[]{"file","gsiftp"};    // no sandbox management
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

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        m_parameters = attributes;
        if (attributes.containsKey(DEFAULT_JDL_FILE)) {
            File defaultJdlFile = new File((String) attributes.get(DEFAULT_JDL_FILE));
            try {
                // may override jsaga-universe.xml attributes
                new DefaultJDL(defaultJdlFile).fill(m_parameters);
            } catch (FileNotFoundException e) {
                throw new BadParameterException(e);
            }
        }

    	m_wmsServerUrl = "https://"+host+":"+port+basePath;
    	if(attributes.containsKey(MONITOR_SERVICE_URL)) {
    		// LB server name get in config
    		URL lbUrl = (URL) attributes.get(MONITOR_SERVICE_URL);
            m_lbServerUrl = lbUrl.getHost() + ":" + (lbUrl.getPort()>0 ? ""+lbUrl.getPort() : attributes.get(MONITOR_PORT));
    	} else {
            // LB server will be extracted from jobid
            m_lbServerUrl = null;
        }

    	// get certificate directory
        if (m_certRepository == null) {
            throw new NoSuccessException("Configuration attribute not found in context: "+Context.CERTREPOSITORY);
        } else if (!m_certRepository.isDirectory()) {
            throw new NoSuccessException("Directory not found: "+m_certRepository);
        }
    	
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
	    	m_client = new WMProxyAPI (m_wmsServerUrl, m_tmpProxyFile.getAbsolutePath(), m_certRepository.getAbsolutePath());
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
        m_parameters = null;
        m_wmsServerUrl = null;
        m_credential = null;
        m_client = null;
    }
    
    public String submit(String jobDesc, boolean checkMatch, String uniqId)
    	throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
        m_isInteractive = false;
    	try {
            // put lbServerUrl in JDL
    		if (m_lbServerUrl != null) {
			    jobDesc += "LBAddress=\""+m_lbServerUrl+"\";";
            }
			
			// parse JDL and Check Matching
			checkJDLAndMAtch(jobDesc, checkMatch, m_client);

            String jobId;
            if (jobDesc.contains("InputSandbox")) {
                // get input files from jobDesc
                Properties prop = new Properties();
                try {
                    prop.load(new ByteArrayInputStream(jobDesc.getBytes()));
                } catch (IOException e) {
                    throw new NoSuccessException("Unable to parse native job description: "+jobDesc);
                }
                String inSbx = prop.getProperty("InputSandbox");
                inSbx = inSbx.substring(1, inSbx.length()-2).replaceAll("\"", "");
                String[] array = inSbx.split(", ");
                File[] inputFiles = new File[array.length];
                for (int i=0; i<array.length; i++) {
                    inputFiles[i] = new File(array[i]);
                    if (! inputFiles[i].exists()) {
                        throw new NoSuccessException("File not found: "+inputFiles[i]);
                    }
                }

                // register job
                jobId = m_client.jobRegister(jobDesc, m_delegationId).getId();
                if(logger.isDebugEnabled())
                    logger.debug("Id for job:"+jobId);

                // upload input files to the sandbox associated to the registered job
                StringList list = m_client.getSandboxDestURI(jobId, "gsiftp");
                if(list == null  || list.getItem() == null || list.getItem().length < 1) {
                    throw new NoSuccessException("Unable to find a sandbox dest uri");
                }
                for (int i=0; i<inputFiles.length; i++) {
                    File inputFile = inputFiles[i];
                    try {
                        String[] uriList = list.getItem();
                        GlobusURL to = new GlobusURL(uriList[0]+"/"+inputFile.getName());
                        GridFTPClient test = new GridFTPClient(to.getHost(), to.getPort());
                        test.authenticate(m_credential);
                        test.put(inputFile, "/"+to.getPath(), true);
                    } catch(Exception e) {
                        throw new NoSuccessException("Unable to stage input file: "+inputFile,e);
                    }
                }

                // set LB from jobId
                if (m_lbServerUrl == null) {
                    WMStoLB.getInstance().setLBHost(m_wmsServerUrl, jobId);
                }

                // start job
                m_client.jobStart(jobId);
            } else {
                // submit
                jobId = m_client.jobSubmit(jobDesc, m_delegationId).getId();
                if(logger.isDebugEnabled())
                    logger.debug("Id for job:"+jobId);

                // set LB from jobId
                if (m_lbServerUrl == null) {
                    WMStoLB.getInstance().setLBHost(m_wmsServerUrl, jobId);
                }

            }
	    	return jobId;
        } catch (NoSuccessException e) {
            throw e;
		} catch (AuthorizationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (AuthenticationFaultException e) {
			throw new PermissionDeniedException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
        }
    }
    


	public JobIOHandler submit(String jobDesc, boolean checkMatch,
                               String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		m_isInteractive = true;
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

            // set LB from jobId
            if (m_lbServerUrl == null) {
                WMStoLB.getInstance().setLBHost(m_wmsServerUrl, jobId);
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
            if (m_isInteractive) {
                if(stdoutFile != null && stdoutFile.exists()) {
                    stdoutFile.delete(); // fixme: file not deleted!
                }
                if(stderrFile != null && stderrFile.exists()) {
                    stderrFile.delete(); // fixme: file not deleted!
                }
                if(m_tmpProxyFile != null && m_tmpProxyFile.exists()) {
                    m_tmpProxyFile.delete(); // fixme: file not deleted!
                }
            } else {
                //Use the "gsiftp" transfer protocols to retrieve the list of files produced by the jobs.
                StringAndLongList result = m_client.getOutputFileList(nativeJobId, "gsiftp");
                if ( result != null )
                {
                    //Retrieve the file(s) from the WMProxy Server.
                    StringAndLongType[] list = result.getFile();
                    for (int i=0; list!=null && i<list.length ; i++){
                        String from = list[i].getName();
                        File to = new File(new File(from).getName());
                        GlobusURL fromURL = createGlobusURL(from);
                        GlobusURL toURL = createGlobusURL(to);

                        UrlCopy uCopy = new UrlCopy();
                        uCopy.setDestinationCredentials(m_credential);
                        uCopy.setSourceCredentials(m_credential);
                        uCopy.setDestinationUrl(toURL);
                        uCopy.setSourceUrl(fromURL);
                        try {
                            logger.info("Downloading output: "+from);
                            uCopy.copy();
                        } catch (UrlCopyException e) {
                            throw new NoSuccessException("Failed to download output: "+from, e);
                        }
                    }
                }
            }

            // purge
            try {
                m_client.jobPurge(nativeJobId);
            } catch(Exception  e) {/*ignore*/}
        } catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

    private static GlobusURL createGlobusURL(String url) throws NoSuccessException {
        Matcher m = Pattern.compile("(\\w+)://([\\w-.]+(:\\d+)?)(/.*)").matcher(url);
        if (!m.matches() || m.groupCount()!=4) {
            throw new NoSuccessException("Malformed URL: "+url);
        }
        String scheme = m.group(1);
        String host = m.group(2);
        String path = m.group(4);
        // path must start with '//'
        String fixedUrl = scheme+"://"+host+"/"+path;
        try {
            return new GlobusURL(fixedUrl);
        } catch (MalformedURLException e) {
            throw new NoSuccessException("Malformed URL: "+fixedUrl);
        }
    }

    private static GlobusURL createGlobusURL(File file) throws NoSuccessException {
        try {
            return new GlobusURL(file.toURL());
        } catch (MalformedURLException e) {
            throw new NoSuccessException("[INTERNAL ERROR] Unexpected exception", e);
        }
    }
}
