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

import org.apache.axis.message.addressing.EndpointReferenceType;

/**
 * Interface that is required to be implemented by callbacks registered with the
 * notification consumer implementation.
 */
public interface NotifyCallback
{
    /**
     * Deliver the notification message
     *
     * @param topicPath The topic path for the topic that generated the
     *                  notification
     * @param producer  The producer endpoint reference
     * @param message   The notification message
     */
    public void deliver(
        List topicPath,
        EndpointReferenceType producer,
        Object message);
}
