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


import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.axis.MessageContext;

public class WSSecurityCallbackHandler implements CallbackHandler {

    MessageContext context;

    public WSSecurityCallbackHandler(MessageContext context) {
        this.context = context;
    }

    public MessageContext getContext()
    {
        return context;
    }

    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
        return;
    }
}
