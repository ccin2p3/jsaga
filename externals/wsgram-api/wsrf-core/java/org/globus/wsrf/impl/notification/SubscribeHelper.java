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
package org.globus.wsrf.impl.notification;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.oasis.wsn.InvalidTopicExpressionFaultType;
import org.oasis.wsn.SubscribeCreationFailedFaultType;
import org.oasis.wsn.TopicNotSupportedFaultType;
import org.oasis.wsn.TopicPathDialectUnknownFaultType;
import org.oasis.wsn.Subscribe;

import org.ietf.jgss.GSSCredential;

import org.globus.gsi.jaas.JaasSubject;
import org.globus.security.gridmap.GridMap;
import org.globus.util.I18n;
import org.globus.wsrf.Constants;
import org.globus.wsrf.PersistenceCallback;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;
import org.globus.wsrf.TopicListenerList;
import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.impl.SimpleSubscriptionTopicListener;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.SecurityDescriptorException;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityDescriptor;
import org.globus.wsrf.security.SecureResource;
import org.globus.wsrf.security.SecurityException;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.topicexpression.UnsupportedTopicExpressionDialectException;
import org.globus.wsrf.utils.AddressingUtils;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.encoding.ObjectSerializer;

/**
 * Helper class to allow callers outside of SubscribeProvider to generate
 * new subscriptions.
 */
