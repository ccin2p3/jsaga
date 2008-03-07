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

public interface ContainerOnlyParamsParserCallback {

    /**
     * Sets the interval used by the timer that cleans up expired
     * contexts generated on using GSI Secure Conversation.
     */
    void setContextTimerInterval(String value) 
        throws SecurityDescriptorException;

    /**
     * Sets the interval used by the timer that cleans up expired
     * values saved for preventing replay attack 
     */
    void setReplayTimerInterval(String value) 
        throws SecurityDescriptorException;
}
