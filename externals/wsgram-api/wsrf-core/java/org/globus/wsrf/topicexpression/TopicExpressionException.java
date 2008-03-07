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
package org.globus.wsrf.topicexpression;

import org.globus.common.ChainedException;

/**
 * This is a basic exception raised by <code>TopicExpressionEngine</code> or
 * <code>TopicExpressionEvaluator</code>.
 */
public class TopicExpressionException extends ChainedException
{
    /**
     * Creates a TopicExpressionException without error message.
     */
    public TopicExpressionException()
    {
    }

    /**
     * Creates a TopicExpressionException with a given error message.
     *
     * @param message error message
     */
    public TopicExpressionException(String message)
    {
        super(message);
    }

    /**
     * Creates a TopicExpressionException with a given error message and nested
     * exception.
     *
     * @param message error message
     * @param exception nested exception/
     */
    public TopicExpressionException(String message, Throwable exception)
    {
        super(message, exception);
    }

    /**
     * Creates a TopicExpressionException from a nested exception.
     *
     * @param exception nested exception
     */
    public TopicExpressionException(Throwable exception)
    {
        super("", exception);
    }
}