public class SubscribeHelper
{
    private static Log logger =
        LogFactory.getLog(SubscribeHelper.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private ResourceContext context;
    private Resource producerResource;
    private ResourceKey producerKey;
    private String producerHomeLocation;
    private String subscriptionManager;
    private Subscribe request;
    private SecurityManager securityManager;
    private ContainerSecurityConfig containerSecurityConfig = null;

    /**
     * Constructor
     *
     * @param context              The resource context of caller. Used to
     *                             detect security settings on the subscription
     *                             resource as well as notifications
     * @param producerResource     The producer resource
     * @param producerKey          The key for the producer resource
     * @param producerHomeLocation The location (JNDI) of the producer resource
     *                             home
     * @param request              The subscribe request
     */
    public SubscribeHelper(
        ResourceContext context, Resource producerResource,
        ResourceKey producerKey, String producerHomeLocation,
        Subscribe request)
    {
        this.context = context;
        this.producerResource = producerResource;
        this.producerKey = producerKey;
        this.producerHomeLocation = producerHomeLocation;
        this.subscriptionManager = getSubscriptionManagerServiceName();
        this.request = request;
        this.securityManager = SecurityManager.getManager();
        try
        {
            this.containerSecurityConfig = ContainerSecurityConfig.getConfig();
        }
        catch(ConfigException e)
        {
            logger.debug("Error trying to get container security config", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Create a new subscription. This method creates a new subscription based
     * on the information passed in the constructor. If this method is called
     * with a resource context created by a secure invocation the subscribe call
     * will set up the security of the subscription resource to require the same
     * authentication methods as the invocation and gridmap authorization with
     * the caller of the invoked method as the only allowed user. Furthermore,
     * it will also set up outgoing notifications to use the same security
     * mechanism that was used in the invocation associated with the resource
     * context.
     *
     * @return The WS-Addressing endpoint reference to the new subscription
     *         resource
     * @throws SubscribeCreationFailedFaultType
     *
     * @throws TopicNotSupportedFaultType
     * @throws TopicPathDialectUnknownFaultType
     *
     * @throws InvalidTopicExpressionFaultType
     */
    public EndpointReferenceType subscribe()
        throws SubscribeCreationFailedFaultType,
               TopicNotSupportedFaultType,
               TopicPathDialectUnknownFaultType,
               InvalidTopicExpressionFaultType
    {
        String subscriptionHomeLocation = null;
        SubscriptionHome subscriptionHome = null;

        if(request == null)
        {
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                i18n.getMessage("nullArgument", "request")
            );
            throw fault;
        }

        if(request.getTopicExpression() == null)
        {
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                i18n.getMessage("emptyTopicExpression")
            );
            throw fault;
        }

        EndpointReferenceType consumerReference =
            request.getConsumerReference();

        if(consumerReference == null)
        {
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                i18n.getMessage("emptyConsumerReference")
            );
            throw fault;
        }

        // clone the epr to remove references from any elements 
        // to the entire soap message, etc.
        try 
        {
            consumerReference = 
                (EndpointReferenceType)ObjectSerializer.clone(consumerReference);
        } 
        catch (Exception e)
        {
            // this should no occur but in case
            logger.error("", e);
        }

        if(logger.isDebugEnabled())
        {
            logger.debug(
                request.getTopicExpression().getDialect().getClass().toString());
        }

        try
        {
            Context initialContext = new InitialContext();
            subscriptionHomeLocation =
                Constants.JNDI_SERVICES_BASE_NAME +
                subscriptionManager +
                Constants.HOME_NAME;
            subscriptionHome = (SubscriptionHome)
                initialContext.lookup(subscriptionHomeLocation);
        }
        catch(Exception e)
        {
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(
                i18n.getMessage("resourceDisoveryFailed"));
            throw fault;
        }


        if(!(producerResource instanceof TopicListAccessor))
        {
            TopicNotSupportedFaultType fault =
                new TopicNotSupportedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                i18n.getMessage("noTopicList"));
            logger.debug("", fault);
            throw fault;
        }

        TopicList topicList =
            ((TopicListAccessor) producerResource).getTopicList();
        Collection topics;

        try
        {
            topics = topicList.getTopics(request.getTopicExpression());
        }
        catch(UnsupportedTopicExpressionDialectException e)
        {
            logger.debug("", e);
            TopicPathDialectUnknownFaultType fault =
                new TopicPathDialectUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        }
        catch(Exception e)
        {
            logger.debug("", e);
            InvalidTopicExpressionFaultType fault =
                new InvalidTopicExpressionFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(
                i18n.getMessage("topicExpressionResolutionFailed"));
            throw fault;
        }

        if(topics.isEmpty())
        {
            if(logger.isDebugEnabled())
            {
                Iterator it = topicList.topicIterator();
                String topicsString = "";
                while(it.hasNext())
                {
                    Object topicObj = it.next();
                    if(topicObj instanceof Topic)
                    {
                        Topic topic = (Topic) topicObj;
                        topicsString += "; " + "{" +
                                        topic.getName().getNamespaceURI() + "}"
                                        + topic.getName().getLocalPart();
                    }
                }
                if(request.getTopicExpression() != null)
                {
                    String expressionTopicString = "";
                    Object expressionValue =
                        request.getTopicExpression().getValue();
                    if(expressionValue instanceof QName)
                    {
                        expressionTopicString =
                        "{" + ((QName) expressionValue).getNamespaceURI() +
                        "}" + ((QName) expressionValue).getLocalPart();
                    }
                    logger.debug("no topics found matching expression " +
                                 request.getTopicExpression().getDialect() +
                                 "; " +
                                 expressionTopicString + "\n" +
                                 "avail. topics are: " + topicsString);
                }
            }
            throw new TopicNotSupportedFaultType();
        }

        EndpointReferenceType producerReference;

        try
        {
            producerReference =
                AddressingUtils.createEndpointReference(producerKey);
        }
        catch(Exception e)
        {
            logger.debug("", e);
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(
                i18n.getMessage("eprCreationFailed")
            );
            throw fault;
        }

        // Get security properties from message context
        ClientSecurityDescriptor desc = null;
        ResourceSecurityDescriptor resourceSecurityDescriptor = null;
        try
        {
            desc = setupNotificationSecurityDescriptor(consumerReference);
        }
        catch(SecurityException e)
        {
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(
                i18n.getMessage("secureNotificationSetupFailed")
            );
            throw fault;
        }

        //TODO: change conditional to something better: Only want to do the
        // below if someone is using security on the incoming connection
        if(desc != null)
        {
            try
            {
                resourceSecurityDescriptor = setupResourceSecurityDescriptor();
            }
            catch(Exception e)
            {
                SubscribeCreationFailedFaultType fault =
                    new SubscribeCreationFailedFaultType();
                FaultHelper faultHelper = new FaultHelper(fault);
                faultHelper.addFaultCause(e);
                faultHelper.setDescription(
                    i18n.getMessage("secureSubscriptionSetupFailed")
                );
                throw fault;
            }
        }

        ResourceKey subscriptionKey = null;
        try
        {
            //TODO: When/If we start using some of these other fields we need to
            // look at memory associated with the request tied to them
            subscriptionKey = subscriptionHome.create(
                        consumerReference,
                        producerReference,
                        request.getInitialTerminationTime(),
                        request.getSubscriptionPolicy(),
                        request.getPrecondition(),
                        request.getSelector(),
                        producerKey,
                        producerHomeLocation,
                        request.getTopicExpression(),
                        request.getUseNotify() == null ?
                        true : request.getUseNotify().booleanValue(), desc,
                        resourceSecurityDescriptor);
        }
        catch(SubscriptionCreationException e)
        {
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(
                i18n.getMessage("subscriptionCreateFailed")
            );
            throw fault;
        }

        SimpleSubscriptionTopicListener topicListener =
            new SimpleSubscriptionTopicListener(
                subscriptionKey,
                subscriptionHomeLocation);

        if(logger.isDebugEnabled())
        {
            logger.debug("Subscription is being added to " +
                         String.valueOf(topics.size()) + " topics");
        }

        Iterator topicIterator = topics.iterator();
        while(topicIterator.hasNext())
        {
            ((TopicListenerList) topicIterator.next()).addTopicListener(
                topicListener);
        }

        EndpointReferenceType reference = null;
        try
        {
            String address =
                ServiceHost.getBaseURL() + subscriptionManager;
            reference =
            AddressingUtils.createEndpointReference(address,
                                                    subscriptionKey);
            if(producerResource instanceof PersistenceCallback)
            {
                ((PersistenceCallback) producerResource).store();
            }
        }
        catch(Exception e)
        {
            logger.debug("", e);
            try
            {
                subscriptionHome.remove(subscriptionKey);
            }
            catch(Exception e1)
            {
                logger.error(i18n.getMessage("subscriptionRemoveFailed"), e1);
            }
            SubscribeCreationFailedFaultType fault =
                new SubscribeCreationFailedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            faultHelper.setDescription(
                i18n.getMessage("eprCreationFailed")
            );
            throw fault;
        }

        return reference;
    }

