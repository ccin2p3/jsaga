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
package org.globus.rendezvous.service;

import java.rmi.RemoteException;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.oasis.wsrf.lifetime.ResourceUnknownFaultType;

import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.util.I18n;

import org.globus.rendezvous.service.utils.FaultUtils;
import org.globus.rendezvous.generated.CapacityReachedFaultType;
import org.globus.rendezvous.generated.RankTakenFaultType;
import org.globus.rendezvous.generated.RegisterInput;
import org.globus.rendezvous.generated.RegisterResponse;
import org.globus.rendezvous.generated.RendezvousPortType;
import org.globus.rendezvous.generated.service.RendezvousServiceAddressingLocator;


public class RegisterProvider {

    private RendezvousResource getResource() throws RemoteException {
        Object resource = null;

        try {
            resource = ResourceContext.getResourceContext().getResource();
        } catch (NoSuchResourceException e) {
            ResourceUnknownFaultType fault = new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.setDescription(
                          i18nCore.getMessage("resourceDisoveryFailed")
            );
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (Exception e) {
            throw new RemoteException(
                i18nCore.getMessage("resourceDisoveryFailed"), e);
        }

        return (RendezvousResource) resource;
    }

    public RegisterResponse register(RegisterInput arg0)
        throws RemoteException, CapacityReachedFaultType, RankTakenFaultType
    {
        int rank = -1;

        RendezvousResource rendezvous = this.getResource();

        synchronized(rendezvous)
        {
            if (!rendezvous.isFull()) {
                int desiredRank = arg0.getRank(); //hope no loss
                rank = rendezvous.register(arg0.getData(), desiredRank);
            }
            else //all have registered already
            {
                String errorMessage = i18n.getMessage(
           org.globus.rendezvous.service.utils.Resources.RENDEZVOUS_FULL_ERROR);
                logger.error(errorMessage);
                CapacityReachedFaultType fault = (CapacityReachedFaultType)
                    FaultUtils.makeFault(
                        CapacityReachedFaultType.class, errorMessage, null);
                throw fault;
            }
        }

        RegisterResponse response = new RegisterResponse();
        response.setRank(rank);
        return response;
     }

    private static Log logger = LogFactory.getLog(
        RegisterProvider.class.getName());

    private static I18n i18nCore = I18n.getI18n(
        org.globus.wsrf.utils.Resources.class.getName());

    private static I18n i18n = I18n.getI18n(
        org.globus.rendezvous.service.utils.Resources.class.getName());
}
