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

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.oasis.wsn.ResourceUnknownFaultType;
import org.oasis.wsn.SubscribeCreationFailedFaultType;
import org.oasis.wsn.TopicPathDialectUnknownFaultType;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.SubscribeResponse;
import org.oasis.wsn.InvalidTopicExpressionFaultType;
import org.oasis.wsn.TopicNotSupportedFaultType;

import org.globus.util.I18n;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.impl.ResourceContextImpl;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.utils.Resources;

public class SubscribeProvider
{
    private static Log logger =
        LogFactory.getLog(SubscribeProvider.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    /**
     * Provider for the subscribe operation of the notification producer
     * porttype
     *
     * @param request
     * @return The endpoint of Subscription resource.
     * @throws RemoteException
     * @throws ResourceUnknownFaultType
     * @throws SubscribeCreationFailedFaultType
     *
     * @throws TopicPathDialectUnknownFaultType
     *
     */
    public SubscribeResponse subscribe(Subscribe request)
        throws RemoteException,
               ResourceUnknownFaultType,
               SubscribeCreationFailedFaultType,
               TopicPathDialectUnknownFaultType,
               InvalidTopicExpressionFaultType,
               TopicNotSupportedFaultType
    {
        Resource resource = null;
        ResourceKey key = null;
        String homeLocation = null;
        ResourceContext ctx = null;

        try
        {
            ctx = ResourceContext.getResourceContext();
            key = ctx.getResourceKey();
            resource = ctx.getResource();
            // FIXME: use service name instead?
            homeLocation =
                ((ResourceContextImpl)ctx).getResourceHomeLocation();
        }
        catch (NoSuchResourceException e)
        {
            ResourceUnknownFaultType fault = new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
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

        EndpointReferenceType reference = getSubscribeHelper(
            ctx, resource, key, homeLocation, request).subscribe();
        SubscribeResponse response = new SubscribeResponse();
        response.setSubscriptionReference(reference);
        return response;
    }

    protected SubscribeHelper getSubscribeHelper(
        ResourceContext context, Resource producerResource,
        ResourceKey producerKey, String producerHomeLocation,
        Subscribe request)
    {
        return new SubscribeHelper(context, producerResource, producerKey,
                                   producerHomeLocation, request);
    }
}
