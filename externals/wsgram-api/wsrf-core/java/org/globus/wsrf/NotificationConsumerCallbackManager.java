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

import java.util.List;

/**
 * Interface to be implemented by a notification consumer resource.
 */
public interface NotificationConsumerCallbackManager extends Resource
{
    /**
     * Register a notification callback
     *
     * @param topicPath The topic path for which this callback should be called.
     *                  May be null, in which case the callback is called for
     *                  notification messages for which no other callback is
     *                  registered
     * @param callback  The notification callback
     * @see NotifyCallback
     */
    public void registerCallback(List topicPath, NotifyCallback callback);

    /**
     * Get the notification callback for the supplied topic path
     *
     * @param topicPath The topic path. A null topic path will return the
     *                  default callback.
     * @return The notification callback
     */
    public NotifyCallback getCallback(List topicPath);
}
