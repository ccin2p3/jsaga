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

import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceIdentifier;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.utils.AddressingUtils;

import org.globus.rendezvous.generated.RendezvousPortType;
import org.globus.rendezvous.generated.service.RendezvousServiceAddressingLocator;
import org.globus.rendezvous.generated.CreateSync;
import org.globus.rendezvous.generated.CreateSyncResponse;

public class RendezvousFactoryService
{

   public CreateSyncResponse createSync(CreateSync request)
      throws RemoteException
   {
      ResourceContext ctx = null;
      RendezvousHome home = null;
      ResourceKey key = null;

      try
      {
         ctx = ResourceContext.getResourceContext();
         home = (RendezvousHome) ctx.getResourceHome();
         ResourceIdentifier resource = home.create(
              request.getCapacity());
         key = (ResourceKey)resource.getID();
      }
      catch (RemoteException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RemoteException("", e);
      }

      EndpointReferenceType epr = null;
      try
      {
          epr = AddressingUtils.createEndpointReference(ctx, key);
      }
      catch(Exception e)
      {
          throw new RemoteException("", e);
      }

      CreateSyncResponse response = new CreateSyncResponse();
      response.setEndpointReference(epr);

      return response;
   }

}
