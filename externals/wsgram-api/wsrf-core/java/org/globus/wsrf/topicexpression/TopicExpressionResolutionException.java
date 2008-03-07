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

/**
 * This exception is raised when resolution of a topic expression fails.
 */
public class TopicExpressionResolutionException extends TopicExpressionException
{
    /**
     * Creates a TopicExpressionResolutionException without error message.
     */
    public TopicExpressionResolutionException()
    {
    }

    /**
     * Creates a TopicExpressionResolutionException with a given error message.
     *
     * @param message error message
     */
    public TopicExpressionResolutionException(String message)
    {
        super(message);
    }

    /**
     * Creates a TopicExpressionResolutionException with a given error message
     * and nested exception.
     *
     * @param message   error message
     * @param exception nested exception
     */
    public TopicExpressionResolutionException(
        String message,
        Throwable exception)
    {
        super(message, exception);
    }

    /**
     * Creates a TopicExpressionResolutionException from a nested exception.
     *
     * @param exception nested exception
     */
    public TopicExpressionResolutionException(Throwable exception)
    {
        super(exception);
    }
}
