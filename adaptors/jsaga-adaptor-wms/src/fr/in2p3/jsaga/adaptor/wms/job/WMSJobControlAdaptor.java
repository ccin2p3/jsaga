package fr.in2p3.jsaga.adaptor.wms.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Map;

import javax.xml.rpc.Stub;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.log4j.Logger;
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
import org.glite.wms.wmproxy.StringList;
import org.glite.wms.wmproxy.WMProxyLocator;
import org.glite.wms.wmproxy.WMProxy_PortType;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.HTTPSSender;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.auth.NoAuthorization;
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
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.globus.gsi.GSIConstants.CertificateType;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.X509Credential;


/*
 * ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) *** *** http://cc.in2p3.fr/
 * *** *************************************************** File:
 * WMSJobControlAdaptor Author: Nicolas DEMESY (nicolas.demesy@bt.com) Jerome
 * Revillard (jrevillard@maatg.com) - MAAT France Date: 18 fev. 2008 Updated: 8
 * Mai 2010 (Jerome) **************************************************
 */
/**
 * TODO : Support of jsdl:TotalCPUTime, jsdl:OperatingSystemType,
 * jsdl:TotalCPUCount, jsdl:CPUArchitecture TODO : Support of space in
 * environment value TODO : Test MPI jobs
 */
