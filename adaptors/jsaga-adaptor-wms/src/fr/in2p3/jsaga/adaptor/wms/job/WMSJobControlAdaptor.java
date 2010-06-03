package fr.in2p3.jsaga.adaptor.wms.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Map;

import javax.xml.rpc.Stub;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.glite.jdl.AdParser;
import org.glite.jdl.JobAdException;
import org.glite.security.delegation.GrDPConstants;
import org.glite.security.delegation.GrDPX509Util;
import org.glite.wms.wmproxy.AuthenticationFaultType;
import org.glite.wms.wmproxy.AuthorizationFaultType;
import org.glite.wms.wmproxy.BaseFaultType;
import org.glite.wms.wmproxy.GenericFaultType;
import org.glite.wms.wmproxy.InvalidArgumentFaultType;
import org.glite.wms.wmproxy.JdlType;
import org.glite.wms.wmproxy.NoSuitableResourcesFaultType;
import org.glite.wms.wmproxy.ServerOverloadedFaultType;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyLocator;
import org.glite.wms.wmproxy.WMProxy_PortType;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.HTTPSSender;
import org.globus.ftp.GridFTPClient;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.gridsite.www.namespaces.delegation_1.DelegationSoapBindingStub;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.UOptionalBoolean;
import fr.in2p3.jsaga.adaptor.base.usage.UOptionalInteger;
import fr.in2p3.jsaga.adaptor.base.usage.UOr;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobControlAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* 		  Jerome Revillard (jrevillard@maatg.com) - MAAT France
* Date:   18 fev. 2008
* Updated: 8 Mai  2010 (Jerome)
* ***************************************************/
/**
 * TODO : Support of jsdl:TotalCPUTime, jsdl:OperatingSystemType, jsdl:TotalCPUCount, jsdl:CPUArchitecture
 * TODO : Support of space in environment value
 * TODO : Test MPI jobs
 */
