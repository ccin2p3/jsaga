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
package org.globus.wsrf.encoding;

import org.globus.common.ChainedException;

/**
 * This exception is raised if the deserialization to an object failed.
 */
public class DeserializationException extends ChainedException {
    
    /**
     * Creates a DeserializationException without error message.
     */
    public DeserializationException() {
    }
    
    /**
     * Creates a DeserializationException with a given error message.
     *
     * @param message error message
     */
    public DeserializationException(String message) {
        super(message);
    }
    
    /**
     * Creates a DeserializationException with a given error message and nested
     * exception.
     *
     * @param message error message
     * @param exception nested exception/
     */
    public DeserializationException(String message,
                                    Throwable exception) {
        super(message, exception);
    }
    
    /**
     * Creates a DeserializationException from a nested exception.
     *
     * @param exception nested exception
     */
    public DeserializationException(Throwable exception) {
        super("", exception);
    }
    
}
