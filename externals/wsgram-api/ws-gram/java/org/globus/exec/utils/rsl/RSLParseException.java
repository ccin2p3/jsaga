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
package org.globus.exec.utils.rsl;

/**
 * This exception is raised if the serialization to or from an object failed
 */
public class RSLParseException extends Exception {
    
    public RSLParseException() { }
    
    public RSLParseException(
        String                              message)
    {
        super(message);
    }
    
    public RSLParseException(
        String                              message,
        Throwable                           cause)
    {
        super(message, cause);
    }
    
    public RSLParseException(
        Throwable                           cause)
    {
        super(cause);
    }
    
}
