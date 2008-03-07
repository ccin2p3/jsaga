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
package org.globus.axis.providers;

import org.apache.axis.MessageContext;

import java.lang.reflect.Method;

import java.security.PrivilegedExceptionAction;

class PrivilegedInvokeMethodAction implements PrivilegedExceptionAction {

    private RPCProvider provider;
    private MessageContext msgContext;
    private Method method;
    private Object obj;
    private Object[] argValues;

    public PrivilegedInvokeMethodAction(
        RPCProvider provider,
        MessageContext msgContext,
        Method method,
        Object obj,
        Object[] argValues
    ) {
        this.provider = provider;
        this.msgContext = msgContext;
        this.method = method;
        this.obj = obj;
        this.argValues = argValues;
    }

    public Object run() throws Exception {
	return this.provider.invokeMethodSub(this.msgContext, this.method, 
					     this.obj,
					     this.argValues);
    }
}