    /**
     * Set up the resource descriptor to be used on the resource.
     *
     * @throws SecurityException
     * @throws ConfigException
     * @throws SecurityDescriptorException
     */
    protected ResourceSecurityDescriptor setupResourceSecurityDescriptor()
        throws SecurityException, ConfigException, SecurityDescriptorException
    {
        ServiceSecurityDescriptor serviceSecurityDescriptor =
            ServiceSecurityConfig.getSecurityDescriptor();
        ContainerSecurityDescriptor containerSecurityDescriptor =
            containerSecurityConfig.getSecurityDescriptor();
        ResourceSecurityDescriptor desc = null;
        boolean setAuth = false;
        List authMethods = null;
        QName operationName = (QName) context.getProperty(
            org.globus.wsrf.impl.security.authentication.Constants.OPERATION_NAME);

        if(producerResource instanceof SecureResource)
        {
            desc = createResourceSecurityDescriptor();
            ResourceSecurityDescriptor producerSecurityDescriptor =
                ((SecureResource) producerResource).getSecurityDescriptor();

            if(producerSecurityDescriptor != null)
            {
                if(operationName != null)
                {
                    authMethods = producerSecurityDescriptor.getAuthMethods(
                        operationName);
                }
                if(authMethods != null)
                {
                    desc.setAuthMethods(authMethods);
                    setAuth = true;
                }

                authMethods =
                    producerSecurityDescriptor.getDefaultAuthMethods();
                if(!setAuth && authMethods != null)
                {
                    desc.setAuthMethods(authMethods);
                    setAuth = true;
                }
            }
        }
        if(!setAuth && serviceSecurityDescriptor != null)
        {
            desc = createResourceSecurityDescriptor();
            if(operationName != null)
            {
                authMethods = serviceSecurityDescriptor.getAuthMethods(
                    operationName);
            }
            if(authMethods != null)
            {
                desc.setAuthMethods(authMethods);
                setAuth = true;
            }

            authMethods =
                serviceSecurityDescriptor.getDefaultAuthMethods();
            if(!setAuth && authMethods != null)
            {
                desc.setAuthMethods(authMethods);
                setAuth = true;
            }
        }

        if(desc == null && containerSecurityDescriptor != null)
        {
            desc = createResourceSecurityDescriptor();
        }

        return desc;
    }

