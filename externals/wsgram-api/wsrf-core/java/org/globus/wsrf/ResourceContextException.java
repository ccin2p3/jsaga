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

import java.rmi.RemoteException;

/**
 * This is a basic exception raised by {@link ResourceContext ResourceContext}
 * operations.
 */
public class ResourceContextException extends RemoteException {

    /**
     * Creates a ResourceContextException without error message.
     */
    public ResourceContextException() {
    }
    
    /**
     * Creates a ResourceContextException with a given error message.
     *
     * @param message error message
     */
    public ResourceContextException(String message) {
        super(message);
    }
    
    /**
     * Creates a ResourceContextException with a given error message and nested
     * exception.
     *
     * @param message error message
     * @param exception nested exception/
     */
    public ResourceContextException(String message,
                                    Throwable exception) {
        super(message, exception);
    }
    
    /**
     * Creates a ResourceContextException from a nested exception.
     *
     * @param exception nested exception
     */
    public ResourceContextException(Throwable exception) {
        super("", exception);
    }
    
}