public class WMSJobControlAdaptor extends WMSJobAdaptorAbstract
        implements StagingJobAdaptorTwoPhase, CleanableJobAdaptor {

    private static Logger m_logger = Logger.getLogger(WMSJobControlAdaptor.class);
    
    private WMProxy_PortType m_client;
    private DelegationSoapBindingStub delegationServiceStub;
    private String delegationId;
    private String defaultDelegationId = UUID.randomUUID().toString();
    private final AtomicBoolean delegated = new AtomicBoolean(false);
    private String m_wmsServerUrl;
    private String m_LBAddress;

    public String getType() {
        return "wms";
    }

    public int getDefaultPort() {
        return 7443;
    }

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new WMSJobMonitorAdaptor();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        if (attributes.containsKey(DEFAULT_JDL_FILE)) {
            File defaultJdlFile = new File((String) attributes.get(DEFAULT_JDL_FILE));
            try {
                // may override jsaga-default-contexts.xml attributes
                new DefaultJDL(defaultJdlFile).fill(attributes);
            } catch (FileNotFoundException e) {
                throw new BadParameterException(e);
            }
        }

        m_wmsServerUrl = "https://" + host + ":" + port + basePath;
        m_LBAddress = (String) attributes.get("LBAddress");

        // get certificate directory
        if (m_certRepository == null) {
            throw new NoSuccessException("Configuration attribute not found in context: " + Context.CERTREPOSITORY);
        } else if (!m_certRepository.isDirectory()) {
            throw new NoSuccessException("Directory not found: " + m_certRepository);
        }

        try {
            delegationId = (String) attributes.get("DelegationID");
            if (delegationId != null) {
                ((Stub) m_client)._setProperty(GSIConstants.GSI_USER_DN, delegationId);
            } else {
                delegationId = defaultDelegationId;
            }

            SimpleProvider provider = new SimpleProvider();
            SimpleTargetedChain c = new SimpleTargetedChain(new HTTPSSender());
            provider.deployTransport("https", c);
            c = new SimpleTargetedChain(new HTTPSSender());
            provider.deployTransport("http", c);
            WMProxyLocator serviceLocator = new WMProxyLocator(provider);

            TrustedCertificates trustedCertificates = TrustedCertificates.load(m_certRepository.getAbsolutePath());

            m_client = serviceLocator.getWMProxy_PortType(new URL(m_wmsServerUrl));
            ((Stub) m_client)._setProperty(GSIConstants.GSI_CREDENTIALS, m_credential);
            ((Stub) m_client)._setProperty(GSIConstants.GSI_TRANSPORT, GSIConstants.ENCRYPTION);
            ((Stub) m_client)._setProperty(GSIConstants.TRUSTED_CERTIFICATES, trustedCertificates);
            ((Stub) m_client)._setProperty(GSIConstants.GSI_AUTHORIZATION, NoAuthorization.getInstance());

            //((Stub) m_client)._setProperty(GSIConstants.GSI_AUTH_USERNAME, delegationId);
            //((Stub) m_client)._setProperty(GSIConstants.GSI_USER_DN, delegationId);
            //((Stub) m_client)._setProperty(GSIConstants.GSI_USER_DN, delegationId);

            ((org.apache.axis.client.Stub) m_client).setTimeout(120 * 1000); //2 mins

            delegationServiceStub = (DelegationSoapBindingStub) serviceLocator.getWMProxyDelegation_PortType(new URL(m_wmsServerUrl));
            ((Stub) delegationServiceStub)._setProperty(GSIConstants.GSI_CREDENTIALS, m_credential);
            ((Stub) delegationServiceStub)._setProperty(GSIConstants.GSI_TRANSPORT, GSIConstants.ENCRYPTION);
            ((Stub) delegationServiceStub)._setProperty(GSIConstants.TRUSTED_CERTIFICATES, trustedCertificates);
            ((Stub) delegationServiceStub)._setProperty(GSIConstants.GSI_AUTHORIZATION, NoAuthorization.getInstance());
            ((org.apache.axis.client.Stub) delegationServiceStub).setTimeout(120 * 1000); //2 mins

            //((Stub) delegationServiceStub)._setProperty(GSIConstants.GSI_AUTH_USERNAME, delegationId);
            //((Stub) delegationServiceStub)._setProperty(GSIConstants.GSI_USER_DN, delegationId);  
            //if(myProxyDN != null) ((Stub) delegationServiceStub)._setProperty(GSIConstants.GSI_AUTH_USERNAME, myProxyDN);  

        } catch (Exception exc) {
            disconnect();
            throw new NoSuccessException(exc.getMessage());
        }

        // ping service
        if ("true".equalsIgnoreCase((String) attributes.get(JobAdaptor.CHECK_AVAILABILITY))) {
            try {
                m_logger.debug("Connected to WMS version " + m_client.getVersion());
            } catch (org.glite.wms.wmproxy.AuthenticationFaultType exc) {
                disconnect();
                throw new AuthenticationFailedException(createExceptionMessage(exc));
            } catch (org.glite.wms.wmproxy.GenericFaultType exc) {
                disconnect();
                throw new NoSuccessException(exc.getMessage());
            } catch (java.rmi.RemoteException exc) {
                disconnect();
                throw new NoSuccessException(exc.getMessage());
            } catch (Exception exc) {
                disconnect();
                throw new NoSuccessException(exc.getMessage());
            }
        }

        delegated.set(false);
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
            //Credentials delegation
            delegateCredentials(delegationId);

            // parse JDL and Check Matching
            if (checkMatch) {
                checkJDLAndMatch(jobDesc, m_client);
            }

            // register job
            String nativeJobId = m_client.jobRegister(jobDesc, delegationId).getId();

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

    private void checkJDLAndMatch(String jobDesc, WMProxy_PortType m_client2) throws NoSuccessException, ServerOverloadedFaultType, AuthorizationFaultType, GenericFaultType, AuthenticationFaultType, NoSuitableResourcesFaultType, InvalidArgumentFaultType, RemoteException {
        // parse JDL
        try {
            AdParser.parseJdl(jobDesc);
        } catch (JobAdException e) {
            throw new NoSuccessException("The job description is not valid", e);
        }

        // get available CE
        StringAndLongList result = m_client.jobListMatch(jobDesc, delegationId);
        if (result != null) {
            // list of CE
           StringAndLongType[] list = (StringAndLongType[])result.getFile();
           if (list == null) {
        	   throw new BadResource("No Computing Element matching your job requirements has been found!");
           }
        } else {
            throw new BadResource("No Computing Element matching your job requirements has been found!");
        }
    }

    private void delegateCredentials(String delegationId) throws AuthenticationFailedException, NoSuccessException {
        synchronized (delegated) {
            if (!delegated.get()) {
                try {
                    //synchronized (m_delegationId) {
                    //  if (!m_delegationDone || force) {
                    String certReq = delegationServiceStub.getProxyReq(delegationId);

                    //create proxy from certificate request
                    X509Credential globusCred = ((GlobusGSSCredentialImpl) m_credential).getX509Credential();

                    X509Certificate[] userCerts = globusCred.getCertificateChain();
                    PrivateKey key = globusCred.getPrivateKey();

                    BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory.getDefault();

                    //By default, use GSI_2_PROXY type
                    CertificateType proxyType = globusCred.getProxyType();
                    if(proxyType.equals(CertificateType.EEC)){
                    	proxyType = CertificateType.GSI_2_PROXY;
                    }
                    
                    X509Certificate certificate = factory.createCertificate(new ByteArrayInputStream(GrDPX509Util.readPEM(
                            new ByteArrayInputStream(certReq.getBytes()), GrDPConstants.CRH,
                            GrDPConstants.CRF)), userCerts[0], key, 12 * 3600, proxyType); //12 hours proxy

                    X509Certificate[] finalCerts = new X509Certificate[userCerts.length + 1];
                    finalCerts[0] = certificate;
                    for (int index = 1; index <= userCerts.length; ++index) {
                        finalCerts[index] = userCerts[index - 1];
                    }
                    m_client.putProxy(delegationId, new String(GrDPX509Util.certChainToByte(finalCerts)));

                    //System.out.println(m_client.getDelegatedProxyInfo(delegationId));

                    // m_delegationDone = true;
                    //    }
                    //  }
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
                } catch (java.rmi.RemoteException exc) {
                    disconnect();
                    throw new NoSuccessException(exc);
                } catch (Exception exc) {
                    disconnect();
                    throw new NoSuccessException(exc.getMessage());
                }
            }
            delegated.set(true);
        }
    }

    public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return null;    // use the CREAM default staging directory
    }

    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        StringList result = null;
        try {
            result = m_client.getSandboxDestURI(nativeJobId, "gsiftp");
        } catch (BaseFaultType e) {
            rethrow(e);
        } catch (RemoteException e) {
            throw new NoSuccessException(e.getMessage());
        }
        if (result == null || result.getItem() == null || result.getItem().length < 1) {
            throw new NoSuccessException("Unable to find sandbox dest uri");
        }
        String baseUri = result.getItem(0);

        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch (BaseFaultType e) {
            rethrow(e);
        } catch (RemoteException e) {
            throw new NoSuccessException(e.getMessage());
        }
        StagingJDL parsedJdl = new StagingJDL(jdl);
        return parsedJdl.getInputStagingTransfer(baseUri);
    }

    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        StringAndLongList result = null;
        try {
            result = m_client.getOutputFileList(nativeJobId, "gsiftp");
        } catch (BaseFaultType e) {
            rethrow(e);
        } catch (RemoteException e) {
            throw new NoSuccessException(e.getMessage());
        }
        if (result == null || result.getFile() == null || result.getFile().length < 1) {
            throw new NoSuccessException("Unable to find output file list");
        }

        String jdl = null;
        try {
            jdl = m_client.getJDL(nativeJobId, JdlType.ORIGINAL);
        } catch (BaseFaultType e) {
            rethrow(e);
        } catch (RemoteException e) {
            throw new NoSuccessException(e.getMessage());
        }
        StagingJDL parsedJdl = new StagingJDL(jdl);
        return parsedJdl.getOutputStagingTransfers(result.getFile());
    }

    public void start(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            m_client.jobStart(nativeJobId);
        } catch (BaseFaultType e) {
            rethrow(e);
        } catch (RemoteException e) {
            throw new NoSuccessException(e.getMessage());
        }
    }

    public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            m_client.jobCancel(nativeJobId);
        } catch (BaseFaultType e) {
            rethrow(e);
        } catch (RemoteException e) {
            throw new NoSuccessException(e.getMessage());
        }
    }

    public void clean(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
//        if(m_tmpProxyFile != null && m_tmpProxyFile.exists()) {
//            m_tmpProxyFile.delete(); // warning: file not deleted (deletion managed by JVM)
//        }

        // purge job
        try {
            m_client.jobPurge(nativeJobId);
        } catch (BaseFaultType e) {
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
        } catch (org.glite.wms.wmproxy.JobUnknownFaultType exc) {
            throw new NoSuccessException(createExceptionMessage(exc));
        } catch (Exception exc) {
            throw new NoSuccessException(exc.getMessage(), exc);
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
        String[] cause = (String[]) exc.getFaultCause();
        // fault description
        String desc = exc.getDescription();
        if (desc.length() > 0) {
            message = desc + "\n";
        }
        // method
        String meth = exc.getMethodName();
        if (meth.length() > 0) {
            message += "Method: " + meth + "\n";
        }
        // time stamp
        Calendar calendar = exc.getTimestamp();
        //java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM);
        //hours = calendar.get(Calendar.HOUR_OF_DAY) - (calendar.get(Calendar.ZONE_OFFSET)/ (60*60*1000));
		/*
         * calendar.set(Calendar.HOUR_OF_DAY, hours); df.setCalendar(calendar);
         * calendar = df.getCalendar( ); if (calendar != null){
         */
        calendar.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        date = dayStr[calendar.get(Calendar.DAY_OF_WEEK) - 1] + " " // Calendar.SUNDAY = 1
                + monthStr[calendar.get(Calendar.MONTH)] + " " // Calendar.JANUARY = 0
                + twodigits(calendar.get(Calendar.DAY_OF_MONTH)) + " "
                + calendar.get(Calendar.YEAR) + " ";
        //hours =  - (calendar.get(Calendar.ZONE_OFFSET)/ (60*60*1000));
        date += twodigits(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                + twodigits(calendar.get(Calendar.MINUTE)) + ":"
                + twodigits(calendar.get(Calendar.SECOND));
        date += " " + calendar.getTimeZone().getID();
        if (date.length() > 0) {
            message += "TimeStamp: " + date + "\n";
        }
        //}
        // error code
        if (ec.length() > 0) {
            message += "ErrorCode: " + ec + "\n";
        }
        // fault cause(s)
        if (cause != null) {
            for (int i = 0; i < cause.length; i++) {
                if (i == 0) {
                    message += "Cause: " + cause[i] + "\n";
                } else {
                    message += cause[i] + "\n";
                }
            }
        }
        return message;
    }

    private static String twodigits(int n) {
        String td = "";
        if (n >= 0 && n < 10) {
            td = "0" + n;
        } else {
            td = "" + n;
        }
        return td;
    }
    private final static String[] monthStr = {"Jan", "Feb", "March", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
    private final static String[] dayStr = {"Sun", "Mon", "Tue", "Wedn", "Thu", "Fri", "Sat"};
}
