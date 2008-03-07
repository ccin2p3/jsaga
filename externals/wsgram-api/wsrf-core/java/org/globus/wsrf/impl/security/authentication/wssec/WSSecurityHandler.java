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
package org.globus.wsrf.impl.security.authentication.wssec;

import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

// server-side handler
public class WSSecurityHandler extends WSSecurityBasicHandler {

    // server
    public boolean handleRequest(MessageContext context) {
        return handleMessage(
            (SOAPMessageContext) context, WSSecurityRequestEngine.getEngine()
        );
    }

    // client
    public boolean handleResponse(MessageContext context) {
        return true;
    }
}
