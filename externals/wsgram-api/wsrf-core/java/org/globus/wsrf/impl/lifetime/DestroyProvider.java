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
package org.globus.wsrf.impl.lifetime;

import java.rmi.RemoteException;

import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.oasis.wsrf.lifetime.ResourceNotDestroyedFaultType;
import org.oasis.wsrf.lifetime.ResourceUnknownFaultType;
import org.oasis.wsrf.lifetime.Destroy;
import org.oasis.wsrf.lifetime.DestroyResponse;

public class DestroyProvider {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public DestroyResponse destroy(Destroy request)
        throws RemoteException,
               ResourceNotDestroyedFaultType,
               ResourceUnknownFaultType {
        try {
            ResourceContext ctx = ResourceContext.getResourceContext();
            ResourceHome home = ctx.getResourceHome();
            ResourceKey key = ctx.getResourceKey();
            home.remove(key);
        } catch (NoSuchResourceException e) {
            ResourceUnknownFaultType fault = new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                          i18n.getMessage("resourceRemoveFailed")
            );
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (Exception e) {
            ResourceNotDestroyedFaultType fault =
                new ResourceNotDestroyedFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                          i18n.getMessage("resourceRemoveFailed")
            );
            faultHelper.addFaultCause(e);
            throw fault;
        }

        return new DestroyResponse();
    }

}
