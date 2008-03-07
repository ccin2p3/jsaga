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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.QueryExpressionType;

import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;
import org.globus.wsrf.PersistenceCallback;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceIdentifier;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.Subscription;
import org.globus.wsrf.impl.ResourceHomeImpl;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

public class SubscriptionHome extends ResourceHomeImpl
{
    private static Log logger =
        LogFactory.getLog(SubscriptionHome.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private Constructor resourceConstructor;

    public synchronized void initialize() throws Exception
    {
        super.initialize();
        this.resourceConstructor =
            this.resourceClass.getConstructor(
                new Class[] {EndpointReferenceType.class,
                             EndpointReferenceType.class,
                             Calendar.class,
                             Object.class,
                             QueryExpressionType.class,
                             QueryExpressionType.class,
                             ResourceKey.class,
                             String.class,
                             TopicExpressionType.class,
                             boolean.class,
                             boolean.class,
                             ClientSecurityDescriptor.class,
                             ResourceSecurityDescriptor.class});
    }

    /**
     * Create a subscription resource
     *
     * @param consumerReference              The EPR of the notification
     *                                       consumer
     * @param producerReference              The EPR of the producer resource
     * @param initialTerminationTime         The initial termination time for
     *                                       the subscription
     * @param subscriptionPolicy             The policy associciated with the
     *                                       subscription
     * @param precondition                   The precondition associciated with
     *                                       the subscription
     * @param selector                       The selector associciated with the
     *                                       subscription
     * @param producerKey                    The key for the producer resource
     * @param producerHomeLocation           The JNDI path for the producer
     *                                       resource's resource home
     * @param topicPathExpression            The topic path expression for this
     *                                       subscription
     * @param useNotify                      Flag indicating if notifications
     *                                       should be wrapped
     * @param notificationSecurityDescriptor The security settings to use when
     *                                       sending notifications for this
     *                                       subscription
     * @param resourceSecurityDescriptor     The resource security descriptor to
     *                                       apply to the created resource
     * @return The resource key for the created resource
     * @throws SubscriptionCreationException If the create fails
     */
    public ResourceKey create(
        EndpointReferenceType consumerReference,
        EndpointReferenceType producerReference,
        Calendar initialTerminationTime,
        Object subscriptionPolicy,
        QueryExpressionType precondition,
        QueryExpressionType selector,
        ResourceKey producerKey,
        String producerHomeLocation,
        TopicExpressionType topicPathExpression,
        boolean useNotify,
        ClientSecurityDescriptor notificationSecurityDescriptor,
        ResourceSecurityDescriptor resourceSecurityDescriptor)
        throws SubscriptionCreationException
    {
        Subscription subscriptionResource = null;

        try
        {
            subscriptionResource = createSubscription(
                consumerReference,
                producerReference,
                initialTerminationTime,
                subscriptionPolicy,
                precondition,
                selector,
                producerKey,
                producerHomeLocation,
                topicPathExpression,
                useNotify,
                notificationSecurityDescriptor,
                resourceSecurityDescriptor);
        }
        catch(Exception e)
        {
            throw new SubscriptionCreationException(
                i18n.getMessage("subscriptionCreateFailed"),e);
        }

        if(subscriptionResource instanceof PersistenceCallback)
        {
            try
            {
                ((PersistenceCallback) subscriptionResource).store();
            }
            catch(ResourceException e)
            {
                throw new SubscriptionCreationException(
                    i18n.getMessage("subscriptionCreateFailed"),e);
            }
        }

        ResourceKey key = new SimpleResourceKey(
            this.keyTypeName,
            ((ResourceIdentifier) subscriptionResource).getID());

        this.add(key, subscriptionResource);
        return key;
    }

    /**
     *
     * @param consumerReference
     * @param producerReference
     * @param initialTerminationTime
     * @param subscriptionPolicy
     * @param precondition
     * @param selector
     * @param producerKey
     * @param producerHomeLocation
     * @param topicPathExpression
     * @param useNotify
     * @param notificationSecurityDescriptor
     * @param resourceSecurityDescriptor
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    protected Subscription createSubscription(
        EndpointReferenceType consumerReference,
        EndpointReferenceType producerReference,
        Calendar initialTerminationTime,
        Object subscriptionPolicy,
        QueryExpressionType precondition,
        QueryExpressionType selector,
        ResourceKey producerKey,
        String producerHomeLocation,
        TopicExpressionType topicPathExpression,
        boolean useNotify,
        ClientSecurityDescriptor notificationSecurityDescriptor,
        ResourceSecurityDescriptor resourceSecurityDescriptor)
            throws NoSuchMethodException, IllegalAccessException,
                   InvocationTargetException, InstantiationException
    {
        return (Subscription) this.resourceConstructor.newInstance(
            new Object [] {consumerReference,
                           producerReference,
                           initialTerminationTime,
                           subscriptionPolicy,
                           precondition,
                           selector,
                           producerKey,
                           producerHomeLocation,
                           topicPathExpression,
                           Boolean.FALSE,
                           (useNotify) ? Boolean.TRUE : Boolean.FALSE,
                           notificationSecurityDescriptor,
                           resourceSecurityDescriptor});
    }
}
