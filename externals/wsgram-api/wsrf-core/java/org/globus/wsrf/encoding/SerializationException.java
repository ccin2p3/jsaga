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
 * This exception is raised if the serialization from an object failed.
 */
public class SerializationException extends ChainedException {
    
    /**
     * Creates a SerializationException without error message.
     */
    public SerializationException() {
    }
    
    /**
     * Creates a SerializationException with a given error message.
     *
     * @param message error message
     */
    public SerializationException(String message) {
        super(message);
    }
    
    /**
     * Creates a SerializationException with a given error message and nested
     * exception.
     *
     * @param message error message
     * @param exception nested exception/
     */
    public SerializationException(String message,
                                  Throwable exception) {
        super(message, exception);
    }
    
    /**
     * Creates a SerializationException from a nested exception.
     *
     * @param exception nested exception
     */
    public SerializationException(Throwable exception) {
        super("", exception);
    }
    
}
