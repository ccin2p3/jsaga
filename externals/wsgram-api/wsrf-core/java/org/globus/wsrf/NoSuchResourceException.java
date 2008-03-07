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
 * This exception is raised when a resource for a given key is not found.
 * Usually raised by {@link ResourceHome ResourceHome} operations.
 */
public class NoSuchResourceException extends ResourceException {
    
    public NoSuchResourceException() {
    }
    
    public NoSuchResourceException(String message) {
        super(message);
    }
    
    public NoSuchResourceException(String message,
                                   Throwable ex) {
        super(message, ex);
    }
    
    public NoSuchResourceException(Throwable ex) {
        super(ex);
    }
    
}
