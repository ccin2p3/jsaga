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

/**
 * This exception is raised if the given query dialect is unsupported.
 */
public class UnsupportedQueryDialectException extends QueryException {

    /**
     * Creates a UnsupportedQueryDialectException without error message.
     */
    public UnsupportedQueryDialectException() {
    }
    
    /**
     * Creates a UnsupportedQueryDialectException with a given error message.
     *
     * @param message error message
     */
    public UnsupportedQueryDialectException(String message) {
        super(message);
    }
    
    /**
     * Creates a UnsupportedQueryDialectException with a given error message 
     * and nested exception.
     *
     * @param message error message
     * @param exception nested exception/
     */
    public UnsupportedQueryDialectException(String message,
                                            Throwable exception) {
        super(message, exception);
    }
    
    /**
     * Creates a UnsupportedQueryDialectException from a nested exception.
     *
     * @param exception nested exception
     */
    public UnsupportedQueryDialectException(Throwable exception) {
        super("", exception);
    }
    
}
