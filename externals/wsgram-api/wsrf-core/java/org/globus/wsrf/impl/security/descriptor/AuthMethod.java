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

import javax.xml.rpc.handler.MessageContext;

import java.io.Serializable;

/**
 * Interface for representing authentication method. 
 */
public interface AuthMethod extends Serializable {

    /*
     * Returns true if invocation has been authenticated successfully
     * else returns a false.
     */
    boolean isAuthenticated(MessageContext ctx);

    /**
     * Returns a string that identifies the method
     */   
    String getName();
}
