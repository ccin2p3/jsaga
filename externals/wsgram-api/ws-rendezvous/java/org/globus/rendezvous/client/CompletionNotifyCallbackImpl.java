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
package org.globus.rendezvous.client;

import java.util.List;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

// To be able to refer to Rendezvous services
import org.globus.wsrf.utils.AddressingUtils;
import org.globus.wsrf.encoding.ObjectDeserializer;

//security
import javax.xml.rpc.Stub;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.rendezvous.client.ClientSecurityStrategy;

import org.globus.rendezvous.client.RendezvousConstants;
import org.globus.rendezvous.client.RendezvousHelper;
import org.globus.rendezvous.generated.RendezvousPortType;
import org.globus.rendezvous.generated.service.RendezvousServiceAddressingLocator;
import org.globus.rendezvous.generated.RendezvousResourceProperties;
import org.globus.rendezvous.generated.StateChangeNotificationMessageType;

//For being a resource that accepts notifications
import org.globus.wsrf.NotifyCallback;
import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.core.notification.ResourcePropertyValueChangeNotificationElementType;
import org.oasis.wsn.Subscribe;
import org.oasis.wsn.TopicExpressionType;
import org.oasis.wsrf.properties.ResourcePropertyValueChangeNotificationType;

//To query master's byte array value
import org.oasis.wsrf.properties.GetResourcePropertyResponse;

// For being a resource that supports notifications
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.Topic;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.TopicListAccessor;

import org.globus.wsrf.impl.ReflectionResource;
import org.globus.wsrf.impl.ReflectionResourceProperty;
import org.globus.wsrf.impl.ResourcePropertyTopic;
import org.globus.wsrf.impl.SimpleResourcePropertySet;
import org.globus.wsrf.impl.SimpleTopicList;
import org.globus.wsrf.impl.SimpleTopic;
import org.globus.wsrf.impl.SimpleResourceProperty;
import org.globus.wsrf.utils.XmlUtils;

import org.globus.util.I18n;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.utils.PerformanceLog;
import org.globus.util.I18n;

/**
 * Derive this class in order to inherit behavior on rendezvous completion
 * notifications.
 */
public abstract class CompletionNotifyCallbackImpl
    implements NotifyCallback
{

    public CompletionNotifyCallbackImpl(ClientSecurityStrategy security) {
        this.security = security;
    }

    public void deliver(List topicPath,
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
         StateChangeNotificationMessageType changeNotification;
         try {
             changeNotification =
                 (StateChangeNotificationMessageType) ObjectDeserializer.
                 toObject(
                 (Element) message,
                 StateChangeNotificationMessageType.class);
          }
          catch (Exception e) {
              String errorMessage =
                  i18n.getMessage(Resources.NOTIFICATION_DESERIALIZATION_ERROR);
              logger.error(errorMessage, e);
              throw new RuntimeException(e);
          }

          if (!changeNotification.isRendezvousCompleted()) {
              throw new RuntimeException(
                  "notification for rendezvous: not complete!");
          }
          byte[] bytes = changeNotification.getRegistrantData();

          if (logger.isDebugEnabled()) {
              String wholeDataString = "";
              for (int i = 0; i < bytes.length; i++) {
                  wholeDataString = wholeDataString + bytes[i] + " ";
              }
              logger.debug("Value of received binary data: " +
                           wholeDataString);
          }

          onRendezvousCompleted(bytes);
    }

    abstract public void onRendezvousCompleted(byte[] data);


    //==========================================================================
    // Security:

    private ClientSecurityStrategy security;


   private static final Log logger =
        LogFactory.getLog(CompletionNotifyCallbackImpl.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private static PerformanceLog performanceLogger = new PerformanceLog(
        CompletionNotifyCallbackImpl.class.getName() + ".performance");


}
