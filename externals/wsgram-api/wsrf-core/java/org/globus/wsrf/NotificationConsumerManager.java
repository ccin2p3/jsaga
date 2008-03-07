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
package org.globus.wsrf;

import java.net.URL;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.MessageContext;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.message.addressing.ReferencePropertiesType;
import org.apache.axis.server.AxisServer;

import org.globus.util.I18n;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.notification.ClientNotificationConsumerManager;
import org.globus.wsrf.impl.notification.NotificationConsumerHome;
import org.globus.wsrf.impl.notification.ServerNotificationConsumerManager;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.utils.AddressingUtils;
import org.globus.wsrf.utils.Resources;

public abstract class NotificationConsumerManager {

    private static Log logger =
        LogFactory.getLog(NotificationConsumerManager.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    protected NotificationConsumerHome notificationConsumerHome;
    protected Map consumers;

    protected NotificationConsumerManager() {
        this.consumers = new HashMap();
    }

    /**
     * Returns an instance of <code>NotificationConsumerManager</code>.
     */
    public synchronized static NotificationConsumerManager getInstance() {
        // must return a new instance each time
        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx != null &&
            ctx.getProperty(MessageContext.TRANS_URL) != null &&
            ctx.getAxisEngine() instanceof AxisServer) {
            return new ServerNotificationConsumerManager();
        } else {
            return new ClientNotificationConsumerManager();
        }
    }

