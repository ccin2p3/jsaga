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
import java.util.List;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.NotificationConsumerCallbackManager;
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.topicexpression.TopicExpressionEngine;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.impl.TopicExpressionEngineImpl;
import org.globus.util.I18n;

import org.oasis.wsn.ResourceUnknownFaultType;
import org.oasis.wsn.NotificationMessageHolderType;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsn.Notify;

public class NotificationConsumerProvider
{
    static Log logger =
        LogFactory.getLog(NotificationConsumerProvider.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    /**
     * Provider for the notify operation, ie. the notification consumer
     * porttype.
     *
     * @param request
     * @throws RemoteException
     */
    public void notify(Notify request) throws RemoteException
    {
        NotificationMessageHolderType[] notifications =
            request.getNotificationMessage();
        TopicExpressionType topicExpression;
        EndpointReferenceType producer;
        List topicPath;
        Object message;
        NotifyCallback callback;
        NotifyCallback defaultCallback;

        Object resource = null;
        try
        {
            resource = ResourceContext.getResourceContext().getResource();
        }
        catch (NoSuchResourceException e)
        {
            throw new ResourceUnknownFaultType();
        }
        catch(Exception e)
        {
            throw new RemoteException(
                i18n.getMessage("resourceDisoveryFailed"), e);
        }

        NotificationConsumerCallbackManager manager =
            (NotificationConsumerCallbackManager) resource;

        TopicExpressionEngine topicExpressionEngine =
            TopicExpressionEngineImpl.getInstance();
        defaultCallback = manager.getCallback(null);

        if(logger.isDebugEnabled())
        {
            logger.debug(
                "Invoked with " +
                String.valueOf(notifications.length) + " notification message(s)");
        }

        for(int i = 0; i < notifications.length; i++)
        {
            topicExpression = notifications[i].getTopic();
            producer = notifications[i].getProducerReference();
            message = notifications[i].getMessage();

            try
            {
                topicPath = topicExpressionEngine.getConcretePath(
                    topicExpression);
                callback = manager.getCallback(topicPath);

                if(callback == null)
                {
                    callback = defaultCallback;
                }

                if(callback != null)
                {
                    callback.deliver(topicPath,
                                     producer,
                                     message);
                }

                if(logger.isDebugEnabled())
                {
                    logger.debug("Got notification on topic: " +
                                 topicExpression.toString());
                    logger.debug(
                        "With dialect " + topicExpression.getDialect().toString());
                    logger.debug("From producer at " + producer.getAddress());
                    if(producer.getProperties() != null &&
                       producer.getProperties().get_any() != null)
                    {
                        String value =
                            AnyHelper.toSingleString(producer.getProperties());
                        logger.debug("With resource ids " + value);
                    }
                    logger.debug("Notification message: " + message);
                }
            }
            catch(Exception e)
            {
                throw new RemoteException(
                    i18n.getMessage("notifyCallbackError"), e);
            }
        }
    }
}