    /**
     * Create a new resource security descriptor and fill in the subject and
     * authorization bits
     *
     * @throws SecurityException
     */
    private ResourceSecurityDescriptor createResourceSecurityDescriptor()
        throws SecurityException
    {
        ResourceSecurityDescriptor desc = new ResourceSecurityDescriptor();
        Subject subject = JaasSubject.getCurrentSubject();
        if(subject == null)
        {
            subject = securityManager.getSubject(producerResource);
        }

        desc.setSubject(subject);
        String peer = securityManager.getCaller();
        if(peer != null)
        {
            GridMap gridmap = new GridMap();
            gridmap.map(peer, System.getProperty("user.name"));
            desc.setGridMap(gridmap);
            desc.setAuthz(Authorization.AUTHZ_GRIDMAP);
        }
        return desc;
    }

    /**
     * Set up the client security descriptor used for sending notifications.
     *
     * @throws SecurityException
     * @param consumerEPR
     */
    protected ClientSecurityDescriptor setupNotificationSecurityDescriptor(
        EndpointReferenceType consumerEPR)
        throws SecurityException
    {
        ClientSecurityDescriptor desc = null;
        Integer protLevel = null;
        boolean secureMessage = false;
        boolean secureConversation = false;

        protLevel = (Integer) context.getProperty(
            org.globus.wsrf.security.Constants.GSI_SEC_MSG);

        if(protLevel == null)
        {
            protLevel = (Integer) context.getProperty(
                org.globus.wsrf.security.Constants.GSI_SEC_CONV);
            if(protLevel == null)
            {
                protLevel = (Integer) context.getProperty(
                    org.globus.wsrf.security.Constants.GSI_TRANSPORT);
            }
            else
            {
                secureConversation = true;
            }
        }
        else
        {
            secureMessage = true;
        }

        if(consumerEPR.getAddress().getScheme().equals("https"))
        {
            desc = new ClientSecurityDescriptor();
            desc.setGSITransport(
                protLevel == null?
                org.globus.wsrf.security.Constants.SIGNATURE : protLevel);
        }
        else
        {
            if (protLevel != null && secureMessage)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Secure message " + protLevel);
                }

                desc = new ClientSecurityDescriptor();
                desc.setGSISecureMsg((Integer)protLevel);
            }

            if (protLevel != null && secureConversation)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Secure conversation " + protLevel);
                }
                if (desc == null)
                {
                    desc = new ClientSecurityDescriptor();
                }
                desc.setGSISecureConv((Integer)protLevel);
            }
        }

        if(desc != null)
        {
            Subject currentSubject = JaasSubject.getCurrentSubject();

            if(currentSubject == null)
            {
                currentSubject = securityManager.getSubject(producerResource);
            }

            if(currentSubject != null)
            {
                Set privateCreds =
                    currentSubject.getPrivateCredentials(GSSCredential.class);
                if(privateCreds != null && !privateCreds.isEmpty())
                {
                    Iterator iterator = privateCreds.iterator();
                    GSSCredential cred = (GSSCredential)iterator.next();
                    desc.setGSSCredential(cred);
                }
            }
        }
        //Todo: set up authorization to identity of subscriber?

        return desc;
    }

    protected String getSubscriptionManagerServiceName()
    {
        return Constants.SUBSCRIPTION_MANAGER_SERVICE_NAME;
    }
}
