package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import org.apache.axis.AxisProperties;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.glite.jdl.AdParser;
import org.glite.jdl.JobAdException;
import org.glite.wms.wmproxy.*;
import org.globus.ftp.GridFTPClient;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.Map;
import java.util.Properties;


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
		implements StagingJobAdaptorTwoPhase, CleanableJobAdaptor, StreamableJobBatch {
    private static final String HOST_NAME = "HostName";
    private static final String DEFAULT_JDL_FILE = "DefaultJdlFile";

	private String clientConfigFile = Base.JSAGA_VAR+ File.separator+ "client-config-wms.wsdd";
	private File m_tmpProxyFile;
	
    private Map m_parameters;
	private WMProxyAPI m_client;
    private String m_delegationId = "myId";
    private String m_wmsServerHost;
    private String m_wmsServerUrl;

    public String getType() {
        return "wms";
    }

    public int getDefaultPort() {
        return 7443;
    }
    
    public Usage getUsage() {
        return new UOr(new U[]{
                new UOptional(DEFAULT_JDL_FILE),
                // JDL attributes
                new UOptional("LBAddress"),
                new UOptional("requirements"),
                new UOptional("rank"),
                new UOptional("virtualorganisation"),
                new UOptionalInteger("RetryCount"),
                new UOptionalInteger("ShallowRetryCount"),
                new UOptional("OutputStorage"),
                new UOptionalBoolean("AllowZippedISB"),
                new UOptionalBoolean("PerusalFileEnable"),
                new UOptional("ListenerStorage"),
                new UOptional("MyProxyServer")
        });
    }
    
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                // JDL attributes
                new Default("requirements", "(other.GlueCEStateStatus==\"Production\")"),
                new Default("rank", "(-other.GlueCEStateEstimatedResponseTime)")
        };
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
        m_parameters.put(HOST_NAME, host);
        if (attributes.containsKey(DEFAULT_JDL_FILE)) {
            File defaultJdlFile = new File((String) attributes.get(DEFAULT_JDL_FILE));
            try {
                // may override jsaga-universe.xml attributes
                new DefaultJDL(defaultJdlFile).fill(m_parameters);
            } catch (FileNotFoundException e) {
                throw new BadParameterException(e);
            }
        }

        // set WMS url
        m_wmsServerHost = host;
    	m_wmsServerUrl = "https://"+host+":"+port+basePath;

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
    
    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	try {
			// parse JDL and Check Matching
			checkJDLAndMAtch(jobDesc, checkMatch, m_client);

            // register job
            String nativeJobId = m_client.jobRegister(jobDesc, m_delegationId).getId();

            // set LB from nativeJobId
            if (! m_parameters.containsKey("LBAddress")) {
                WMStoLB.getInstance().setLBHost(m_wmsServerUrl, nativeJobId);
            }
	    	return nativeJobId;
        } catch (BaseException e) {
            rethrow(e);
            return null;    // dead code
        }
    }
    
    private String m_stagingPrefix;
	public JobIOHandler submit(String jobDesc, boolean checkMatch, String uniqId, InputStream stdin) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        m_stagingPrefix = "/tmp/"+uniqId;

        // connect to gsiftp
        GridFTPClient stagingClient;
        try {
            stagingClient = new GridFTPClient(m_wmsServerHost, 2811);
            stagingClient.authenticate(m_credential);
        } catch (Exception e) {
            throw new NoSuccessException("Failed to connect to GridFTP server: "+m_wmsServerHost, e);
        }

        // submit
        String jobId = this.submit(jobDesc, checkMatch, uniqId);
        return new WMSJobIOHandler(stagingClient, m_stagingPrefix, jobId);
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

    public String getStagingBaseURL() {
        String hostname = (String) m_parameters.get(HOST_NAME);
        return "gsiftp://"+hostname+":2811/tmp";
    }

    public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch (BaseException e) {
            rethrow(e);
        }
        Properties jobDesc = parseJobDescription(jdl);
        return getStringValue_IfExists(jobDesc, "SandboxDirectory");
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
/*
        StringList result = null;
        try {
            result = m_client.getSandboxDestURI(nativeJobId, "gsiftp");
        } catch (BaseException e) {
            rethrow(e);
        }
        if(result==null  || result.getItem()==null || result.getItem().length<1) {
            throw new NoSuccessException("Unable to find sandbox dest uri");
        }
*/
        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch (BaseException e) {
            rethrow(e);
        }
        String baseUri = "";
        Properties jobDesc = parseJobDescription(jdl);
        int transfersLength = getIntValue_IfExists(jobDesc, "InputSandboxPreStaging");
        StagingTransfer[] transfers = new StagingTransfer[transfersLength];
        for (int i=0; i<transfersLength; i++) {
            transfers[i] = new StagingTransfer(
                    getStringValue(jobDesc, "InputSandboxPreStaging_"+i+"_From"),
                    baseUri+getStringValue(jobDesc, "InputSandboxPreStaging_"+i+"_To"),
                    getBooleanValue(jobDesc, "InputSandboxPreStaging_"+i+"_Append"));
        }
        return transfers;
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
/*
        StringAndLongList result = null;
        try {
            result = m_client.getOutputFileList(nativeJobId, "gsiftp");
        } catch (BaseException e) {
            rethrow(e);
        }
        if (result==null || result.getFile()==null || result.getFile().length<1) {
            throw new NoSuccessException("Unable to find output file list");
        }
*/
        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch (BaseException e) {
            rethrow(e);
        }
        String baseUri = "";
        Properties jobDesc = parseJobDescription(jdl);
        int transfersLength = getIntValue_IfExists(jobDesc, "OutputSandboxPostStaging");
        StagingTransfer[] transfers = new StagingTransfer[transfersLength];
        for (int i=0; i<transfersLength; i++) {
            transfers[i] = new StagingTransfer(
                    baseUri+getStringValue(jobDesc, "OutputSandboxPostStaging_"+i+"_From"),
                    getStringValue(jobDesc, "OutputSandboxPostStaging_"+i+"_To"),
                    getBooleanValue(jobDesc, "OutputSandboxPostStaging_"+i+"_Append"));
        }
        return transfers;
    }

    public void start(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            m_client.jobStart(nativeJobId);
        } catch (BaseException e) {
            rethrow(e);
        }
    }

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	try {
	    	m_client.jobCancel(nativeJobId);
        } catch (BaseException e) {
            rethrow(e);
		}
    }

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        if(m_tmpProxyFile != null && m_tmpProxyFile.exists()) {
            m_tmpProxyFile.delete(); // warning: file not deleted (deletion managed by JVM)
        }

        if (m_stagingPrefix != null) {
            try {
                GridFTPClient client = new GridFTPClient(m_wmsServerHost, 2811);
                client.authenticate(m_credential);
                client.deleteFile(m_stagingPrefix+"-"+WMSJobIOHandler.OUTPUT_SUFFIX);
                client.deleteFile(m_stagingPrefix+"-"+WMSJobIOHandler.ERROR_SUFFIX);
            } catch (Exception e) {
                throw new NoSuccessException("Failed to cleanup job: "+nativeJobId, e);
            }
        }

        // purge job
        try {
            m_client.jobPurge(nativeJobId);
        } catch(BaseException  e) {
            rethrow(e);
        }
	}

    private static void rethrow(BaseException exception) throws PermissionDeniedException, NoSuccessException {
        try {
            throw exception;
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
        } catch (ServiceException e) {
            throw new NoSuccessException(e);
        } catch (ServerOverloadedFaultException e) {
            throw new NoSuccessException(e);
        } catch (BaseException e) {
            throw new NoSuccessException(e);
        }
    }

    private static Properties parseJobDescription(String jdl) throws NoSuccessException {
        Properties jobDesc = new Properties();
        try {
            jobDesc.load(new ByteArrayInputStream(jdl.getBytes()));
        } catch (IOException e) {
            throw new NoSuccessException("Failed to retrieve JDL", e);
        }
        return jobDesc;
    }
    private static String getValue(Properties jobDesc, String key) throws NoSuccessException {
        String value = jobDesc.getProperty(key);
        if (value!=null) {
            String trimmed = value.trim();
            if (trimmed.endsWith(";")) {
                return trimmed.substring(0, trimmed.length()-1);
            } else {
                throw new NoSuccessException("Failed to parse JDL attribute: "+value);
            }
        } else {
            return null;
        }
    }
    private static String getStringValue(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        } else {
            throw new NoSuccessException("Failed to parse JDL attribute: "+value);
        }
    }
    private static String getStringValue_IfExists(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length()-1);
            } else {
                throw new NoSuccessException("Failed to parse JDL attribute: "+value);
            }
        } else {
            return null;
        }
    }
    private static int getIntValue_IfExists(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new NoSuccessException("Failed to parse JDL attribute: "+value, e);
            }
        } else {
            return 0;
        }
    }
    private static boolean getBooleanValue(Properties jobDesc, String key) throws NoSuccessException {
        String value = getValue(jobDesc, key);
        if (value!=null) {
            return Boolean.parseBoolean(value);
        } else {
            throw new NoSuccessException("Failed to parse JDL attribute: "+value);
        }
    }
}
