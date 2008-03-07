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
package org.globus.wsrf.impl.security.authentication.secureconv.service;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceIdentifier;

/**
 * Associates a GSSContext with a context Id. It is used for GSI secure
 * converstation. <b>For internal use only.</b>
 */
public class SecurityContext implements Resource, ResourceIdentifier {

    private GSSContext context;
    private String contextId;

    public SecurityContext(GSSContext context, String contextId) {
        this.context = context;
        this.contextId = contextId;
    }

    public GSSContext getContext() {
        return context;
    }

    public Object getID() {
        return contextId;
    }

    /**
     * A convinience function to extract delegated user
     * credential if any
     *
     * @return the delegated user credential. null if delegation
     *         was not performed.
     */
    public GSSCredential getDelegatedCredential() throws Exception {
        return this.context.getDelegCred();
    }
}
