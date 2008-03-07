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
package org.globus.wsrf.impl.security.descriptor;

import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

public class SecurityDescriptorException extends ElementParserException {
    public SecurityDescriptorException(String message) {
        this(message, null);
    }

    public SecurityDescriptorException(
        String message,
        Exception exception
    ) {
        super(message, exception);
    }
}