public class WMSJobControlAdaptor extends WMSJobAdaptorAbstract
		implements StagingJobAdaptorTwoPhase, CleanableJobAdaptor, StreamableJobBatch {
    private static final String DEFAULT_JDL_FILE = "DefaultJdlFile";
	
	private WMProxy_PortType m_client;
    private String m_delegationId = "myId";
    private String m_wmsServerHost;
    private String m_wmsServerUrl;
    private String m_LBAddress;

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

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new WMSJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        if (attributes.containsKey(DEFAULT_JDL_FILE)) {
            File defaultJdlFile = new File((String) attributes.get(DEFAULT_JDL_FILE));
            try {
                // may override jsaga-universe.xml attributes
                new DefaultJDL(defaultJdlFile).fill(attributes);
            } catch (FileNotFoundException e) {
                throw new BadParameterException(e);
            }
        }

        // set WMS url
        m_wmsServerHost = host;
    	m_wmsServerUrl = "https://"+host+":"+port+basePath;
        m_LBAddress = (String) attributes.get("LBAddress");

    	// get certificate directory
        if (m_certRepository == null) {
            throw new NoSuccessException("Configuration attribute not found in context: "+Context.CERTREPOSITORY);
        } else if (!m_certRepository.isDirectory()) {
            throw new NoSuccessException("Directory not found: "+m_certRepository);
        }	
        	
        try{
			SimpleProvider provider = new SimpleProvider();
			SimpleTargetedChain c =  new SimpleTargetedChain(new HTTPSSender());
			provider.deployTransport("https", c);
			c =  new SimpleTargetedChain(new HTTPSSender());
			provider.deployTransport("http", c);
			WMProxyLocator serviceLocator = new WMProxyLocator(provider);
			
			TrustedCertificates trustedCertificates = TrustedCertificates.load(m_certRepository.getAbsolutePath());
			
			m_client = serviceLocator.getWMProxy_PortType(new URL(m_wmsServerUrl));
            ((Stub)m_client)._setProperty(GSIConstants.GSI_CREDENTIALS,m_credential);
            ((Stub)m_client)._setProperty(GSIConstants.GSI_TRANSPORT, GSIConstants.ENCRYPTION);
            ((Stub)m_client)._setProperty(GSIConstants.TRUSTED_CERTIFICATES, trustedCertificates);
            ((Stub)m_client)._setProperty(GSIConstants.GSI_AUTHORIZATION, HostAuthorization.getInstance());
            
			DelegationSoapBindingStub delegationServiceStub = (DelegationSoapBindingStub) serviceLocator.getWMProxyDelegation_PortType(new URL(m_wmsServerUrl));
            ((Stub)delegationServiceStub)._setProperty(GSIConstants.GSI_CREDENTIALS,m_credential);
            ((Stub)delegationServiceStub)._setProperty(GSIConstants.GSI_TRANSPORT, GSIConstants.ENCRYPTION);
            ((Stub)delegationServiceStub)._setProperty(GSIConstants.TRUSTED_CERTIFICATES, trustedCertificates);
            ((Stub)delegationServiceStub)._setProperty(GSIConstants.GSI_AUTHORIZATION, HostAuthorization.getInstance());
            
            String certReq = delegationServiceStub.getProxyReq(m_delegationId);
            
            //create proxy from certificate request
            GlobusCredential globusCred = ((GlobusGSSCredentialImpl)m_credential).getGlobusCredential();
            
            X509Certificate[] userCerts = globusCred.getCertificateChain();
			PrivateKey key = globusCred.getPrivateKey();
			
			BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();
			
			X509Certificate certificate = factory.createCertificate(new ByteArrayInputStream(GrDPX509Util.readPEM(
                    new ByteArrayInputStream(certReq.getBytes()), GrDPConstants.CRH,
                    GrDPConstants.CRF)),userCerts[0], key, 12*3600, GSIConstants.GSI_2_PROXY); //12 hours proxy

			X509Certificate[] finalCerts = new X509Certificate[userCerts.length+1];
			finalCerts[0] = certificate;
			for (int index = 1; index <= userCerts.length; ++index){
				finalCerts[index] = userCerts[index - 1];
			}
			m_client.putProxy(m_delegationId, new String(GrDPX509Util.certChainToByte(finalCerts)));
        } catch (org.glite.wms.wmproxy.AuthenticationFaultType exc) {
        	disconnect();
			throw new AuthenticationFailedException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.AuthorizationFaultType exc) {
			disconnect();
			throw new AuthenticationFailedException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.ServerOverloadedFaultType exc) {
			disconnect();
			throw new NoSuccessException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.GenericFaultType exc) {
			disconnect();
			throw new NoSuccessException(createExceptionMessage(exc));
		} catch ( java.rmi.RemoteException exc) {
			disconnect();
			throw new NoSuccessException(exc.getMessage());
		} catch (Exception exc) {
			disconnect();
			throw new NoSuccessException(exc.getMessage());
		}

		
        // ping service
		if("true".equalsIgnoreCase((String) attributes.get(JobAdaptor.CHECK_AVAILABILITY))) {
            try {
            	m_client.getVersion();
            } catch (org.glite.wms.wmproxy.AuthenticationFaultType exc) {
            	disconnect();
    			throw new AuthenticationFailedException(createExceptionMessage(exc));
    		} catch (org.glite.wms.wmproxy.GenericFaultType exc) {
    			disconnect();
    			throw new NoSuccessException(exc.getMessage());
    		} catch ( java.rmi.RemoteException exc) {
    			disconnect();
    			throw new NoSuccessException(exc.getMessage());
    		} catch (Exception exc) {
    			disconnect();
    			throw new NoSuccessException(exc.getMessage());
    		}
        }
    }	

	public void disconnect() throws NoSuccessException {
        m_wmsServerUrl = null;
        m_credential = null;
        m_client = null;
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorXSLT("xsl/job/jdl.xsl");
    }

    public String submit(String jobDesc, boolean checkMatch, String uniqId) throws PermissionDeniedException, TimeoutException, NoSuccessException, BadResource {
    	try {
			// parse JDL and Check Matching
            if (checkMatch) {
			    checkJDLAndMatch(jobDesc, m_client);
            }

            // register job
            String nativeJobId = m_client.jobRegister(jobDesc, m_delegationId).getId();

            // set LB from nativeJobId
            if (m_LBAddress == null) {
                WMStoLB.getInstance().setLBHost(m_wmsServerUrl, nativeJobId);
            }
	    	return nativeJobId;
    	} catch (BaseFaultType e) {
            rethrow(e);
            return null;    // dead code
        } catch (Exception e) {
            try {
                AdParser.parseJdl(jobDesc);
            } catch (JobAdException e2) {
                throw new NoSuccessException("The job description is not valid", e2);
            }
            throw new NoSuccessException(e);
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

    private void checkJDLAndMatch(String jobDesc, WMProxy_PortType m_client2) throws NoSuccessException, ServerOverloadedFaultType, AuthorizationFaultType, GenericFaultType, AuthenticationFaultType, NoSuitableResourcesFaultType, InvalidArgumentFaultType, RemoteException{
		// parse JDL
		try {
			AdParser.parseJdl(jobDesc);
		} catch (JobAdException e) {
			throw new NoSuccessException("The job description is not valid", e);
		}

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

    public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
        StagingJDL parsedJdl = new StagingJDL(jdl);
        return parsedJdl.getStagingDirectory();
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
/*
        StringList result = null;
        try {
            result = m_client.getSandboxDestURI(nativeJobId, "gsiftp");
        } catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
        if(result==null  || result.getItem()==null || result.getItem().length<1) {
            throw new NoSuccessException("Unable to find sandbox dest uri");
        }
*/
        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
        String baseUri = "";
        StagingJDL parsedJdl = new StagingJDL(jdl);
        return parsedJdl.getInputStagingTransfer(baseUri);
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
/*
        StringAndLongList result = null;
        try {
            result = m_client.getOutputFileList(nativeJobId, "gsiftp");
        } catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
        if (result==null || result.getFile()==null || result.getFile().length<1) {
            throw new NoSuccessException("Unable to find output file list");
        }
*/
        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
        String baseUri = "";
        StagingJDL parsedJdl = new StagingJDL(jdl);
        return parsedJdl.getOutputStagingTransfers(baseUri);
    }

    public void start(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            m_client.jobStart(nativeJobId);
        } catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
    }

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	try {
	    	m_client.jobCancel(nativeJobId);
    	} catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
    }

	public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
