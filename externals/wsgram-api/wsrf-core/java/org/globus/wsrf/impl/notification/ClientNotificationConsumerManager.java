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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.container.ContainerException;
import org.globus.wsrf.container.ServiceContainer;

public class ClientNotificationConsumerManager
    extends NotificationConsumerManager {

    static Log logger =
        LogFactory.getLog(ClientNotificationConsumerManager.class.getName());

    private ServiceContainer serviceContainer = null;
    private Map properties = null;

    protected static final String CLIENT_PROFILE = "client";
    
    public ClientNotificationConsumerManager() {
        this(new HashMap());
    }

    public ClientNotificationConsumerManager(Map properties) {
        this.properties = properties;
        
        // overwrite these?
        this.properties.put(ServiceContainer.MAIN_THREAD,
                            Boolean.FALSE);
        this.properties.put(ContainerConfig.CONFIG_PROFILE,
                            CLIENT_PROFILE);
    }

    /**
     * Start the notification consumer service
     *
     * @throws ContainerException
     */
    public synchronized void startListening()
        throws ContainerException {
        if (this.serviceContainer != null) {
            return;
        }

        serviceContainer = ServiceContainer.createContainer(this.properties);

        // should have jndi context setup by now
        if (this.notificationConsumerHome == null) {
            initializeConsumerHome();
        }
    }

    /**
     * Stop the notification consumer service
     *
     * @throws ContainerException
     */
    public synchronized void stopListening()
        throws ContainerException {
        if (this.serviceContainer == null) {
            return;
        }
        removeNotificationConsumers();
        this.serviceContainer.stop();
        this.serviceContainer = null;
    }

    /**
     * Is the notification consumer service started?
     *
     * @return True if the notification consumer serivce is running, false if
     *         not
     */
    public synchronized boolean isListening() {
        return (this.serviceContainer != null);
    }

    public synchronized URL getURL() {
        return (this.serviceContainer != null) ?
            this.serviceContainer.getURL() : null;
    }

}
