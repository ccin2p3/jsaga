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
package org.globus.wsrf.query;

import org.globus.common.ChainedException;

/**
 * This is a basic exception raised by <code>QueryEngine</code> or 
 * <code>ExpressionEvaluator</code>.
 */
public class QueryException extends ChainedException {

    /**
     * Creates a QueryException without error message.
     */
    public QueryException() {
    }
    
    /**
     * Creates a QueryException with a given error message.
     *
     * @param message error message
     */
    public QueryException(String message) {
        super(message);
    }
    
    /**
     * Creates a QueryException with a given error message and nested
     * exception.
     *
     * @param message error message
     * @param exception nested exception/
     */
    public QueryException(String message,
                          Throwable exception) {
        super(message, exception);
    }
    
    /**
     * Creates a QueryException from a nested exception.
     *
     * @param exception nested exception
     */
    public QueryException(Throwable exception) {
        super("", exception);
    }
    
}
