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
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.NotificationConsumerManager;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.container.ContainerException;

public class ServerNotificationConsumerManager
    extends NotificationConsumerManager {

    static Log logger =
        LogFactory.getLog(ServerNotificationConsumerManager.class.getName());

    private URL url;
    private boolean isListening = false;

    public ServerNotificationConsumerManager() {
        try {
            this.url = ServiceHost.getBaseURL();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private synchronized void initialize() throws ContainerException {
        if (this.notificationConsumerHome == null) {
            initializeConsumerHome();
        }
    }

    /**
     * Start the notification consumer service. Noop.
     *
     * @throws ContainerException
     */
    public synchronized void startListening() throws ContainerException {
        initialize();
        this.isListening = true;
        
    }

    /**
     * Stop the notification consumer service. Noop.
     *
     * @throws ContainerException
     */
    public synchronized void stopListening() throws ContainerException {
        removeNotificationConsumers();
        this.isListening = false;
    }

    /**
     * Returns if the notification consumer service is running.
     *
     * @return always <code>true</code>.
     *
     */
    public synchronized boolean isListening() {
        return this.isListening;
    }
    
    public URL getURL() {
        return this.url;
    }
    
}
