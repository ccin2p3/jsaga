/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.exec.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.AttributedURI;
import org.apache.axis.message.addressing.EndpointReferenceType;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.oasis.wsn.SubscriptionManager;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsn.WSBaseNotificationServiceAddressingLocator;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.SubscribeResponse;
import org.oasis.wsrf.lifetime.ResourceUnknownFaultType;
import org.oasis.wsrf.lifetime.Destroy;
import org.oasis.wsrf.lifetime.SetTerminationTime;
import org.oasis.wsrf.lifetime.SetTerminationTimeResponse;
//import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
import org.oasis.wsrf.properties.GetResourcePropertyResponse;
import org.w3c.dom.Element;

import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.util.Util;
import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationUtil;
import org.globus.delegationService.DelegationPortType;
import org.globus.delegationService.DelegationServiceAddressingLocator;
import org.globus.exec.generated.CreateManagedJobInputType;
import org.globus.exec.generated.CreateManagedJobOutputType;
import org.globus.exec.generated.FaultType;
import org.globus.exec.generated.FaultResourcePropertyType;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.generated.ManagedJobFactoryPortType;
import org.globus.exec.generated.ManagedJobPortType;
import org.globus.exec.generated.MultiJobDescriptionType;
import org.globus.exec.generated.ReleaseInputType;
import org.globus.exec.generated.ServiceLevelAgreementType;
import org.globus.exec.generated.StateChangeNotificationMessageType;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.utils.Resources;
import org.globus.exec.utils.ManagedExecutableJobConstants;
import org.globus.exec.utils.ManagedJobConstants;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.client.ManagedJobClientHelper;
import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
import org.globus.exec.utils.rsl.RSLHelper;
import org.globus.exec.utils.rsl.RSLParseException;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.rft.generated.DeleteRequestType;
import org.globus.rft.generated.TransferRequestType;
import org.globus.rft.generated.TransferType;
import org.globus.security.gridmap.GridMap;
import org.globus.util.I18n;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.container.ServiceContainer;
import org.globus.wsrf.encoding.DeserializationException;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.IdentityAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.GSISecureMsgAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSITransportAuthMethod;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.XmlUtils;


/**
 * This class represents a simple gram job. It allows
 * for submitting a job,canceling it,
 * sending a signal command and registering and
 * unregistering job state chang listeners.
 *
 * This class hides the middleware API from the consumer.
 */
