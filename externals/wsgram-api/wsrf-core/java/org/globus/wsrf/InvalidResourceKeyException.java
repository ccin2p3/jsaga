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
 * This exception is raised if the {@link ResourceKey ResourceKey} is in
 * any way invalid.
 */
public class InvalidResourceKeyException extends ResourceException {
    
    public InvalidResourceKeyException() {
    }
    
    public InvalidResourceKeyException(String message) {
        super(message);
    }
    
    public InvalidResourceKeyException(String message,
                                       Throwable ex) {
        super(message, ex);
    }
    
    public InvalidResourceKeyException(Throwable ex) {
        super(ex);
    }
    
}
