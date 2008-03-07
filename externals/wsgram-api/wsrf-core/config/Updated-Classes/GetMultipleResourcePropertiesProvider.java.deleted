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

import org.oasis.wsrf.properties.GetMultipleResourcePropertiesResponse;
import org.oasis.wsrf.properties.GetMultipleResourceProperties_Element;
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
import java.util.List;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

public class GetMultipleResourcePropertiesProvider {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(GetMultipleResourceProperties_Element request)
        throws RemoteException,
               InvalidResourcePropertyQNameFaultType,
               ResourceUnknownFaultType {

        if (request == null) {
            InvalidResourcePropertyQNameFaultType fault =
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                          i18n.getMessage("nullArgument", "request")
            );
            throw fault;
        }

        QName [] qnames = request.getResourceProperty();

        if (qnames == null) {
            InvalidResourcePropertyQNameFaultType fault =
                new InvalidResourcePropertyQNameFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(i18n.getMessage("noRPNames"));
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

        List list = new ArrayList();
        SOAPElement [] rp;
        for (int i=0;i<qnames.length;i++) {
            ResourceProperty prop = set.get(qnames[i]);
            if (prop == null) {
                InvalidResourcePropertyQNameFaultType fault =
                    new InvalidResourcePropertyQNameFaultType();
                FaultHelper faultHelper = new FaultHelper(fault);
                faultHelper.setDescription(qnames[i].toString());
                throw fault;
            }
            try {
                rp = prop.toSOAPElements();
            } catch (Exception e) {
                throw new RemoteException(
                    i18n.getMessage("rpSerializationError", prop), e);
            }
            if (rp != null) {
                for (int j=0;j<rp.length;j++) {
                    list.add(rp[j]);
                }
            }
        }

        GetMultipleResourcePropertiesResponse response =
            new GetMultipleResourcePropertiesResponse();

        AnyHelper.setAny(response, list);

        return response;
    }
}
