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

/**
 * This is a basic exception raised by {@link ResourceContext ResourceContext}
 * operations in cases where a ResourceHome is not configured for a given
 * service.
 */
public class NoResourceHomeException extends ResourceContextException {

    /**
     * Creates a NoResourceHomeException without error message.
     */
    public NoResourceHomeException() {
    }
    
    /**
     * Creates a NoResourceHomeException with a given error message.
     *
     * @param message error message
     */
    public NoResourceHomeException(String message) {
        super(message);
    }
    
    /**
     * Creates a NoResourceHomeException with a given error message and nested
     * exception.
     *
     * @param message error message
     * @param exception nested exception/
     */
    public NoResourceHomeException(String message,
                                   Throwable exception) {
        super(message, exception);
    }
    
    /**
     * Creates a NoResourceHomeException from a nested exception.
     *
     * @param exception nested exception
     */
    public NoResourceHomeException(Throwable exception) {
        super("", exception);
    }
    
}