    /**
     * Returns an instance of <code>NotificationConsumerManager</code>.
     */
    public synchronized static NotificationConsumerManager getInstance(
        Map properties) {
        // must return a new instance each time
        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx != null &&
            ctx.getProperty(MessageContext.TRANS_URL) != null &&
            ctx.getAxisEngine() instanceof AxisServer) {
            // Ignoring properties on the server side,
            // maybe throw exception instead?
            return new ServerNotificationConsumerManager();
        } else {
            return new ClientNotificationConsumerManager(properties);
        }
    }

    protected void initializeConsumerHome() throws ContainerException {
        try {
            Context initialContext = new InitialContext();
            this.notificationConsumerHome =
            (NotificationConsumerHome) initialContext.lookup(
                Constants.JNDI_SERVICES_BASE_NAME +
                getNotificationConsumerServiceName() +
                Constants.HOME_NAME
            );
        } catch(NamingException e) {
            throw new ContainerException(
                i18n.getMessage("notificationConsumerHomeLookupFailure"), e);
        }
    }

    /**
     * Start the notification consumer service
     *
     * @throws ContainerException
     */
    public abstract void startListening() throws ContainerException;

    /**
     * Stop the notification consumer service
     *
     * @throws ContainerException
     */
    public abstract void stopListening() throws ContainerException;

    /**
     * Is the notification consumer service started?
     *
     * @return True if the notification consumer serivce is running, false if
     *         not
     */
    public abstract boolean isListening();

    /**
     * Returns the base URL of the container in which the notification
     * consumer service is running in.
     *
     * @return the base URL of the container running the notification
     *         consumer service.
     */
    public abstract URL getURL();

    /**
     * Create a notification consumer resource with the given callbacks. Note
     * that this method requires that the notification consumer is listening.
     *
     * @param topicPaths A array of concrete topic paths
     * @param callbacks  A array of callbacks corresponding to the topics in the
     *                   topic path array. A individual callback will be called
     *                   when the notification consumer services receives a
     *                   notification message on the topic corresponding to the
     *                   callback.
     * @return The endpoint reference of the created notification consumer
     *         resource
     * @throws ResourceException
     */
    public EndpointReferenceType createNotificationConsumer(
        List[] topicPaths,
        NotifyCallback[] callbacks)
        throws ResourceException {
        return createNotificationConsumer(topicPaths, callbacks, null);
    }

    /**
     * Create a notification consumer resource with the given parameters. Note
     * that this method requires that the notification consumer is listening.
     *
     * @param topicPaths A array of concrete topic paths
     * @param callbacks  A array of callbacks corresponding to the topics in the
     *                   topic path array. A individual callback will be called
     *                   when the notification consumer services receives a
     *                   notification message on the topic corresponding to the
     *                   callback.
     * @param desc       The resource security descriptor to set on the created
     *                   notification consumer resource.
     * @return The endpoint reference of the created notification consumer
     *         resource
     * @throws ResourceException
     */
    public EndpointReferenceType createNotificationConsumer(
        List[] topicPaths,
        NotifyCallback[] callbacks,
        ResourceSecurityDescriptor desc)
        throws ResourceException {
        if((topicPaths == null && callbacks != null) ||
           (topicPaths != null && callbacks == null) ||
           (topicPaths != null && callbacks != null &&
            topicPaths.length != callbacks.length)) {
                throw new IllegalArgumentException(
                    i18n.getMessage("notificationConsumerArgumentMismatch"));
            }

        if (!isListening()) {
            //TODO: typed exception for this?
            throw new ResourceException(
                i18n.getMessage("notificationConsumerNotListening"));
        }

        ResourceKey key = this.notificationConsumerHome.create(desc);
        this.consumers.put(key, "");

        EndpointReferenceType epr = null;
        NotificationConsumerCallbackManager manager = null;

        try {
            String address = getURL().toString() +
                             getNotificationConsumerServiceName();
            epr = AddressingUtils.createEndpointReference(address,
                                                          key);
            manager = (NotificationConsumerCallbackManager)
                this.notificationConsumerHome.find(key);
        } catch (ResourceException e) {
            throw e;
        } catch(Exception e) {
            throw new ResourceException("", e);
        }

        if(topicPaths != null) {
            for(int i = 0; i < callbacks.length; i++) {
                manager.registerCallback(topicPaths[i], callbacks[i]);
            }
        }

        return epr;
    }

    /**
     * Create a notification consumer resource with the given callback. Note
     * that this method requires that the notification consumer is listening.
     *
     * @param callback The callback to be called for incoming notifications
     * @return The endpoint reference of the created notification consumer
     *         resource
     * @throws ResourceException
     */
    public EndpointReferenceType createNotificationConsumer(
        NotifyCallback callback) throws ResourceException {
        return createNotificationConsumer(callback, null);
    }

    /**
     * Create a notification consumer resource with the given callback. Note
     * that this method requires that the notification consumer is listening.
     *
     * @param callback The callback to call upon receipt of a notification on
     *                 the aboce topic
     * @param desc     The resource security descriptor to set on the created
     *                 notification consumer resource.
     * @return The endpoint reference of the created notification consumer
     *         resource
     * @throws ResourceException
     */
    public EndpointReferenceType createNotificationConsumer(
        NotifyCallback callback, ResourceSecurityDescriptor desc)
        throws ResourceException {
        return createNotificationConsumer(new List[]{null},
                                          new NotifyCallback[]{callback},
                                          desc);
    }



    /**
     * Create a notification consumer resource with the given callback. Note
     * that this method requires that the notification consumer is listening.
     *
     * @param topicPath The concrete topic for which the callback will be
     *                  called
     * @param callback  The callback to call upon receipt of a notification on
     *                  the aboce topic
     * @return The endpoint reference of the created notification consumer
     *         resource
     * @throws ResourceException
     */
    public EndpointReferenceType createNotificationConsumer(
        List topicPath, NotifyCallback callback) throws ResourceException {
        return createNotificationConsumer(topicPath, callback, null);
    }

    /**
     * Create a notification consumer resource with the given callback. Note
     * that this method requires that the notification consumer is listening.
     *
     * @param topicPath The concrete topic for which the callback will be
     *                  called
     * @param callback  The callback to call upon receipt of a notification on
     *                  the aboce topic
     * @param desc      The resource security descriptor to set on the created
     *                  notification consumer resource.
     * @return The endpoint reference of the created notification consumer
     *         resource
     * @throws ResourceException
     */
    public EndpointReferenceType createNotificationConsumer(
        List topicPath, NotifyCallback callback,
        ResourceSecurityDescriptor desc) throws ResourceException {
        return createNotificationConsumer(new List[]{topicPath},
                                          new NotifyCallback[]{callback},
                                          desc);
    }

    /**
     * Create a notification consumer resource with no registered callbacks
     * (incoming notifications will be ignored unless you manually register
     * callbacks). Note that this method requires that the notification consumer
     * is listening.
     *
     * @return The endpoint reference of the created notification consumer
     *         resource
     * @throws ResourceException
     */
    public EndpointReferenceType createNotificationConsumer()
        throws ResourceException {
        return createNotificationConsumer((List[]) null, null);
    }

    private ResourceKey getKey(
        EndpointReferenceType consumerEndpointReference)
        throws InvalidResourceKeyException {
        ReferencePropertiesType referenceProperties =
            consumerEndpointReference.getProperties();
        return new SimpleResourceKey(
            referenceProperties.get_any()[0],
            this.notificationConsumerHome.getKeyTypeClass()
        );
    }

    /**
     * Get the callback manager for a given notification consumer endpoint
     * reference
     *
     * @param consumerEndpointReference The EPR for which to retrieve the
     *                                  callback manager
     * @return The callback manager
     */
    public NotificationConsumerCallbackManager
        getNotificationConsumerCallbackManager(
        EndpointReferenceType consumerEndpointReference)
        throws ResourceException {
        ResourceKey key = getKey(consumerEndpointReference);
        return (NotificationConsumerCallbackManager)
            this.notificationConsumerHome.find(key);
    }

    /**
     * Destroy a notification consumer resource
     *
     * @param consumerEndpointReference The endpoint reference of the resource
     *                                  to remove
     */
    public synchronized void removeNotificationConsumer(
        EndpointReferenceType consumerEndpointReference)
        throws ResourceException {
        ResourceKey key = getKey(consumerEndpointReference);
        this.notificationConsumerHome.remove(key);
        this.consumers.remove(key);
    }

    /**
     * Get the name of the default notification consumer service
     *
     * @return The name of the default notification consumer service
     */
    protected String getNotificationConsumerServiceName() {
        return Constants.NOTIFICATION_CONSUMER_SERVICE_NAME;
    }

    /**
     * Removes all notification consumer resources associated with this
     * NotificationConsumerManager.
     */
    protected synchronized void removeNotificationConsumers() {
        Iterator iter = this.consumers.keySet().iterator();
        while(iter.hasNext()) {
            ResourceKey key = (ResourceKey)iter.next();
            try {
                this.notificationConsumerHome.remove(key);
            } catch (Exception e) {
                logger.debug(
                     i18n.getMessage("notificationConsumerCleanupFailed"), e);
            }
        }
        this.consumers.clear();
    }

}