public class GramJob implements NotifyCallback
{
    private static Log logger = LogFactory.getLog(GramJob.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private String securityType = null;
    private Integer msgProtectionType = null;
    private Authorization authorization = null;

    // holds job credentials
    private GSSCredential proxy = null;

    private boolean limitedDelegation = true;

    private boolean delegationEnabled = true;

    private boolean personal = false;

    private JobDescriptionType jobDescription;
    private EndpointReferenceType jobEndpointReference;
    private String jobHandle;
    private String id = null;

    //job status:
    private FaultType fault;
    private StateEnumeration state;
    private Object stateMonitor = new Object();
    private boolean holding;
    private int error;
    private int exitCode;
    private Vector listeners;

    private boolean destroyed = false;

    private Date duration;
    private Date terminationDate;

    private NotificationConsumerManager notificationConsumerManager;
    private EndpointReferenceType notificationConsumerEPR;
    private EndpointReferenceType notificationProducerEPR;

    public static final int DEFAULT_TIMEOUT = 120000;

    private int axisStubTimeOut = DEFAULT_TIMEOUT;

    private static final String BASE_SERVICE_PATH = "/wsrf/services/";

    private static final String SERVICE_PATH
        = BASE_SERVICE_PATH
        + "ManagedJobFactoryService";

    private static final String PERSONAL_SERVICE_PATH
        = BASE_SERVICE_PATH
        + "ManagedJobFactoryService";

    public static final Integer DEFAULT_MSG_PROTECTION =
        Constants.SIGNATURE;

    private static final String DEFAULT_SECURITY_TYPE = Constants.GSI_TRANSPORT;

    public static final Authorization DEFAULT_AUTHZ =
        HostAuthorization.getInstance();

    protected EndpointReferenceType delegationFactoryEndpoint = null;
    protected EndpointReferenceType stagingDelegationFactoryEndpoint = null;

    /**
    * Creates a gram job with no RSL. This default
    * constructor is used in conjunction with {@link #setEndpoint()}.
    */
    public GramJob() {
        this.state = null;
        this.holding = false;
    }

    /**
    * Creates a gram job with specified job description.
    */
    public GramJob(JobDescriptionType jobDescription)
    {
        this();
        try {
            this.jobDescription
                = (JobDescriptionType) ObjectSerializer.clone(jobDescription);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
    * Creates a gram job with specified file containing the rsl.
    *
    * Currently the rsl is required to be in the new XML-based form.
    * For backwords compatibility this should accept the old format,
    * but the conversion algorithm isn't in place yet.
    *
    * @param rslFile   file with job specification
    */
    public GramJob(File rslFile) throws RSLParseException, FileNotFoundException
    {
        //reading GT4 RSL
        this(RSLHelper.readRSL(rslFile));

    }

    /**
    * Creates a gram job with specified rsl.
    *
    * Currently the rsl is required to be in the new XML-based form.
    * For backwords compatibility this should accept the old format,
    * but the conversion algorithm isn't in place yet.
    *
    * @param rsl   resource specification string
    */
    public GramJob(String rsl) throws RSLParseException
    {
        //reading GT4 RSL
        this(RSLHelper.readRSL(rsl));
    }

    /**
    * Add a listener to the GramJob. The listener will be notified whenever
    * the state of the GramJob changes.
    *
    * @param listener The object that wishes to receive state updates.
    * @see org.globus.gram.GramJobListener
    */
    public void addListener(GramJobListener listener) {
        if (listeners == null) listeners = new Vector();
        listeners.addElement(listener);
    }

    /**
    * Remove a listener from the GramJob. The listener will no longer be
    * notified of state changes for the GramJob.
    *
    * @param listener The object that wishes to stop receiving state updates.
    * @see org.globus.gram.GramJobListener
    */
    public void removeListener(GramJobListener listener) {
        if (listeners == null) return;
        listeners.removeElement(listener);
    }

    /**
    * Gets the credentials of this job.
    *
    * @return job credentials. If null none were set.
    *
    */
    public GSSCredential getCredentials() {
        return this.proxy;
    }

    /**
    * Sets credentials of the job
    *
    * @param  newProxy user credentials
    * @throws IllegalArgumentException if credentials are already set
    */
    public void setCredentials(GSSCredential newProxy) {
        if (this.proxy != null) {
            throw new IllegalArgumentException("Credentials already set");
        } else {
            this.proxy = newProxy;
        }
    }

    /**
    * Get the current state of this job.
    *
    * @return current job state
    */
    public StateEnumeration getState() {
        return this.state;
    }

    /**
     * Sets the state of the job and update the local state listeners.
     * Users should not call this function.
     * <b>Precondition</b>state != null
     * @param state state of the job
     */
    private void setState(
        StateEnumeration                    state,
        boolean                             holding)
    {
        if (state.equals(this.state)) {
            return;
        }
        this.state = state;
        logger.debug("setting job state to " + state);

        this.holding = holding;
        logger.debug("holding: " + holding);

        if (listeners == null) {
            return;
        }
        int size = listeners.size();
        for(int i=0; i<size; i++) {
            GramJobListener listener = (GramJobListener)listeners.elementAt(i);
            listener.stateChanged(this);
        }
    }

    public boolean isHolding() {
        return this.holding;
    }

    /**
     * Submits an interactive i.e. non-batch job with
     * limited delegation
     *
     * @see #request(String, String, boolean, boolean) for explanation
     * of parameters
     */
    public void submit(EndpointReferenceType factoryEndpoint)
            throws Exception
    {
        submit(factoryEndpoint, false, true, null);
    }

    /**
     * Submits a job with limited delegation.
     *
     * @see #request(URL, String, boolean, boolean) for explanation
     * of parameters
     */
    public void submit(EndpointReferenceType factoryEndpoint,
                       boolean batch)
            throws Exception
    {
        submit(factoryEndpoint, batch, true, null);
    }

    /**
     @todo add throws ...Exception for invalid credentials?
    * Submits a job to the specified service either as
    * an interactive or batch job. It can perform limited
    * or full delegation.
    *
    * @param factoryEndpoint the resource manager service endpoint.
    * The service address can be specified in the following ways:
    * <br>
    * host <br>
    * host:port <br>
    * host:port/service <br>
    * host/service <br>
    * host:/service <br>
    * host::subject <br>
    * host:port:subject <br>
    * host/service:subject <br>
    * host:/service:subject <br>
    * host:port/service:subject <br>
    *
    * @param factoryEndpoint the endpoint reference to the job factory service
    * @param batch
    *        specifies if the job should be submitted as
    *        a batch job.
    * @param limitedDelegation
    *        true for limited delegation, false for
    *        full delegation.
    * @param jobId
    *        For reliable service instance creation, use the specified jobId
    *        to allow repeated, reliable attempts to submit the job submission
    *        in the presence of an unreliable transport.
    *
    * @see #request(String) for detailed resource manager
    *       contact specification.
    */
    public void submit(EndpointReferenceType factoryEndpoint,
                       boolean batch,
                       boolean limitedDelegation,
                       String jobId)
        throws Exception
    {
        if (logger.isInfoEnabled()) {
            logger.info("<startTime name=\"submission\">"
                        +System.currentTimeMillis()
                        +"</startTime>");
        }

        this.id = jobId;

        this.limitedDelegation = limitedDelegation;

        EndpointReferenceType factoryEndpointOverride
            = this.jobDescription.getFactoryEndpoint();
        if (factoryEndpointOverride != null)
        {
            if (logger.isDebugEnabled()) {
                Element eprElement = ObjectSerializer.toElement(
                    factoryEndpointOverride,
                    RSLHelper.FACTORY_ENDPOINT_ATTRIBUTE_QNAME);
                logger.debug("Factory EPR Override: "
                            + XmlUtils.toString(eprElement));
            }
            factoryEndpoint = factoryEndpointOverride;
        } else
        {
            if (logger.isDebugEnabled()) {
                logger.debug("No Factory Endpoint Override...using supplied.");
            }
            this.jobDescription.setFactoryEndpoint(factoryEndpoint);
        }

        if (logger.isDebugEnabled()) {
            Element eprElement = ObjectSerializer.toElement(
                factoryEndpoint,
                RSLHelper.FACTORY_ENDPOINT_ATTRIBUTE_QNAME);
            logger.debug("Factory EPR: " + XmlUtils.toString(eprElement));
        }

        if (factoryEndpoint != null)
        {
            setSecurityTypeFromEndpoint(factoryEndpoint);

            if (isDelegationEnabled()) {
                populateJobDescriptionEndpoints(factoryEndpoint);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Job Submission ID: " + this.id);
        }

        ManagedJobFactoryPortType factoryPort =
            getManagedJobFactoryPortType(factoryEndpoint);
        this.jobEndpointReference
            = createJobEndpoint(factoryPort, batch);

        if (logger.isDebugEnabled()) {
            logger.debug("Job Endpoint:\n"
                        + this.jobEndpointReference);
        }
    }

    private void setSecurityTypeFromEndpoint(EndpointReferenceType epr)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Factory Endpoint Address URL Scheme:\n"
                        + epr.getAddress().getScheme());
        }

        if (epr.getAddress().getScheme().equals("http"))
        {
            if (logger.isDebugEnabled()) {
                logger.debug("using message-level security");
            }
            this.securityType = Constants.GSI_SEC_MSG;
        } else
        {
            if (logger.isDebugEnabled()) {
                logger.debug("using transport-level security");
            }
            Util.registerTransport();
            this.securityType = Constants.GSI_TRANSPORT;
        }
     }

    private void populateJobDescriptionEndpoints(
        EndpointReferenceType               mjFactoryEndpoint)
        throws                              Exception
    {
        //get job delegation factory endpoints
        EndpointReferenceType[] delegationFactoryEndpoints
            = fetchDelegationFactoryEndpoints(mjFactoryEndpoint);

        //delegate to single/multi-job
        EndpointReferenceType delegationEndpoint
            = delegate(delegationFactoryEndpoints[0], this.limitedDelegation);
        this.jobDescription.setJobCredentialEndpoint(
            delegationEndpoint);
        //separate delegation not supported
        this.jobDescription.setStagingCredentialEndpoint(
            delegationEndpoint);
        if (logger.isDebugEnabled()) {
            logger.debug("delegated credential endpoint:\n"
                        + delegationEndpoint);
        }
        //delegate to RFT
        populateStagingDescriptionEndpoints(
            mjFactoryEndpoint,
            delegationFactoryEndpoints[1],
            this.jobDescription);

        //delegate to sub-job if multi-job
        if (this.jobDescription instanceof MultiJobDescriptionType) {
            JobDescriptionType[] subJobDescriptions
                = ((MultiJobDescriptionType)this.jobDescription).getJob();
            for (int index=0; index<subJobDescriptions.length; index++) {
                EndpointReferenceType subJobFactoryEndpoint
                    = subJobDescriptions[index].getFactoryEndpoint();
                if (logger.isDebugEnabled()) {
                    Element eprElement = ObjectSerializer.toElement(
                        subJobFactoryEndpoint,
                        RSLHelper.FACTORY_ENDPOINT_ATTRIBUTE_QNAME);
                    logger.debug("Sub-Job Factory EPR: "
                                + XmlUtils.toString(eprElement));
                }
                if (subJobFactoryEndpoint != null) {
                    if (subJobFactoryEndpoint.getAddress() == null) {
                        logger.error(
                            "Sub-Job Factory Endpoint Address is null.");
                    }
                    //get job delegation factory endpoints
                    EndpointReferenceType[] subJobDelegationFactoryEndpoints
                        = fetchDelegationFactoryEndpoints(
                            subJobFactoryEndpoint);
                    EndpointReferenceType subJobCredentialEndpoint
                        = delegate(subJobDelegationFactoryEndpoints[0],
                                   true);
                    subJobDescriptions[index].setJobCredentialEndpoint(
                        subJobCredentialEndpoint);
                    //separate delegation not supported
                    subJobDescriptions[index].setStagingCredentialEndpoint(
                        subJobCredentialEndpoint);
                    if (logger.isDebugEnabled()) {
                        logger.debug("sub-job delegated credential endpoint:\n"
                                    + subJobCredentialEndpoint);
                    }
                    //delegate to sub-job RFT
                    populateStagingDescriptionEndpoints(
                        subJobFactoryEndpoint,
                        subJobDelegationFactoryEndpoints[1],
                        subJobDescriptions[index]);
                }
            }
        }
    }

    private void populateStagingDescriptionEndpoints(
        EndpointReferenceType               mjFactoryEndpoint,
        EndpointReferenceType               delegationFactoryEndpoint,
        JobDescriptionType                  jobDescription)
        throws                              Exception
    {
        //set staging factory endpoints and delegate
        TransferRequestType stageOut = jobDescription.getFileStageOut();
        TransferRequestType stageIn = jobDescription.getFileStageIn();
        DeleteRequestType cleanUp = jobDescription.getFileCleanUp();
        if ((stageOut != null) || (stageIn != null) || (cleanUp != null))
        {
            String factoryAddress = mjFactoryEndpoint.getAddress().toString();
            factoryAddress = factoryAddress.replaceFirst(
                "ManagedJobFactoryService",
                "ReliableFileTransferFactoryService");

            //delegate to RFT
            EndpointReferenceType transferCredentialEndpoint
                = delegate(delegationFactoryEndpoint, true);

            if (logger.isDebugEnabled())
            {
                logger.debug("transferCredentialEndpoint for job "
                            + this.id + ":\n"
                    + ObjectSerializer.toString(
                        transferCredentialEndpoint,
                        org.apache.axis.message.addressing.Constants.
                            QNAME_ENDPOINT_REFERENCE));
            }

            //set delegated credential endpoint for stage-out
            if (stageOut != null)
            {
                stageOut.setTransferCredentialEndpoint(
                    transferCredentialEndpoint);
            }

            //set delegated credential endpoint for stage-in
            if (stageIn != null)
            {
                stageIn.setTransferCredentialEndpoint(
                    transferCredentialEndpoint);
            }

            //set delegated credential endpoint for clean up
            if (cleanUp != null)
            {
                cleanUp.setTransferCredentialEndpoint(
                    transferCredentialEndpoint);
            }
        }
    }

    public EndpointReferenceType[] fetchDelegationFactoryEndpoints(
        EndpointReferenceType               factoryEndpoint)
        throws                              Exception
    {
        ManagedJobFactoryPortType factoryPort =
            getManagedJobFactoryPortType(factoryEndpoint);
        
        MessageElement[] message = new MessageElement[2];
        message[0] = new MessageElement(ManagedJobFactoryConstants.RP_DELEGATION_FACTORY_ENDPOINT);
        message[1] = new MessageElement(ManagedJobFactoryConstants.RP_STAGING_DELEGATION_FACTORY_ENDPOINT);
        GetMultipleResourcePropertiesResponse request =
            new GetMultipleResourcePropertiesResponse(message);
                        
        if (logger.isInfoEnabled()) {
            logger.info("<startTime name=\"fetchDelegFactoryEndoints\">"
                        +System.currentTimeMillis()
                        +"</startTime>");
        }
        GetMultipleResourcePropertiesResponse response =
            factoryPort.getMultipleResourceProperties(
            		new QName[]{
            				ManagedJobFactoryConstants.RP_DELEGATION_FACTORY_ENDPOINT, 
            				ManagedJobFactoryConstants.RP_STAGING_DELEGATION_FACTORY_ENDPOINT
            		});
        
        if (logger.isInfoEnabled()) {
            logger.info("<endTime name=\"fetchDelegFactoryEndoints\">"
                        +System.currentTimeMillis()
                        +"</endTime>");
        }

        SOAPElement [] any = response.get_any();

        EndpointReferenceType[] endpoints = new EndpointReferenceType[] {
            (EndpointReferenceType) ObjectDeserializer.
                toObject(any[0], EndpointReferenceType.class),
            (EndpointReferenceType) ObjectDeserializer.
                toObject(any[1], EndpointReferenceType.class)
        };


        return endpoints;
    }

    private EndpointReferenceType delegate(
        EndpointReferenceType               delegationFactoryEndpoint,
        boolean                             limitedDelegation)
        throws                              Exception
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Delegation Factory Endpoint:\n"
                        + delegationFactoryEndpoint);
        }

        // Credential to sign with
        GlobusCredential credential = null;
        if (this.proxy != null) {
            //user-specified credential
            credential
                = ((GlobusGSSCredentialImpl)this.proxy).getGlobusCredential();
        } else {
            //default credential
            credential = GlobusCredential.getDefaultCredential();
        }

        // lifetime in seconds
        int lifetime = DEFAULT_DURATION_HOURS * 60 * 60;
        if (this.duration != null) {
            long currentTime = System.currentTimeMillis();
            lifetime = (int)(this.duration.getTime() - currentTime);
        }

        ClientSecurityDescriptor secDesc = new ClientSecurityDescriptor();
        if (this.securityType.equals(Constants.GSI_SEC_MSG))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting GSISecureMsg protection type");
            }
            secDesc.setGSISecureMsg(this.getMessageProtectionType());
        } else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Setting GSITransport protection type");
            }
            secDesc.setGSITransport(this.getMessageProtectionType());
        }
        secDesc.setAuthz(getAuthorization());

        if (this.proxy != null) secDesc.setGSSCredential(this.proxy);

        // Get the public key to delegate on.
        if (logger.isInfoEnabled()) {
            logger.info("<startTime name=\"fetchDelegCertChainRP\">"
                        +System.currentTimeMillis()
                        +"</startTime>");
        }
        X509Certificate[] certsToDelegateOn =
            DelegationUtil.getCertificateChainRP(
                delegationFactoryEndpoint,
                secDesc);
        if (logger.isInfoEnabled()) {
            logger.info("<endTime name=\"fetchDelegCertChainRP\">"
                        +System.currentTimeMillis()
                        +"</endTime>");
        }
        X509Certificate certToSign = certsToDelegateOn[0];

        if (logger.isDebugEnabled()) {
            logger.debug("delegating...using authz method "
                        + getAuthorization());
        }

        //FIXME remove when there is a DelegationUtil.delegate(EPR, ...)
        String protocol = delegationFactoryEndpoint.getAddress().getScheme();
        String host = delegationFactoryEndpoint.getAddress().getHost();
        int port = delegationFactoryEndpoint.getAddress().getPort();
        String factoryUrl
            = protocol + "://" + host + ":" + port
            + BASE_SERVICE_PATH
            + DelegationConstants.FACTORY_PATH;
        if (logger.isDebugEnabled()) {
            logger.debug("Delegation Factory URL " + factoryUrl);
        }

        // send to delegation service and get epr.
        if (logger.isInfoEnabled()) {
            logger.info("<startTime name=\"delegate\">"
                        +System.currentTimeMillis()
                        +"</startTime>");
        }
        EndpointReferenceType credentialEndpoint = DelegationUtil.delegate(
            factoryUrl,
            credential,
            certToSign,
            lifetime,
            !limitedDelegation,
            secDesc);
            //getAuthorization());
        if (logger.isDebugEnabled()) {
            logger.debug("Delegated Credential Endpoint:\n"
                        + credentialEndpoint);
        }
        if (logger.isInfoEnabled()) {
            logger.info("<endTime name=\"delegate\">"
                        +System.currentTimeMillis()
                        +"</endTime>");
        }

        return credentialEndpoint;
    }

    /**
     *
     * @param pathArray String[] old
     * @param newPath String
     * @return String[] new path array
     */
    private String[] addPathToArray(String[] pathArray, String newPath) {
        String[] newPathArray;
        if (pathArray != null) {
            List newPathList = new ArrayList(Arrays.asList(pathArray));
            newPathList.add(newPath);
            newPathArray = (String[])newPathList.toArray(new String[0]);
        }
        else {
            newPathArray = new String[1];
            newPathArray[0] = newPath;
        }
        return newPathArray;

    }

    private String catenate(String baseURL, String path) {
        final String SEPARATOR = "/";
        String newPath = path;
        if (path.indexOf("://") < 0) { //not a URL already
            if (baseURL.endsWith(SEPARATOR)) {
                baseURL = baseURL.substring(0, baseURL.length() - 1);
            }
            //assert !baseURL.endsWith(SEPARATOR)
            if (!path.startsWith(SEPARATOR)) {
                baseURL = baseURL + SEPARATOR;
            }
            newPath = baseURL + path;
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("path " + path +
                             " is a URL already. No prepending of URL");
            }
        }
        return newPath;
    }

    public void prependBaseURLtoStageInSources(String baseURL) {
        TransferRequestType stageInDirectives =
            this.jobDescription.getFileStageIn();
        if (stageInDirectives != null) {
            TransferType[] transferArray = stageInDirectives.getTransfer();
            for (int i = 0; i < transferArray.length; i++) {
                String source = transferArray[i].getSourceUrl();
                transferArray[i].setSourceUrl(
                    catenate(baseURL, source));
            }
        }
        else {
            logger.debug("no stage in directives");
        }
    }

    public void prependBaseURLtoStageOutDestinations(String baseURL) {
        TransferRequestType stageOutDirectives =
            this.jobDescription.getFileStageOut();
        if (stageOutDirectives != null) {
            TransferType[] transferArray
                = stageOutDirectives.getTransfer();
            for (int i = 0; i < transferArray.length; i++) {
                String source = transferArray[i].getDestinationUrl();
                transferArray[i].setDestinationUrl(
                    catenate(baseURL, source));
            }
        }
        else {
            logger.debug("no stage out directives");
        }
    }


    /**
     * <b>Precondition</b>the job has not been submitted
     * @param path String
     */
    /*
    public void setDryRun(boolean enabled) {
        this.jobDescription.setDryRun(new Boolean(enabled));
    }
    */

    /**
     * @return the job description
     */
    public JobDescriptionType getDescription() throws Exception {
        if (this.jobDescription == null) {
            refreshRSLAttributes();
        }
        return this.jobDescription;
    }

    private EndpointReferenceType createJobEndpoint(
        ManagedJobFactoryPortType           factoryPort,
        boolean                             batch)
        throws                              Exception
    {
        //Create a service instance base on creation info
        logger.debug("creating ManagedJob instance");

        if (logger.isDebugEnabled()) {
            long millis = System.currentTimeMillis();
            BigDecimal seconds = new BigDecimal(
                ((double)millis)/1000);
            seconds = seconds.setScale(3, BigDecimal.ROUND_HALF_DOWN);
            logger.debug( "submission time, in seconds from the Epoch:"
                          + "\nbefore: " +  seconds.toString());
            logger.debug( "\nbefore, in milliseconds: " + millis);
        }

        ((org.apache.axis.client.Stub) factoryPort).setTimeout(
                this.axisStubTimeOut);

        CreateManagedJobInputType jobInput = new CreateManagedJobInputType();
        jobInput.setInitialTerminationTime(getServiceTerminationTime());
        if (this.id != null) {
            jobInput.setJobID(new AttributedURI(this.id));
        }
        if (this.jobDescription instanceof MultiJobDescriptionType) {
            jobInput.setMultiJob(
                (MultiJobDescriptionType) this.getDescription());
        } else {
            jobInput.setJob(this.getDescription());
        }

        if (!batch) {
            if (this.securityType.equals(Constants.GSI_SEC_MSG))
            {
                this.notificationConsumerManager =
                    NotificationConsumerManager.getInstance();
            } else
            {
                Map properties = new HashMap();
                properties.put(ServiceContainer.CLASS,
                    "org.globus.wsrf.container.GSIServiceContainer");
                this.notificationConsumerManager =
                    NotificationConsumerManager.getInstance(properties);
            }
            this.notificationConsumerManager.startListening();
            try {
                List topicPath = new LinkedList();
                topicPath.add(ManagedJobConstants.RP_STATE);

                ResourceSecurityDescriptor securityDescriptor
                    = new ResourceSecurityDescriptor();
                //TODO implement "service-side host authorization"
                String authz = null;
                if (authorization == null) {
                    authz = Authorization.AUTHZ_NONE;
                }
                else if (authorization instanceof HostAuthorization) {
                    authz = Authorization.AUTHZ_NONE;
                }
                else if (authorization instanceof SelfAuthorization) {
                    authz = Authorization.AUTHZ_SELF;
                }
                else if (authorization instanceof IdentityAuthorization) {
                    GridMap gridMap = new GridMap();
                    gridMap.map(
                        ( (IdentityAuthorization) authorization).getIdentity(),
                        "HaCk");
                    securityDescriptor.setGridMap(gridMap);

                    authz = Authorization.AUTHZ_GRIDMAP;
                }
                else {
                    logger.error("Unsupported authorization method class "
                                 + authorization.getClass().getName());
                    return null;
                }
                securityDescriptor.setAuthz(authz);
                Vector authMethod = new Vector();
                if (this.securityType.equals(Constants.GSI_SEC_MSG))
                {
                    authMethod.add(GSISecureMsgAuthMethod.BOTH);
                } else
                {
                    authMethod.add(GSITransportAuthMethod.BOTH);
                }
                securityDescriptor.setAuthMethods(authMethod);

                notificationConsumerEPR =
                    notificationConsumerManager.createNotificationConsumer(
                    topicPath,
                    this,
                    securityDescriptor);

                Subscribe subscriptionRequest = new Subscribe();
                subscriptionRequest.setConsumerReference(
                    notificationConsumerEPR);
                TopicExpressionType topicExpression = new TopicExpressionType(
                    WSNConstants.SIMPLE_TOPIC_DIALECT,
                    ManagedJobConstants.RP_STATE);
                subscriptionRequest.setTopicExpression(topicExpression);
                jobInput.setSubscribe(subscriptionRequest);
            }
            catch (Exception e) {
                //may happen...? Let's not fail.
                logger.error(e);
                try {
                    unbind();
                }
                catch (Exception unbindEx) {
                    //let's not fail the unbinding
                    logger.error(unbindEx);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("<startTime name=\"createManagedJob\">"
                        +System.currentTimeMillis()
                        +"</startTime>");
        }
        CreateManagedJobOutputType response
            = factoryPort.createManagedJob(jobInput);
        if (logger.isInfoEnabled()) {
            logger.info("<endTime name=\"createManagedJob\">"
                        +System.currentTimeMillis()
                        +"</endTime");
        }
        EndpointReferenceType jobEPR = response.getManagedJobEndpoint();

        this.notificationProducerEPR = response.getSubscriptionEndpoint();

        if (logger.isDebugEnabled()) {
            Calendar terminationTime = response.getNewTerminationTime();
            Calendar serviceCurrentTime = response.getCurrentTime();
            logger.debug(
                "Termination time granted by the factory to the job resource: "+
                terminationTime.getTime());
            logger.debug(
                "Current time seen by the factory service on creation: " +
                serviceCurrentTime.getTime());
        }

        return jobEPR;
    }


    /**
     * Returns true if the job has been requested.
     * Useful to determine if destroy() can be called when it
     * is not obvious.
     */
    public boolean isRequested() {
      //TODO see if can replace with check on state (== ACTIVE?)
      return this.jobEndpointReference != null;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    public boolean isPersonal() {
        return this.personal;
    }

    private ManagedJobFactoryPortType getManagedJobFactoryPortType(
        EndpointReferenceType               factoryEndpoint)
        throws                              Exception
    {
        ManagedJobFactoryPortType factoryPort =
            ManagedJobFactoryClientHelper.getPort(factoryEndpoint);

        setStubSecurityProperties((Stub) factoryPort);

        return factoryPort;
    }


    /**
    * Cancels a job.
    */
    public void cancel()
        throws Exception
    {
            logger.debug("destroy() called in cancel()");
            destroy();
            setState(StateEnumeration.Failed, false);
    }

    /**
    * Registers a callback listener for this job.
    * (Reconnects to the job)
    * <b>Precondition</b> this.jobEndpointReference != null
    * @throws GramException
    *         if error occurs during job registration.
    * @throws GSSException
    *         if user credentials are invalid.
    */
    public void bind() throws Exception {
        logger.debug("bind() called");
        this.notificationConsumerManager =
            NotificationConsumerManager.getInstance();
        try {
            this.notificationConsumerManager.startListening();
        }
        catch (Exception startListeningEx) {
            //should not have happened. Let's fail.
            throw startListeningEx;
        }
        //consumer started listening --> unbind() may be called to recover
        try {
            List topicPath = new LinkedList();
            topicPath.add(ManagedJobConstants.RP_STATE);

            ResourceSecurityDescriptor securityDescriptor
                = new ResourceSecurityDescriptor();
            //TODO implement "service-side host authorization"
            String authz = null;
            if (authorization == null) {
                authz = Authorization.AUTHZ_NONE;
            }
            else if (authorization instanceof HostAuthorization) {
                authz = Authorization.AUTHZ_NONE;
            }
            else if (authorization instanceof SelfAuthorization) {
                authz = Authorization.AUTHZ_SELF;
            }
            else if (authorization instanceof IdentityAuthorization) {
                GridMap gridMap = new GridMap();
                gridMap.map(
                    ( (IdentityAuthorization) authorization).getIdentity(),
                    "HaCk");
                securityDescriptor.setGridMap(gridMap);

                authz = Authorization.AUTHZ_GRIDMAP;
            }
            else {
                logger.error("Unsupported authorization method class "
                             + authorization.getClass().getName());
                return;
            }
            securityDescriptor.setAuthz(authz);
            Vector authMethod = new Vector();
            authMethod.add(GSISecureMsgAuthMethod.BOTH);
            securityDescriptor.setAuthMethods(authMethod);

            notificationConsumerEPR =
                notificationConsumerManager.createNotificationConsumer(
                topicPath,
                this,
                securityDescriptor);

            Subscribe request = new Subscribe();
            request.setConsumerReference(notificationConsumerEPR);
            TopicExpressionType topicExpression = new TopicExpressionType(
                WSNConstants.SIMPLE_TOPIC_DIALECT,
                ManagedJobConstants.RP_STATE);
            request.setTopicExpression(topicExpression);

            ManagedJobPortType jobPort
                = ManagedJobClientHelper.getPort(this.jobEndpointReference);
            setStubSecurityProperties((Stub) jobPort);

            SubscribeResponse response = jobPort.subscribe(request);
            this.notificationProducerEPR = response.getSubscriptionReference();
        }
        catch (Exception e) {
            //may happen...? Let's not fail.
            logger.error(e);
            try {
                unbind();
            }
            catch (Exception unbindEx) {
                //let's not fail the unbinding
                logger.error(unbindEx);
            }
        }
    }

    /**
    * Unregisters a callback listener for this job.
    * (disconnects from the job)
    * <b>Precondition</b> ClientNotificationConsumer.isListening()
    */
    public void unbind() throws NoSuchResourceException, Exception
    {
        //unsubscribe
        if (this.notificationProducerEPR != null) {
            SubscriptionManager subscriptionPort
                = new WSBaseNotificationServiceAddressingLocator().
                    getSubscriptionManagerPort(this.notificationProducerEPR);

            setStubSecurityProperties((Stub) subscriptionPort);

            if (logger.isInfoEnabled()) {
                logger.info("<startTime name=\"subscriptionDestroy\">"
                            +System.currentTimeMillis()
                            +"</startTime>");
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("Calling destroy on notificationProducerEPR:\n"
                    + ObjectSerializer.toString(
                        this.notificationProducerEPR,
                        org.apache.axis.message.addressing.Constants.
                            QNAME_ENDPOINT_REFERENCE));
            }
            subscriptionPort.destroy(new Destroy());
            if (logger.isInfoEnabled()) {
                logger.info("<endTime name=\"subscriptionDestroy\">"
                            +System.currentTimeMillis()
                            +"</endTime>");
            }
        }

        //stop notification consumer
        if (!notificationConsumerManager.isListening()) {
            String errorMessage = i18n.getMessage(
                Resources.PRECONDITION_VIOLATION,
                "!notificationConsumerManager.isListening()");
            throw new RuntimeException(errorMessage);
        }

        if (this.notificationConsumerEPR != null) {
            logger.debug("removing the notification consumer");
            notificationConsumerManager.removeNotificationConsumer(
                notificationConsumerEPR);
        }
        logger.debug("stopping the consumer manager from listening");
        notificationConsumerManager.stopListening();
    }

    /**
     * Precondition: isRequested()
     * Postcondition: isLocallyDestroyed()
     *
     * @throws GramException
     *         if error occurs during job service destruction.
     */
    public synchronized void destroy() throws Exception {
        if (! this.destroyed) {
            logger.debug("destroy() called");

            //fetch the job description needed for cleaning up the delegated
            //credential if this is a batch job
            if (isDelegationEnabled())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Job Description for job " + this.id
                                + " (BEFORE):\n" + toString());
                }

                getDescription();

                if (logger.isDebugEnabled())
                {
                    logger.debug("Job Description for job " + this.id
                                + " (AFTER):\n" + toString());
                }
            }

            try {
                if (  (this.notificationConsumerManager != null)
                    && this.notificationConsumerManager.isListening()) {
                    unbind();
                }
            }
            catch (NoSuchResourceException noSuchResEx) {
                String warnMessage = i18n.getMessage(
                    Resources.REMOTE_RESOURCE_DESTROY_ERROR);
                logger.warn(warnMessage, noSuchResEx);
                //not an error - the job may have
                //been automatically destroyed by soft state
            }
            ManagedJobPortType jobPort =
                ManagedJobClientHelper.getPort(this.jobEndpointReference);
            setStubSecurityProperties((Stub) jobPort);
            if (logger.isInfoEnabled()) {
                logger.info("<startTime name=\"destroy\">"
                            +System.currentTimeMillis()
                            +"</startTime>");
            }
            try {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Calling destroy on jobEndpointReference:\n"
                        + ObjectSerializer.toString(
                            this.jobEndpointReference,
                            org.apache.axis.message.addressing.Constants.
                                QNAME_ENDPOINT_REFERENCE));
                }

                jobPort.destroy(new Destroy());
            }
            catch (ResourceUnknownFaultType resUnknownFault) {
                String warnMessage = i18n.getMessage(
                    Resources.REMOTE_RESOURCE_DESTROY_ERROR);
                logger.warn(warnMessage, resUnknownFault);
                //not an error - the job may have
                //been automatically destroyed by soft state
            }
            if (logger.isInfoEnabled()) {
                logger.info("<endTime name=\"destroy\">"
                            +System.currentTimeMillis()
                            +"</endTime>");
            }

            if (isDelegationEnabled())
            {
                destroyDelegatedCredentials();
            }

            this.destroyed = true;

        } else {
            logger.warn("destroy() already called");
            //Do Nothing here
        }
    }

    private void destroyDelegatedCredentials()
        throws                              Exception
    {
        //destroy the job credential
        EndpointReferenceType jobCredentialEndpoint
            = this.jobDescription.getJobCredentialEndpoint();
        if (jobCredentialEndpoint != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Calling destroy on jobCredentialEndpoint:\n"
                    + ObjectSerializer.toString(
                        jobCredentialEndpoint,
                        org.apache.axis.message.addressing.Constants.
                            QNAME_ENDPOINT_REFERENCE));
            }

            destroyDelegatedCredential(jobCredentialEndpoint);
        }

        //not destroying staging credential because this client sets it
        //to the same value as the job credential

        destroyTransferDelegatedCredential(this.jobDescription);

        //destroy sub-job delegated credentials if multi-job
        if (this.jobDescription instanceof MultiJobDescriptionType)
        {
            JobDescriptionType[] subJobDescriptions
                = ((MultiJobDescriptionType)this.jobDescription).getJob();
            for (int index=0; index<subJobDescriptions.length; index++)
            {
                EndpointReferenceType subJobCredentialEndpoint
                    = subJobDescriptions[index].getJobCredentialEndpoint();
                if (jobCredentialEndpoint != null)
                {
                    destroyDelegatedCredential(subJobCredentialEndpoint);
                }

                destroyTransferDelegatedCredential( subJobDescriptions[index]);

            }
        }
    }

    private void destroyTransferDelegatedCredential(
        JobDescriptionType                  jobDescription)
        throws                              Exception
    {
        //set staging factory endpoints and delegate
        TransferRequestType stageOut = jobDescription.getFileStageOut();
        TransferRequestType stageIn = jobDescription.getFileStageIn();
        DeleteRequestType cleanUp = jobDescription.getFileCleanUp();
        EndpointReferenceType transferCredentialEndpoint = null;
        if (stageOut != null)
        {
            transferCredentialEndpoint
                = stageOut.getTransferCredentialEndpoint();
        } else if (stageIn != null)
        {
            transferCredentialEndpoint
                = stageIn.getTransferCredentialEndpoint();
        } else if (cleanUp != null)
        {
            transferCredentialEndpoint
                = cleanUp.getTransferCredentialEndpoint();
        }

        if (transferCredentialEndpoint != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Calling destroy on transferCredentialEndpoint for"
                            +" job " + this.id + ":\n"
                    + ObjectSerializer.toString(
                        transferCredentialEndpoint,
                        org.apache.axis.message.addressing.Constants.
                            QNAME_ENDPOINT_REFERENCE));
            }

            destroyDelegatedCredential(transferCredentialEndpoint);
        }
    }

    private void destroyDelegatedCredential(
        EndpointReferenceType               credentialEndpoint)
        throws                              Exception
    {
        DelegationPortType delegatedCredentialPort
            = new DelegationServiceAddressingLocator()
            .getDelegationPortTypePort(credentialEndpoint);
        setStubSecurityProperties((Stub) delegatedCredentialPort);
        try {
            if (logger.isInfoEnabled()) {
                logger.info("<startTime name=\"delegatedCredentialDestroy\">"
                            +System.currentTimeMillis()
                            +"</startTime>");
            }
            delegatedCredentialPort.destroy(new Destroy());
            if (logger.isInfoEnabled()) {
                logger.info("<endTime name=\"delegatedCredentialDestroy\">"
                            +System.currentTimeMillis()
                            +"</endTime>");
            }
        }
        catch (ResourceUnknownFaultType resUnknownFault) {
            logger.warn("Unable to destroy resource");
            if (logger.isDebugEnabled())
            {
                resUnknownFault.printStackTrace();
            }
            //not an error - the job may have
            //been automatically destroyed by soft state
        }
    }

    /**
     *
     * @return boolean true if #destroy() destroy() has been called
     */
    public synchronized boolean isLocallyDestroyed() {
        return this.destroyed;
    }

    public void release() throws Exception {

            ManagedJobPortType jobPort =
                ManagedJobClientHelper.getPort(this.jobEndpointReference);

            setStubSecurityProperties((Stub) jobPort);

            org.apache.axis.client.Stub s =
                (org.apache.axis.client.Stub)jobPort;
            s.setTimeout(this.axisStubTimeOut);
            logger.debug("setting timeout for Axis to " +
                         this.axisStubTimeOut + " ms");

            logger.debug("releasing ManagedJob from hold");
            jobPort.release(new ReleaseInputType());
    }

    /**
    * Sets the error code of the job.
    * Note: User should not use this method.
    *
    * @param code error code
    */
    protected void setError(int code) {
        this.error = error;
    }

    /**
    * Gets the error of the job.
    *
    * @return error number of the job.
    */
    public int getError() {
        return error;
    }

    /**
     * Return information about the cause of a job failure
     * (when <code>getStateAsString.equals(StateEnumeration._Failed)</code>)
     */
    public FaultType getFault() {
        return this.fault;
    }

    /**
     * <b>Precondition</b>: isRequested()
     */
    public EndpointReferenceType getEndpoint() {
        return this.jobEndpointReference;
    }

    public void setEndpoint(EndpointReferenceType endpoint)
        throws Exception
    {
        this.jobEndpointReference = endpoint;
    }

    public String getID()
    {
        return this.id;
    }

    /**
     * Can be used instead of {@link #getEndpointReference}
     * <b>Precondition</b>: isRequested()
     */
    public String getHandle() {
        if (this.jobHandle == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Generating handle from endpoint "
                            + this.jobEndpointReference);
            }
            this.jobHandle = ManagedJobClientHelper.getHandle( //ResolverUtils.getResourceHandle(
                this.jobEndpointReference);
            if (logger.isDebugEnabled()) {
                logger.debug("New handle: " + this.jobHandle);
            }
        }
        return this.jobHandle;
    }

    public int getExitCode() {
        return exitCode;
    }

    /**
     * Can be used instead of {@link #setEndpointReference}
     */
    public void setHandle(String handle) throws Exception {
        this.jobHandle = handle;
        this.jobEndpointReference = ManagedJobClientHelper.getEndpoint(handle);
        //ResolverUtils.resolve(this.jobResourceHandle);
        setSecurityTypeFromEndpoint(this.jobEndpointReference);
    }

    /**
     * Set timeout for HTTP socket. Default is 120000 (2 minutes).
     * @param timeout the timeout value, in milliseconds.
     */
    public void setTimeOut(int timeout) {
        this.axisStubTimeOut = timeout;
    }

    /**
    * Returns string representation of this job.
    *
    * @return string representation of this job. Useful for
    *         debugging.
    */
    public String toString() {
        String jobDescString = "RSL: ";
        JobDescriptionType jobDesc = null;
        try {
            jobDesc = this.getDescription();
        }
        catch (Exception e) {
            String errorMessage =
                    i18n.getMessage(Resources.FETCH_JOB_DESCRIPTION_ERROR);
            logger.error(errorMessage, e);
        }
        if (jobDesc != null) {
            jobDescString += RSLHelper.convertToString(jobDesc);
        }
        return jobDescString;
        /**
         * @todo print ID of job (resource key?)
         */
        }

    /**
     * Deliver the notification message
     *
     * @param topicPath The topic path for the topic that generated the
     *                  notification
     * @param producer  The producer endpoint reference
     * @param message   The notification message
     */
    public void deliver(
        List topicPath,
        EndpointReferenceType producer,
        Object message)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("receiving notification");
            if (message instanceof Element) {
                logger.debug("message is of type "
                            + message.getClass().getName());
                logger.debug("message contents: \n"
                            + XmlUtils.toString((Element) message));
            }
        }

        try {
            StateChangeNotificationMessageType changeNotification =
                (StateChangeNotificationMessageType)ObjectDeserializer.toObject(
                    (Element) message,
                    StateChangeNotificationMessageType.class);
            StateEnumeration state = changeNotification.getState();
            boolean holding = changeNotification.isHolding();
            if (state.equals(StateEnumeration.Failed)) {
                setFault(getFaultFromRP(changeNotification.getFault()));
            }
            if (   state.equals(StateEnumeration.StageOut)
                || state.equals(StateEnumeration.Done)
                || state.equals(StateEnumeration.Failed))
            {
                this.exitCode = changeNotification.getExitCode();

                if (logger.isDebugEnabled()) {
                    logger.debug("Setting exit code to "
                            + Integer.toString(exitCode));
                }
                if (logger.isInfoEnabled()) {
                    logger.info("<endTime name=\"submission\">"
                                +System.currentTimeMillis()
                                +"</endTime>");
                }
            }

            synchronized (this.stateMonitor) {
                setState(state, holding);
            }
        } catch (Exception e) {
            String errorMessage = "Notification message processing FAILED:" +
                "Could not get value or set new status.";
            logger.error(errorMessage, e);
            //no propagation of error here?
        }

    }

    private void setFault(FaultType fault)
        throws Exception
    {
        this.fault = fault;
    }

    private FaultType getFaultFromRP(FaultResourcePropertyType fault)
    {
        if (fault == null)
        {
            return null;
        }

        if (fault.getFault() != null) {
            return fault.getFault();
        } else if (fault.getCredentialSerializationFault() != null) {
            return fault.getCredentialSerializationFault();
        } else if (fault.getExecutionFailedFault() != null) {
            return fault.getExecutionFailedFault();
        } else if (fault.getFilePermissionsFault() != null) {
            return fault.getFilePermissionsFault();
        } else if (fault.getInsufficientCredentialsFault() != null) {
            return fault.getInsufficientCredentialsFault();
        } else if (fault.getInternalFault() != null) {
            return fault.getInternalFault();
        } else if (fault.getInvalidCredentialsFault() != null) {
            return fault.getInvalidCredentialsFault();
        } else if (fault.getInvalidPathFault() != null) {
            return fault.getInvalidPathFault();
        } else if (fault.getServiceLevelAgreementFault() != null) {
            return fault.getServiceLevelAgreementFault();
        } else if (fault.getStagingFault() != null) {
            return fault.getStagingFault();
        } else if (fault.getUnsupportedFeatureFault() != null) {
            return fault.getUnsupportedFeatureFault();
        } else {
            return null;
        }
    }

    private FaultType deserializeFaultRP(SOAPElement any)
        throws DeserializationException
    {
        return getFaultFromRP(
            (FaultResourcePropertyType) ObjectDeserializer.toObject(
                any, FaultResourcePropertyType.class));
    }

    /**
     * Asks the job service for its state,i.e. its state and
     * the cause if the state is 'Failed'. This is useful when subscribing
     * to notifications is impossible but an immediate result is needed.
     * <b>Precondition</b>job has been submitted
     * @throws Exception if the service data cannot be fetched or
     *                   the job state not extracted from the data.
     */
     public void refreshStatus() throws Exception
    {
        if (logger.isDebugEnabled()) {
            logger.debug("refreshing state of job with endpoint "
                        + this.jobEndpointReference);
        }

        boolean singleJob = isSingleJob();

        ManagedJobPortType jobPort =
            ManagedJobClientHelper.getPort(this.jobEndpointReference);

        setStubSecurityProperties((Stub) jobPort);

        //GetMultipleResourceProperties_Element request = new GetMultipleResourceProperties_Element();
        GetMultipleResourcePropertiesResponse request ;
        GetMultipleResourcePropertiesResponse response ;
    	if (singleJob)
        {
            logger.debug("Including exitCode in the RP query.");
            MessageElement[] message = new MessageElement[4];
            message[0] = new MessageElement(ManagedJobConstants.RP_STATE);
            message[1] = new MessageElement(ManagedJobConstants.RP_HOLDING);
            message[2] = new MessageElement(ManagedJobConstants.RP_FAULT);
            message[3] = new MessageElement(ManagedExecutableJobConstants.RP_EXIT_CODE);
            request = new GetMultipleResourcePropertiesResponse(message);
            /*request.setResourceProperty(new QName[] {
                ManagedJobConstants.RP_STATE,
                ManagedJobConstants.RP_HOLDING,
                ManagedJobConstants.RP_FAULT,
                ManagedExecutableJobConstants.RP_EXIT_CODE});
                response = jobPort.getMultipleResourceProperties(request);
                */
            response = jobPort.getMultipleResourceProperties(new QName[] {
                    ManagedJobConstants.RP_STATE,
                    ManagedJobConstants.RP_HOLDING,
                    ManagedJobConstants.RP_FAULT,
                    ManagedExecutableJobConstants.RP_EXIT_CODE});
        }
        else
        {
        	MessageElement[] message = new MessageElement[3];
            message[0] = new MessageElement(ManagedJobConstants.RP_STATE);
            message[1] = new MessageElement(ManagedJobConstants.RP_HOLDING);
            message[2] = new MessageElement(ManagedJobConstants.RP_FAULT);
            request = new GetMultipleResourcePropertiesResponse(message);
            /*request.setResourceProperty(new QName[] {
                ManagedJobConstants.RP_STATE,
                ManagedJobConstants.RP_HOLDING,
                ManagedJobConstants.RP_FAULT});
                response = jobPort.getMultipleResourceProperties(request);*/
            response = jobPort.getMultipleResourceProperties(new QName[] {
                    ManagedJobConstants.RP_STATE,
                    ManagedJobConstants.RP_HOLDING,
                    ManagedJobConstants.RP_FAULT});
        }
        
        SOAPElement [] any = response.get_any();
        if (logger.isInfoEnabled())
        {
            logger.info("Raw status query response message:\n"
                        + AnyHelper.toSingleString((MessageElement[]) any));
        }

        logger.debug("Deserializing \"state\".");
        StateEnumeration state =
            (StateEnumeration) ObjectDeserializer.
            toObject(any[0], StateEnumeration.class);

        logger.debug("Deserializing \"holding\".");
        Boolean holding
            = (Boolean) ObjectDeserializer.toObject(any[1], Boolean.class);

        int exitCodeIndex = 0;
        if (state.equals(StateEnumeration.Failed))
        {
            logger.debug("Deserializing \"fault\".");

            //set the fault
            FaultType fault = deserializeFaultRP(any[2]);
            this.setFault(fault);

            //where to find the exit code
            exitCodeIndex = 3;
        } else
        {
            //where to find the exit code
            exitCodeIndex = 2;
        }

        if (   (   state.equals(StateEnumeration.StageOut)
                || state.equals(StateEnumeration.Done)
                || state.equals(StateEnumeration.Failed))
            && (exitCodeIndex > 0)
            && singleJob
            && (any.length==(exitCodeIndex+1)))
        {
            logger.debug("Deserializing \"exitCode\".");

            Integer exitCodeWrapper
                = (Integer) ObjectDeserializer.
                toObject(any[exitCodeIndex], Integer.class);
            logger.debug("Fetched exitCode value is " + exitCodeWrapper);
            this.exitCode = exitCodeWrapper.intValue();
        }

        this.setState(state, holding.booleanValue());
    }

    /**
     * Gets submitted RSL from remote Managed Job Service.
     * It is actually not only the final, but substituted RSL.
     * To obtain it call <code>getRSLAttributes</code> afterwards.
     * <b>Precondition</b>job has been submitted
     */
    private void refreshRSLAttributes() throws Exception{
        ManagedJobPortType jobPort =
            ManagedJobClientHelper.getPort(this.jobEndpointReference);

        setStubSecurityProperties((Stub) jobPort);

        GetResourcePropertyResponse response = jobPort.getResourceProperty(
            ManagedJobConstants.RP_SERVICE_LEVEL_AGREEMENT);

        SOAPElement [] any = response.get_any();
        ServiceLevelAgreementType sla =
            (ServiceLevelAgreementType) ObjectDeserializer.
            toObject(any[0], ServiceLevelAgreementType.class);
        this.jobDescription = sla.getJob();
        if (this.jobDescription == null) {
            this.jobDescription = sla.getMultiJob();
        }
    }

    public static List getJobs(
        EndpointReferenceType               factoryEndpoint)
        throws                              Exception
    {
        throw new RuntimeException("NOT IMPLEMENTED YET");
    }

    private void setStubSecurityProperties(Stub stub)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("setting factory stub security...using authz method "
                        + getAuthorization());
        }

        ClientSecurityDescriptor secDesc = new ClientSecurityDescriptor();

        //set security type
        if (this.securityType.equals(Constants.GSI_SEC_MSG))
        {
            secDesc.setGSISecureMsg(this.getMessageProtectionType());
        } else
        {
            secDesc.setGSITransport(this.getMessageProtectionType());
        }

        //set authorization
        secDesc.setAuthz(getAuthorization());

        if (this.proxy != null) {
            //set proxy credential
            secDesc.setGSSCredential(this.proxy);
        }

        stub._setProperty(Constants.CLIENT_DESCRIPTOR,
                          secDesc);
    }

    public void setAuthorization(Authorization auth) {
        this.authorization = auth;
    }

    public Authorization getAuthorization() {
        return (authorization == null) ?
               DEFAULT_AUTHZ :
               this.authorization;
    }

    public void setMessageProtectionType(Integer protectionType) {
        this.msgProtectionType = protectionType;
    }

    public Integer getMessageProtectionType() {
        return (this.msgProtectionType == null) ?
               this.DEFAULT_MSG_PROTECTION :
               this.msgProtectionType;
    }

    public String getDelegationLevel() {
        return (this.limitedDelegation) ?
               GSIConstants.GSI_MODE_LIMITED_DELEG :
               GSIConstants.GSI_MODE_FULL_DELEG;
    }

    public void setDelegationEnabled(boolean delegationEnabled) {
        this.delegationEnabled = delegationEnabled;
    }

    public boolean isDelegationEnabled() {
        return this.delegationEnabled;
    }


    //============== REQUESTED TERMINATION TIME ================================

    /**
     * The default lifetime of the resource is 24 hours.
     *
     * @param duration the duration after which the job service should be
     *                 destroyed. The hours and minutes will be used.
     */
    public void setDuration(Date duration) {
        this.duration = duration;
    }

    /***
     * @param dateTime the date/time desired for termination of this job service
     */
    public void setTerminationTime(Date termTime) {
        this.terminationDate = termTime;
    }

    /**
     * get termination time of managed job service based on parameters
     * specified as JavaBean properties on this object.
     * <b>Precondition</b>job has been requested
     * @throws Exception
     * @return Calendar
     */
    private Calendar getServiceTerminationTime()
        throws Exception
    {
        Calendar terminationTime;

        if (this.duration == null &&
            this.terminationDate == null)
        {
            terminationTime = getDefaultTerminationTime();
        }
        else
        {
            terminationTime = Calendar.getInstance();

            if (this.duration != null) {
                //add duration to termination time
                Calendar durationCalendar = Calendar.getInstance();
                durationCalendar.setTime(this.duration);
                int hours = durationCalendar.get(Calendar.HOUR_OF_DAY);
                int minutes = durationCalendar.get(Calendar.MINUTE);
                terminationTime.add(Calendar.HOUR_OF_DAY, hours);
                terminationTime.add(Calendar.MINUTE, minutes);
            }
            else {
                terminationTime.setTime(this.terminationDate);
            }

        }

        return terminationTime;
    }


    /**
     * Set TerminationTime RP of managed job service based on parameters
     * specified as JavaBean properties on this object.
     * <b>Precondition</b>job has been requested
     * @throws Exception
     * @return Calendar
     */
    public void setServiceTerminationTime()
        throws Exception
    {
        Calendar terminationTime = getServiceTerminationTime();

        logger.debug("setting job resource duration");

        SetTerminationTime request = new SetTerminationTime();
        request.setRequestedTerminationTime(terminationTime);

        SetTerminationTimeResponse response =
            ManagedJobClientHelper.getPort(this.jobEndpointReference).
            setTerminationTime(request);

        if (logger.isDebugEnabled()) {
            Calendar newTermTime = response.getNewTerminationTime();
            logger.debug("requested: " + terminationTime.getTime());
            logger.debug("scheduled: " + newTermTime.getTime());
        }
    }

    public static final int DEFAULT_DURATION_HOURS = 24;

    private static void addDefaultDurationTo(Calendar currentTime) {
        currentTime.add(Calendar.HOUR, DEFAULT_DURATION_HOURS);
    }

    private static Calendar getDefaultTerminationTime()
    {
        Calendar timeOut = Calendar.getInstance();
        addDefaultDurationTo(timeOut);
        return timeOut;
        /**
         * @todo if no term time, infinite or 24 hours?
         */
    }

    public boolean isSingleJob()
    {
        AttributedURI address = this.jobEndpointReference.getAddress();
        String path = address.getPath();
        if (path.indexOf("ManagedExecutableJobService") > 0)
        {
            return true;
        }

        return false;
    }

    public boolean isMultiJob()
    {
        AttributedURI address = this.jobEndpointReference.getAddress();
        String path = address.getPath();
        if (path.indexOf("ManagedMultiJobService") > 0)
        {
            return true;
        }

        return false;
    }
}
