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
package org.globus.wsrf.security.authorization;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.globus.wsrf.impl.security.authorization.exceptions.AttributeException;

/**
 * The <code>PIP</code> interface should be implemeted by
 * interceptors that are responsible for collecting attributes
 * for subject that later on can be used by PDPs to determine 
 * whether the subject is allowed to invoke the requested operation.
 * The ServicePIPs can be put into interceptor chains together with
 * PDPs.
 *
 * @see PDP
 * @see org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain
 */
public interface PIP extends Interceptor {
    /**
     * Collects attributes and populates the subject with
     * public or private credentials to be checked by subsequent
     * PDPs in the same interceptor chain
     * @param peerSubject authenticated subject for which attributes
     *                    should be collected 
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     */
    public void collectAttributes(Subject peerSubject,
			         MessageContext context,
	                         QName operation) throws AttributeException;
}
