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
package org.globus.delegation.factory;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceException;

import org.globus.delegation.DelegationException;

import org.globus.wsrf.impl.SingletonResourceHome;

public class DelegationFactoryHome extends SingletonResourceHome {
    
    protected Resource findSingleton() throws ResourceException {
        DelegationFactoryResource resource = null;
        try {
            resource = new DelegationFactoryResource();
        } catch (DelegationException exp) {
            throw new ResourceException(exp);
        }
        return resource;
    }
}
