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
package org.globus.wsrf.client;

import org.oasis.wsrf.lifetime.WSResourceLifetimeServiceAddressingLocator;
import org.oasis.wsrf.lifetime.ImmediateResourceTermination;
import org.oasis.wsrf.faults.BaseFaultType;

import org.globus.wsrf.utils.FaultHelper;

import javax.xml.rpc.Stub;

import org.apache.commons.cli.ParseException;

public class Destroy extends BaseClient {

    public static void main(String[] args) {
        Destroy client = new Destroy();
        try {
            client.parse(args);
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(COMMAND_LINE_ERROR);
        }
        
        WSResourceLifetimeServiceAddressingLocator locator =
            new WSResourceLifetimeServiceAddressingLocator();

        try {
            ImmediateResourceTermination port = 
                locator.getImmediateResourceTerminationPort(client.getEPR());
	    client.setOptions((Stub)port);
            port.destroy(new org.oasis.wsrf.lifetime.Destroy());
            System.out.println("Destroy operation was successful");
        } catch (Exception e) {
            if (client.isDebugMode()) {
                FaultHelper.printStackTrace(e);
            } else {
                System.err.println("Error: " + FaultHelper.getMessage(e));
            }
            System.exit(APPLICATION_ERROR);
        }
    }
}
