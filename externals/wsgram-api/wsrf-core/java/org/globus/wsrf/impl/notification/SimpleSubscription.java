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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.EndpointReferenceType;

import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.QueryExpressionType;

import org.globus.wsrf.RemoveCallback;
import org.globus.wsrf.PersistenceCallback;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceIdentifier;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceLifetime;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.Subscription;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;
import org.globus.wsrf.TopicListener;
import org.globus.wsrf.TopicListenerList;
import org.globus.wsrf.WSNConstants;
import org.globus.wsrf.impl.ReflectionResourceProperty;
import org.globus.wsrf.impl.SimpleResourcePropertyMetaData;
import org.globus.wsrf.impl.SimpleResourcePropertySet;
import org.globus.wsrf.impl.SimpleSubscriptionTopicListener;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.security.SecureResource;

/**
 * Simple in-memory implementation of a subscription resource
 */
public class SimpleSubscription implements Subscription,
                                           ResourceProperties,
                                           ResourceIdentifier,
                                           ResourceLifetime,
                                           RemoveCallback,
                                           SecureResource,
                                           Serializable
{
    static Log logger =
        LogFactory.getLog(SimpleSubscription.class.getName());
    public static final QName RP_SET =
        new QName(WSNConstants.BASEN_NS,
                  "SubscriptionManagerRP");

    protected EndpointReferenceType consumerReference;
    protected EndpointReferenceType producerReference;
    protected Object policy;
    protected QueryExpressionType precondition;
    protected QueryExpressionType selector;
    protected ResourceKey producerKey;
    protected String producerHomeLocation;
    protected TopicExpressionType topicExpression;
    protected boolean isPaused;
    protected boolean useNotify;
    protected Calendar terminationTime;
    protected Calendar creationTime;
    protected ClientSecurityDescriptor securityDescriptor;
    protected String id = null;
    protected ResourceSecurityDescriptor resourceSecurityDescriptor;

    transient private ResourcePropertySet propertySet;

    private static final UUIDGen uuidGen =
        UUIDGenFactory.getUUIDGen();

    public ResourcePropertySet getResourcePropertySet()
    {
        return this.propertySet;
    }

    public void setTerminationTime(Calendar time)
    {
        this.terminationTime = time;
    }

    public Calendar getTerminationTime()
    {
        return this.terminationTime;
    }

    public Calendar getCurrentTime()
    {
        return Calendar.getInstance();
    }

    public Calendar getCreationTime()
    {
        return this.creationTime;
    }

    public EndpointReferenceType getConsumerReference()
    {
        return this.consumerReference;
    }

    public Object getSubscriptionPolicy()
    {
        return this.policy;
    }

    public QueryExpressionType getPrecondition()
    {
        return this.precondition;
    }

    public EndpointReferenceType getProducerReference()
    {
        return this.producerReference;
    }

    public Object getResource() throws Exception
    {
        Context initialContext = new InitialContext();
        ResourceHome producerHome = (ResourceHome) initialContext.lookup(
            this.producerHomeLocation);
        return producerHome.find(this.producerKey);
    }

    public QueryExpressionType getSelector()
    {
        return this.selector;
    }

    public TopicExpressionType getTopicExpression()
    {
        return topicExpression;
    }

    public boolean isPaused()
    {
        return this.isPaused;
    }

    public void pause() throws Exception
    {
        this.isPaused = true;
    }

    public void resume() throws Exception
    {
        this.isPaused = false;
    }

    public boolean getUseNotify()
    {
        return this.useNotify;
    }

    public ClientSecurityDescriptor getSecurityProperties()
    {
        return this.securityDescriptor;
    }


    public SimpleSubscription()
    {
        this(null, null, null, null, null, null,
             null, null, null, false, true, null, null);
    }

    /**
     * Construct a new subscription resource.
     *
     * @param consumerReference              The WS-Addressing endpoint
     *                                       reference of the consumer
     * @param producerReference              The WS-Addressing endpoint
     *                                       reference of the producer
     * @param initialTerminationTime         The initial termination time of
     *                                       this resource
     * @param policy                         The subscription policy
     * @param precondition                   The precondition
     * @param selector                       The selector
     * @param producerKey                    The key of the producer resource
     * @param producerHomeLocation           The JNDI location of the home of
     *                                       the producer resource
     * @param topicExpression                The topic expression for this
     *                                       subscription
     * @param isPaused                       The initial pause/resume state
     * @param useNotify                      Whether to use raw notifications or
     *                                       not.
     * @param notificationSecurityDescriptor Security settings for notify
     * @param resourceSecurityDescriptor     Security settings for this
     *                                       subscription resource
     */
    public SimpleSubscription(
        EndpointReferenceType consumerReference,
        EndpointReferenceType producerReference,
        Calendar initialTerminationTime,
        Object policy,
        QueryExpressionType precondition,
        QueryExpressionType selector,
        ResourceKey producerKey,
        String producerHomeLocation,
        TopicExpressionType topicExpression,
        boolean isPaused,
        boolean useNotify,
        ClientSecurityDescriptor notificationSecurityDescriptor,
        ResourceSecurityDescriptor resourceSecurityDescriptor)
    {
        this.id = uuidGen.nextUUID();
        this.terminationTime = initialTerminationTime;
        this.consumerReference = consumerReference;
        this.producerReference = producerReference;
        this.policy = policy;
        this.precondition = precondition;
        this.selector = selector;
        this.producerKey = producerKey;
        this.producerHomeLocation = producerHomeLocation;
        this.topicExpression = topicExpression;
        this.isPaused = isPaused;
        this.useNotify = useNotify;
        this.creationTime = Calendar.getInstance();
        this.securityDescriptor = notificationSecurityDescriptor;
        this.propertySet = new SimpleResourcePropertySet(RP_SET);
        this.resourceSecurityDescriptor = resourceSecurityDescriptor;
        ResourceProperty property = null;

        if(this.consumerReference != null &&
           this.consumerReference.getAddress().
            getScheme().equalsIgnoreCase("https") &&
           this.securityDescriptor == null)
        {
            this.securityDescriptor = new ClientSecurityDescriptor();
        }

        try
        {
            property =
                new ReflectionResourceProperty(WSNConstants.CONSUMER_REFERENCE,
                                               this);
            this.propertySet.add(property);
            property =
                new ReflectionResourceProperty(WSNConstants.SELECTOR,
                                               this);
            this.propertySet.add(property);
            property =
                new ReflectionResourceProperty(WSNConstants.USE_NOTIFY,
                                               this);
            this.propertySet.add(property);
            property =
                new ReflectionResourceProperty(WSNConstants.TOPIC_EXPRESSION,
                                               this);
            this.propertySet.add(property);
            property =
                new ReflectionResourceProperty(WSNConstants.SUBSCRIPTION_POLICY,
                                               this);
            this.propertySet.add(property);
            property = new ReflectionResourceProperty(
                             SimpleResourcePropertyMetaData.TERMINATION_TIME,
                             this);
            this.propertySet.add(property);
            property =
                new ReflectionResourceProperty(WSNConstants.CREATION_TIME,
                                               this);
            this.propertySet.add(property);
            property =
                new ReflectionResourceProperty(WSNConstants.PRECONDITION,
                                               this);
            this.propertySet.add(property);
            property = new ReflectionResourceProperty(
                             SimpleResourcePropertyMetaData.CURRENT_TIME,
                             this);
            this.propertySet.add(property);
        }
        catch(Exception e)
        {
            logger.debug("Failed to set up resource properties", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public Object getID()
    {
        return this.id;
    }

    public void remove() throws ResourceException
    {
        Object resource = null;
        try
        {
            resource = getResource();
        }
        catch(Exception e)
        {
            throw new ResourceException("", e);
        }

        if (!(resource instanceof TopicListAccessor))
        {
            return;
        }

        TopicList topicList = ((TopicListAccessor)resource).getTopicList();
        Collection topics = null;
        try
        {
            topics = topicList.getTopics(this.topicExpression);
        }
        catch(Exception e)
        {
            throw new ResourceException("", e);
        }

        synchronized(resource) {
            boolean removed = removeListener(topics);
            if (removed && resource instanceof PersistenceCallback) {
                ((PersistenceCallback)resource).store();
            }
        }
    }

    private boolean removeListener(Collection topics) {
        boolean removed = false;

        Iterator topicIterator = topics.iterator();
        TopicListenerList topicListenerList;
        Iterator topicListenerIterator;
        TopicListener listener;

        while(topicIterator.hasNext())
        {
            topicListenerList = (TopicListenerList) topicIterator.next();
            synchronized(topicListenerList)
            {
                topicListenerIterator =
                    topicListenerList.topicListenerIterator();
                while(topicListenerIterator.hasNext())
                {
                    listener = (TopicListener)topicListenerIterator.next();
                    if (listener instanceof SimpleSubscriptionTopicListener)
                    {
                        SimpleSubscriptionTopicListener lt =
                            (SimpleSubscriptionTopicListener)listener;
                        Object subKey =
                            lt.getSubscriptionResourceKey().getValue();
                        if (subKey.equals(this.id))
                        {
                            topicListenerIterator.remove();
                            removed = true;
                        }
                    }
                }
            }
        }

        return removed;
    }

    public ResourceSecurityDescriptor getSecurityDescriptor()
    {
        return this.resourceSecurityDescriptor;
    }
}