//        if(m_tmpProxyFile != null && m_tmpProxyFile.exists()) {
//            m_tmpProxyFile.delete(); // warning: file not deleted (deletion managed by JVM)
//        }

        if (m_stagingPrefix != null) {
            try {
                GridFTPClient client = new GridFTPClient(m_wmsServerHost, 2811);
                client.authenticate(m_credential);
                client.deleteFile(m_stagingPrefix+"-"+WMSJobIOHandler.OUTPUT_SUFFIX);
                client.deleteFile(m_stagingPrefix+"-"+WMSJobIOHandler.ERROR_SUFFIX);
                client.close();
            } catch (Exception e) {
                throw new NoSuccessException("Failed to cleanup job: "+nativeJobId, e);
            }
        }

        // purge job
        try {
            m_client.jobPurge(nativeJobId);
        } catch(BaseFaultType  e) {
            rethrow(e);
        } catch (RemoteException e) {
        	throw new NoSuccessException(e.getMessage());
		}
	}

    private static void rethrow(BaseFaultType exception) throws PermissionDeniedException, NoSuccessException {
        try {
            throw exception;
        } catch (org.glite.wms.wmproxy.AuthenticationFaultType exc) {
			throw new PermissionDeniedException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.AuthorizationFaultType exc) {
			throw new PermissionDeniedException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.OperationNotAllowedFaultType exc) {
			throw new PermissionDeniedException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.ServerOverloadedFaultType exc) {
			throw new NoSuccessException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.InvalidArgumentFaultType exc) {
			throw new NoSuccessException(createExceptionMessage(exc));
		} catch (org.glite.wms.wmproxy.GenericFaultType exc) {
			throw new NoSuccessException(createExceptionMessage(exc));
		} catch (Exception exc) {
			throw new NoSuccessException(exc.getMessage());
		}
    }
    
    /*
	* Creates an exception message from the input exception object
	*/
	private static String createExceptionMessage(BaseFaultType exc) {
		String message = "";
		String date = "";
		//int hours = 0;
		String ec = exc.getErrorCode();
		String[] cause = (String[])exc.getFaultCause();
		// fault description
		String desc = exc.getDescription();
		if (desc.length()>0) { message = desc + "\n";}
		// method
		String meth = exc.getMethodName() ;
		if (meth.length()>0) { message += "Method: " + meth + "\n";}
		// time stamp
		Calendar  calendar = exc.getTimestamp();
		//java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM);
		//hours = calendar.get(Calendar.HOUR_OF_DAY) - (calendar.get(Calendar.ZONE_OFFSET)/ (60*60*1000));
		/*
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		df.setCalendar(calendar);
		calendar = df.getCalendar( );
		if (calendar != null){
		*/
			calendar.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
			date = dayStr[calendar.get(Calendar.DAY_OF_WEEK)] + " " +
				monthStr[calendar.get(Calendar.MONTH)] + " " +
				twodigits(calendar.get(Calendar.DAY_OF_MONTH)) + " "
				+ calendar.get(Calendar.YEAR) + " ";
			//hours =  - (calendar.get(Calendar.ZONE_OFFSET)/ (60*60*1000));
			date += twodigits(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
				twodigits(calendar.get(Calendar.MINUTE)) + ":" +
				twodigits(calendar.get(Calendar.SECOND)) ;
			date += " " + calendar.getTimeZone().getID( );
			if (date.length()>0) { message += "TimeStamp: " + date + "\n";}
		//}
		// error code
		if (ec.length()>0) { message += "ErrorCode: " + ec + "\n";}
		// fault cause(s)
		for (int i = 0; i < cause.length; i++) {
			if (i==0) { message += "Cause: " + cause[i] + "\n";}
			else { message += cause[i] + "\n" ;}
		}
		return message;
	}

	private static String twodigits(int n) {
		String td = "";
		if (n>=0 && n<10) {
			td = "0" + n ;
		} else {
			td = "" + n;
		}
		return td;
	}
	
	private final static String[] monthStr  = {"Jan", "Feb", "March", "Apr", "May", "June" ,"July", "Aug", "Sept", "Oct", "Nov", "Dec"};
	private final static String[] dayStr = {"Sun", "Mon", "Tue", "Wedn", "Thu", "Fri" ,"Sat"};

}
