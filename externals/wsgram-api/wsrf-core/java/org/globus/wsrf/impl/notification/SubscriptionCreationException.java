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

import org.globus.wsrf.ResourceException;

/**
 * This exception is raised when the subscription resource creation fails.
 */
public class SubscriptionCreationException extends ResourceException
{
    /**
     * Creates a SubscriptionCreationException without error message.
     */
    public SubscriptionCreationException()
    {
    }

    /**
     * Creates a SubscriptionCreationException with a given error message.
     *
     * @param message error message
     */
    public SubscriptionCreationException(String message)
    {
        super(message);
    }

    /**
     * Creates a SubscriptionCreationException with a given error message and
     * nested exception.
     *
     * @param message   error message
     * @param exception nested exception/
     */
    public SubscriptionCreationException(String message, Throwable exception)
    {
        super(message, exception);
    }

    /**
     * Creates a SubscriptionCreationException from a nested exception.
     *
     * @param exception nested exception
     */
    public SubscriptionCreationException(Throwable exception)
    {
        super(exception);
    }
}
