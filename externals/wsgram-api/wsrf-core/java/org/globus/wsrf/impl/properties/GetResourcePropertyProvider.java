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
package org.globus.wsrf.impl.properties;

import org.oasis.wsrf.properties.GetResourcePropertyResponse;
import org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType;
import org.oasis.wsrf.properties.ResourceUnknownFaultType;

import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.util.I18n;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

public class GetResourcePropertyProvider {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public GetResourcePropertyResponse getResourceProperty(QName name)
        throws RemoteException,
               InvalidResourcePropertyQNameFaultType,
               ResourceUnknownFaultType {

        if (name == null) {
            InvalidResourcePropertyQNameFaultType fault =
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(i18n.getMessage("noRPName"));
            throw fault;
        }

        Object resource = null;
        try {
            resource = ResourceContext.getResourceContext().getResource();
        } catch (NoSuchResourceException e) {
            ResourceUnknownFaultType fault = 
                new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (Exception e) {
            throw new RemoteException(
                i18n.getMessage("resourceDisoveryFailed"), e);
        }

        if (!(resource instanceof ResourceProperties)) {
            throw new RemoteException(i18n.getMessage("rpsNotSupported"));
        }

        ResourcePropertySet set =
            ((ResourceProperties)resource).getResourcePropertySet();

        ResourceProperty prop = set.get(name);
        if (prop == null) {
            InvalidResourcePropertyQNameFaultType fault = 
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(name.toString());
            throw fault;
        }

        GetResourcePropertyResponse response =
            new GetResourcePropertyResponse();

        try {
            AnyHelper.setAny(response, prop.toSOAPElements());
        } catch (Exception e) {
            throw new RemoteException(
                i18n.getMessage("rpSerializationError", prop), e);
        }

        return response;
    }

}
